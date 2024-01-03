/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
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

import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.FeedWithFavIcon
import com.geekorum.ttrss.data.FeedsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * A Facade to access Feeds and Categories.
 */
class FeedsRepository
@Inject constructor(
    private val feedsDao: FeedsDao
) {
    val allUnreadFeeds: Flow<List<FeedWithFavIcon>> = feedsDao.getAllUnreadFeeds().map(this::addSpecialFeeds)

    val allFeeds: Flow<List<FeedWithFavIcon>> = feedsDao.getAllFeeds().map(this::addSpecialFeeds)

    val allCategories: Flow<List<Category>> = feedsDao.getAllCategories()

    val allUnreadCategories: Flow<List<Category>> = feedsDao.getAllUnreadCategories()

    fun getFeedById(feedId: Long): Flow<Feed?> {
        return when {
            Feed.isVirtualFeed(feedId) -> flowOf(Feed.createVirtualFeedForId(feedId))
            else -> feedsDao.getFeedById(feedId)
        }
    }

    private fun addSpecialFeeds(feeds: List<FeedWithFavIcon>): List<FeedWithFavIcon> {
        // add special feeds
        val totalUnread = feeds.sumOf { it.feed.unreadCount }
        val allArticles = FeedWithFavIcon(
            feed = Feed.createVirtualFeedForId(Feed.FEED_ID_ALL_ARTICLES, totalUnread),
            favIcon = null)

        //TODO calculate how much articles by special feeds
        val freshArticles = FeedWithFavIcon(
            feed = Feed.createVirtualFeedForId(Feed.FEED_ID_FRESH),
            favIcon = null)
        val starredArticles = FeedWithFavIcon(
            feed = Feed.createVirtualFeedForId(Feed.FEED_ID_STARRED),
            favIcon = null)

        return listOf(allArticles, freshArticles, starredArticles, *feeds.toTypedArray())
    }

    fun getUnreadFeedsForCategory(catId: Long): Flow<List<Feed>> {
        return feedsDao.getUnreadFeedsForCategory(catId)
    }

    fun getFeedsForCategory(catId: Long): Flow<List<Feed>> {
        return feedsDao.getFeedsForCategory(catId)
    }

    suspend fun setFeedsAndCategories(feeds: Collection<Feed>, categories: Collection<Category>) {
        feedsDao.setFeedsAndCategories(feeds, categories)
    }

    suspend fun updateFeedsAndCategoriesUnreadCount(
        feeds: Collection<Feed>, categories: Collection<Category>
    ) {
        feedsDao.updateFeedsAndCategoriesUnreadCount(feeds, categories)
    }

}
