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
package com.geekorum.ttrss.articles_browsing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geekorum.ttrss.articles_list.FeedsRepository
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.session.SessionActivityComponent
import com.geekorum.ttrss.webapi.ApiCallException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

private const val STATE_ONLY_UNREAD = "only_unread"

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class BrowsingSceneDecoratorViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val dispatchers: CoroutineDispatchersProvider,
    private val feedsRepository: FeedsRepository,
    componentFactory: SessionActivityComponent.Factory
): ViewModel() {

//    val account: StateFlow<Account?>

    private val component = componentFactory.newComponent()
    private val apiService: ApiService = component.apiService

    private var refreshFeedsJob: Job? = null

    private val onlyUnread = state.getStateFlow(STATE_ONLY_UNREAD, true)

    val feeds = onlyUnread.flatMapLatest { onlyUnread ->
        if (onlyUnread) feedsRepository.allUnreadFeeds else feedsRepository.allFeeds
    }
        .autoRefreshed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Launch job to refresh feeds and categories when collecting this flow and cancel it on flow completion
     */
    private fun <T> Flow<T>.autoRefreshed() = this.onStart {
        startRefreshFeedsJob()
    }.onCompletion {
        cancelRefreshFeedsJob()
    }

    private suspend fun startRefreshFeedsJob() = withContext(dispatchers.main) {
        if (refreshFeedsJob == null) {
            refreshFeedsJob = viewModelScope.launch {
                try {
                    while (isActive) {
                        refreshFeeds()
                        delay(30_000)
                    }
                } catch (e: ApiCallException) {
                    Timber.w(e, "Unable to refresh feeds and categories")
                }
            }
        }
    }

    private suspend fun cancelRefreshFeedsJob() = withContext(dispatchers.main) {
        refreshFeedsJob?.cancel()
        refreshFeedsJob = null
    }

    @Throws(ApiCallException::class)
    private suspend fun refreshFeeds() = coroutineScope {
        withContext(dispatchers.io) {
            val feeds = async { apiService.getFeeds() }
            val categories = async { apiService.getCategories() }
            feedsRepository.updateFeedsAndCategoriesUnreadCount(feeds.await(), categories.await())
        }
    }

}