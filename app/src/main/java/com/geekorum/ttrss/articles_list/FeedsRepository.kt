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
import androidx.lifecycle.Transformations
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.FeedsDao
import com.geekorum.ttrss.network.ApiCallException
import com.geekorum.ttrss.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * A Facade to access Feeds and Categories.
 * TODO: add a better mechanism to refresh less often
 */
class FeedsRepository
@Inject constructor(
        private val feedsDao: FeedsDao,
        private val apiService: ApiService
) {
    val allUnreadFeeds: LiveData<List<Feed>>
        get() {
            refresh()
            return Transformations.map(feedsDao.allUnreadFeeds, this::addSpecialFeeds)
        }

    val allFeeds: LiveData<List<Feed>>
        get() {
            refresh()
            return Transformations.map(feedsDao.allFeeds, this::addSpecialFeeds)
        }

    val allCategories: LiveData<List<Category>>
        get() {
            refresh()
            return feedsDao.allCategories
        }

    val allUnreadCategories: LiveData<List<Category>>
        get() {
            refresh()
            return feedsDao.allUnreadCategories
        }

    fun getFeedById(feedId: Long): LiveData<Feed?> {
        return when {
            Feed.isVirtualFeed(feedId) -> MutableLiveData<Feed>().apply {
                value = Feed.createVirtualFeedForId(feedId)
            }
            else -> feedsDao.getFeedById(feedId)
        }
    }

    private fun addSpecialFeeds(feeds: List<Feed>): List<Feed> {
        // add special feeds
        val totalUnread = feeds.sumBy { it.unreadCount }
        val allArticles = Feed.createVirtualFeedForId(Feed.FEED_ID_ALL_ARTICLES)
        allArticles.unreadCount = totalUnread

        //TODO calculate how much articles by special feeds
        val freshArticles = Feed.createVirtualFeedForId(Feed.FEED_ID_FRESH)
        val starredArticles = Feed.createVirtualFeedForId(Feed.FEED_ID_STARRED)

        return listOf(allArticles, freshArticles, starredArticles, *feeds.toTypedArray())
    }

    fun getUnreadFeedsForCategory(catId: Long): LiveData<List<Feed>> {
        return feedsDao.getUnreadFeedsForCategory(catId)
    }

    fun getFeedsForCategory(catId: Long): LiveData<List<Feed>> {
        return feedsDao.getFeedsForCategory(catId)
    }

    private fun refresh() = GlobalScope.launch(Dispatchers.IO) {
        try {
            coroutineScope {
                val feeds = async { apiService.getFeeds() }
                val categories = async { apiService.getCategories() }
                feedsDao.setFeedsAndCategories(feeds.await(), categories.await())
            }
        } catch (e: ApiCallException) {
            Timber.w(e, "Unable to refresh feeds and categories")
        }
    }
}
