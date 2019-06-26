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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.webapi.ApiCallException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject

/**
 * [ViewModel] for [FeedListFragment]
 */
class FeedsViewModel @Inject constructor(
    private val feedsRepository: FeedsRepository,
    private val apiService: ApiService
) : ViewModel() {

    private val onlyUnread = MutableLiveData<Boolean>().apply { value = true }

    private val feedLiveData = onlyUnread.switchMap { onlyUnread ->
        if (onlyUnread) feedsRepository.allUnreadFeeds else feedsRepository.allFeeds
    }.refreshed()

    private val selectedCategory = MutableLiveData<Long>()

    val allFeeds: LiveData<List<Feed>> = feedLiveData

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
        this.onlyUnread.value = onlyUnread
    }

    fun setSelectedCategory(selectedCategoryId: Long) {
        selectedCategory.value = selectedCategoryId
    }

    @Throws(ApiCallException::class)
    private suspend fun refreshFeeds() = coroutineScope {
        val feeds = async { apiService.getFeeds() }
        val categories = async { apiService.getCategories() }
        feedsRepository.setFeedsAndCategories(feeds.await(), categories.await())
    }

    /**
     * Refresh feeds and categories when observing this livedata for the first time
     */
    private fun <T> LiveData<T>.refreshed(): LiveData<T> = liveData {
        emitSource(this@refreshed)
        try {
            refreshFeeds()
        } catch (e: ApiCallException) {
            Timber.w(e, "Unable to refresh feeds and categories")
        }
    }

}
