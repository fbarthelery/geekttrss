/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Dao to access the database when synchronizing articles
 */
@Dao
abstract class SynchronizationDao {
    @Query("SELECT * FROM transactions")
    abstract suspend fun getAllTransactions(): List<Transaction>

    @Query("SELECT _id FROM articles ORDER BY _id DESC LIMIT 1")
    abstract suspend fun getLatestArticleId(): Long?

    @Query("SELECT _id FROM articles WHERE feed_id=:feedId ORDER BY _id DESC LIMIT 1")
    abstract suspend fun getLatestArticleIdFromFeed(feedId: Long): Long?

    @Query("SELECT * FROM categories")
    abstract suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM feeds")
    abstract suspend fun getAllFeeds(): List<Feed>

    @Query("SELECT * FROM feed_fav_icon")
    abstract fun getAllFeedFavIcons(): List<FeedFavIcon>

    @Delete
    abstract suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM articles WHERE _id=:id")
    abstract suspend fun getArticleById(id: Long): Article?

    @Query("SELECT * FROM articles WHERE feed_id=:feedId ORDER BY feed_id DESC LIMIT 1")
    abstract fun getLatestArticleFromFeed(feedId: Long): Article?

    @Update
    abstract suspend fun updateArticle(article: Article)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCategories(categories: List<Category>)

    @Query("DELETE FROM ARTICLES where feed_id=:feedId")
    internal abstract suspend fun deleteArticleFromFeed(feedId: Long)

    @Delete
    abstract suspend fun deleteCategories(categories: List<Category>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertFeeds(feeds: List<Feed>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun setFeedFavIcon(feedFavIcon: FeedFavIcon)

    @Delete
    internal abstract suspend fun deleteFeeds(toBeDelete: List<Feed>)

    @Query("DELETE FROM feed_fav_icon where _id=:feedId")
    internal abstract suspend fun deleteFeedFavIcon(feedId: Long)

    @androidx.room.Transaction
    open suspend fun deleteFeedsAndRelatedData(toBeDelete: List<Feed>) {
        for ((id) in toBeDelete) {
            deleteArticleFromFeed(id)
            deleteFeedFavIcon(id)
        }
        deleteFeeds(toBeDelete)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertArticles(dataArticles: List<Article>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertArticlesTags(articlesTags: List<ArticlesTags>)

    @Update(entity = Article::class)
    abstract suspend fun updateArticlesMetadata(metadata: List<Metadata>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAttachments(attachments: List<Attachment>)
}
