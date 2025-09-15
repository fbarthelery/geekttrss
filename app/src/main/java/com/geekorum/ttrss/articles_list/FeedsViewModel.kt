/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
 *
 * This file is part of Geekttrss.
 *
 * Geekttrss is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Geekttrss is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Geekttrss.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.geekorum.ttrss.articles_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.session.SessionActivityComponent
import com.geekorum.ttrss.webapi.ApiCallException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

private const val STATE_ONLY_UNREAD = "only_unread"
private const val STATE_SELECTED_CATEGORY_ID = "selected_category_id"

/**
 * [ViewModel] for to display the list of feeds
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FeedsViewModel @Inject constructor(
    private val dispatchers: CoroutineDispatchersProvider,
    private val feedsRepository: FeedsRepository,
    private val articlesRepository: ArticlesRepository,
    componentFactory: SessionActivityComponent.Factory
) : ViewModel() {

    private val component = componentFactory.newComponent()
    private val apiService: ApiService = component.apiService

    private val onlyUnread = MutableStateFlow(true)

    private val selectedCategory = MutableStateFlow<Long?>(null)

    val feeds = onlyUnread.flatMapLatest { onlyUnread ->
        if (onlyUnread) feedsRepository.allUnreadFeeds else feedsRepository.allFeeds
    }.autoRefreshed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var refreshFeedsJob: Job? = null

    val feedsForCategory = selectedCategory.filterNotNull().flatMapLatest(this::getFeedsForCategory)

    private suspend fun getFeedsForCategory(catId: Long) = onlyUnread.flatMapLatest { onlyUnread ->
        if (onlyUnread)
            feedsRepository.getUnreadFeedsForCategory(catId)
        else
            feedsRepository.getFeedsForCategory(catId)
    }

    val categories = onlyUnread.flatMapLatest { onlyUnread ->
        if (onlyUnread)
            feedsRepository.allUnreadCategories
        else
            feedsRepository.allCategories
    }.autoRefreshed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setOnlyUnread(onlyUnread: Boolean) {
        this.onlyUnread.value = onlyUnread
    }

    fun setSelectedCategory(selectedCategoryId: Long) {
        selectedCategory.value = selectedCategoryId
    }

    @Throws(ApiCallException::class)
    private suspend fun refreshFeeds() = coroutineScope {
        withContext(dispatchers.io) {
            val feeds = async { apiService.getFeeds() }
            val categories = async { apiService.getCategories() }
            feedsRepository.updateFeedsAndCategoriesUnreadCount(feeds.await(), categories.await())
        }
    }

    private suspend fun startRefreshFeedsJob() = withContext(viewModelScope.coroutineContext) {
        if (refreshFeedsJob == null) {
            refreshFeedsJob = viewModelScope.launch {
                try {
                    while(isActive) {
                        refreshFeeds()
                        delay(30_000)
                    }
                } catch (e: ApiCallException) {
                    Timber.w(e, "Unable to refresh feeds and categories")
                }
            }
        }
    }

    private suspend fun cancelRefreshFeedsJob() = withContext(viewModelScope.coroutineContext) {
        refreshFeedsJob?.cancel()
        refreshFeedsJob = null
    }

    /**
     * Launch job to refresh feeds and categories when collecting this flow and cancel it on flow completion
     */
    private fun <T> Flow<T>.autoRefreshed() = this.onStart {
        startRefreshFeedsJob()
    }.onCompletion {
        cancelRefreshFeedsJob()
    }

    fun markFeedAsRead(feed: Feed) = viewModelScope.launch {
        withContext(dispatchers.io) {
            try {
                apiService.markFeedAsRead(feed.id)
                when {
                    feed.isAllArticlesFeed -> articlesRepository.setAllArticlesUnread(false)
                    else -> articlesRepository.setArticlesUnreadForFeed(feed.id, false)
                }
                refreshFeeds()
            } catch (e: ApiCallException) {
                Timber.w(e, "Unable to mark feed as read")
            }
        }
    }

}
