/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2020 by Frederic-Charles Barthelery.
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
import com.geekorum.geekdroid.dagger.ViewModelAssistedFactory
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.webapi.ApiCallException
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

private const val STATE_ONLY_UNREAD = "only_unread"
private const val STATE_SELECTED_CATEGORY_ID = "selected_category_id"

/**
 * [ViewModel] for to display the list of feeds
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedsViewModel @AssistedInject constructor(
    @Assisted private val state: SavedStateHandle,
    private val dispatchers: CoroutineDispatchersProvider,
    private val feedsRepository: FeedsRepository,
    private val apiService: ApiService
) : ViewModel() {

    private val onlyUnread = state.getLiveData(STATE_ONLY_UNREAD, true).asFlow()

    private val selectedCategory = state.getLiveData<Long>(STATE_SELECTED_CATEGORY_ID).asFlow()

    val feeds: Flow<List<Feed>> = onlyUnread.flatMapLatest { onlyUnread ->
        if (onlyUnread) feedsRepository.allUnreadFeeds else feedsRepository.allFeeds
    }.refreshed()


    val feedsForCategory = selectedCategory.flatMapLatest(this::getFeedsForCategory)

    private suspend fun getFeedsForCategory(catId: Long) = onlyUnread.flatMapLatest { onlyUnread ->
        if (onlyUnread)
            feedsRepository.getUnreadFeedsForCategory(catId)
        else
            feedsRepository.getFeedsForCategory(catId)
    }

    val categories: Flow<List<Category>> = onlyUnread.flatMapLatest { onlyUnread ->
        if (onlyUnread)
            feedsRepository.allUnreadCategories
        else
            feedsRepository.allCategories
    }.refreshed()

    fun setOnlyUnread(onlyUnread: Boolean) {
        state[STATE_ONLY_UNREAD] = onlyUnread
    }

    fun setSelectedCategory(selectedCategoryId: Long) {
        state[STATE_SELECTED_CATEGORY_ID] = selectedCategoryId
    }

    @Throws(ApiCallException::class)
    private suspend fun refreshFeeds() = coroutineScope {
        withContext(dispatchers.io) {
            val feeds = async { apiService.getFeeds() }
            val categories = async { apiService.getCategories() }
            feedsRepository.updateFeedsAndCategoriesUnreadCount(feeds.await(), categories.await())
        }
    }

    /**
     * Refresh feeds and categories when collecting this flow for the first time
     */
    private fun <T> Flow<T>.refreshed() = this.onStart {
        viewModelScope.launch {
            try {
                refreshFeeds()
            } catch(e: ApiCallException) {
                Timber.w(e, "Unable to refresh feeds and categories")
            }
        }
    }

    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<FeedsViewModel> {
        override fun create(state: SavedStateHandle): FeedsViewModel
    }
}
