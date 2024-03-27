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
package com.geekorum.ttrss.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ManageFeedsDao {

    @Transaction
    @Query("SELECT * FROM feeds WHERE is_subscribed == 1 ORDER BY title")
    fun getAllSubscribedFeeds(): PagingSource<Int, FeedWithFavIcon>

    @Query("UPDATE feeds SET is_subscribed=:isSubscribed WHERE _id=:feedId")
    suspend fun updateIsSubscribedFeed(feedId: Long, isSubscribed: Boolean)

    @Query("SELECT * FROM feeds WHERE _id=:id")
    abstract fun getFeedById(id: Long): Flow<FeedWithFavIcon?>

    @Transaction
    @Query("SELECT * FROM categories JOIN feeds on categories._id = feeds.cat_id WHERE feeds.is_subscribed == 1 ORDER BY categories.title, feeds.title")
    fun getSubscribedFeedsByCategories(): Flow<Map<Category, List<FeedWithFavIcon>>>

}
