/**
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2018 by Frederic-Charles Barthelery.
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

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/**
 * Dao to access Feeds and Categories
 */
@Dao
abstract class FeedsDao {

    @get:Query("SELECT * FROM feeds WHERE unread_count > 0 ORDER BY title")
    abstract val allUnreadFeeds: LiveData<List<Feed>>

    @get:Query("SELECT * FROM feeds ORDER BY title")
    abstract val allFeeds: LiveData<List<Feed>>

    @get:Query("SELECT * FROM feeds")
    internal abstract val allFeedsList: List<Feed>

    @get:Query("SELECT * FROM categories ORDER BY title")
    abstract val allCategories: LiveData<List<Category>>

    @get:Query("SELECT * FROM categories WHERE unread_count > 0 ORDER BY title")
    abstract val allUnreadCategories: LiveData<List<Category>>

    @get:Query("SELECT * FROM categories")
    internal abstract val allCategoriesList: List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertFeeds(feeds: Collection<Feed>)

    @Delete
    internal abstract fun deleteFeeds(feeds: Collection<Feed>)

    @Query("DELETE FROM ARTICLES where feed_id=:feedId")
    internal abstract fun deleteArticleFromFeed(feedId: Long)

    @Transaction
    open fun deleteFeedsAndArticles(toBeDelete: List<Feed>) {
        for ((id) in toBeDelete) {
            deleteArticleFromFeed(id)
        }
        deleteFeeds(toBeDelete)
    }


    @Query("SELECT * FROM feeds WHERE _id=:id")
    abstract fun getFeedById(id: Long): LiveData<Feed>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertCategories(categories: Collection<Category>)

    @Delete
    abstract fun deleteCategories(categories: Collection<Category>)

    @Query("SELECT * FROM feeds WHERE unread_count > 0 AND cat_id=:catId ORDER BY title")
    abstract fun getUnreadFeedsForCategory(catId: Long): LiveData<List<Feed>>

    @Query("SELECT * FROM feeds WHERE cat_id=:catId ORDER BY title")
    abstract fun getFeedsForCategory(catId: Long): LiveData<List<Feed>>

    @Transaction
    open fun setFeedsAndCategories(feeds: Collection<Feed>, categories: Collection<Category>) {
        setCategories(categories)
        setFeeds(feeds)
    }

    private fun setFeeds(feeds: Collection<Feed>) {
        val feedsIds: List<Long> = feeds.map { it.id }
        val toDelete = allFeedsList.filter { it.id !in feedsIds }

        deleteFeedsAndArticles(toDelete)
        insertFeeds(feeds)
    }

    private fun setCategories(categories: Collection<Category>) {
        val categoriesIds: List<Long> = categories.map { category -> category.id }
        val toDelete = allCategoriesList.filter { it.id !in categoriesIds }
        deleteCategories(toDelete)
        insertCategories(categories)
    }


}
