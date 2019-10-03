/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2019 by Frederic-Charles Barthelery.
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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.geekorum.geekdroid.dagger.ViewModelAssistedFactory
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.webapi.ApiCallException
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber

private const val STATE_ONLY_UNREAD = "only_unread"
private const val STATE_SELECTED_CATEGORY_ID = "selected_category_id"
private const val STATE_SELECTED_FEED_ID = "selected_category_id"

/**
 * [ViewModel] for to display the list of feeds
 */
class FeedsViewModel @AssistedInject constructor(
    @Assisted private val state: SavedStateHandle,
    private val feedsRepository: FeedsRepository,
    private val apiService: ApiService
) : ViewModel() {

    private val onlyUnread = state.getLiveData(STATE_ONLY_UNREAD, true)

    private val selectedCategory = state.getLiveData<Long>(STATE_SELECTED_CATEGORY_ID)

    val feeds: LiveData<List<FeedView>> = onlyUnread.switchMap { onlyUnread ->
        if (onlyUnread) feedsRepository.allUnreadFeeds else feedsRepository.allFeeds
    }.map { feeds ->
        // should be reactive on STATE_SELECTED_FEED_ID ?
        feeds.map {
            FeedView(it, it.id == state[STATE_SELECTED_FEED_ID])
        }
    }.refreshed()


    val feedsForCategory = selectedCategory.switchMap(this::getFeedsForCategory)

    private fun getFeedsForCategory(catId: Long) = onlyUnread.switchMap { onlyUnread ->
        if (onlyUnread)
            feedsRepository.getUnreadFeedsForCategory(catId)
        else
            feedsRepository.getFeedsForCategory(catId)
    }

    val categories: LiveData<List<Category>> = onlyUnread.switchMap { onlyUnread ->
        if (onlyUnread)
            feedsRepository.allUnreadCategories
        else
            feedsRepository.allCategories
    }.refreshed()

    fun setOnlyUnread(onlyUnread: Boolean) {
        state[STATE_ONLY_UNREAD] = onlyUnread
    }

    fun setSelectedFeed(selectedFeedId: Long) {
        state[STATE_SELECTED_FEED_ID] = selectedFeedId
    }

    fun setSelectedCategory(selectedCategoryId: Long) {
        state[STATE_SELECTED_CATEGORY_ID] = selectedCategoryId
    }

    @Throws(ApiCallException::class)
    private suspend fun refreshFeeds() = coroutineScope {
        withContext(Dispatchers.IO){
            val feeds = async { apiService.getFeeds() }
            val categories = async { apiService.getCategories() }
            feedsRepository.updateFeedsAndCategoriesUnreadCount(feeds.await(), categories.await())
        }
    }

    /**
     * Refresh feeds and categories when observing this livedata for the first time
     */
    // cause bug https://issuetracker.google.com/issues/140249349
    private fun <T> LiveData<T>.refreshed(): LiveData<T> = liveData {
        emitSource(this@refreshed)
        try {
            refreshFeeds()
        } catch (e: ApiCallException) {
            Timber.w(e, "Unable to refresh feeds and categories")
        }
    }

    data class FeedView(val feed: Feed, val isSelected: Boolean)

    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<FeedsViewModel> {
        override fun create(state: SavedStateHandle): FeedsViewModel
    }
}
