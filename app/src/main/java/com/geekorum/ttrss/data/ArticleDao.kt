/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2023 by Frederic-Charles Barthelery.
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

/**
 * Dao to read and modify articles.
 */
@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE _id=:id")
    fun getArticleById(id: Long): Flow<Article?>

    @Query("SELECT * FROM articles WHERE _id IN (:articleIds)")
    @Transaction
    fun getArticlesById(articleIds: List<Long>): PagingSource<Int, ArticleWithFeed>

    @Query("SELECT * FROM articles ORDER BY last_time_update DESC")
    @Transaction
    fun getAllArticles(): PagingSource<Int, ArticleWithFeed>
    @Transaction
    @Query("SELECT * FROM articles ORDER BY last_time_update")
    fun getAllArticlesOldestFirst(): PagingSource<Int, ArticleWithFeed>

    @Query("SELECT * FROM articles WHERE unread=1 ORDER BY last_time_update DESC")
    @Transaction
    fun getAllUnreadArticles(): PagingSource<Int, ArticleWithFeed>
    @Query("SELECT * FROM articles WHERE unread=1 ORDER BY last_time_update")
    @Transaction
    fun getAllUnreadArticlesOldestFirst(): PagingSource<Int, ArticleWithFeed>

    @Query("SELECT * FROM articles WHERE unread=1 ORDER BY RANDOM() LIMIT :count")
    @Transaction
    suspend fun getUnreadArticlesRandomized(count: Int): List<ArticleWithFeed>

    @Query("SELECT * FROM articles WHERE feed_id=:feedId ORDER BY last_time_update DESC ")
    @Transaction
    fun getAllArticlesForFeed(feedId: Long): PagingSource<Int, ArticleWithFeed>
    @Query("SELECT * FROM articles WHERE feed_id=:feedId ORDER BY last_time_update")
    @Transaction
    fun getAllArticlesForFeedOldestFirst(feedId: Long): PagingSource<Int, ArticleWithFeed>

    @Query("SELECT * FROM articles WHERE feed_id=:feedId AND unread=1 ORDER BY last_time_update DESC")
    @Transaction
    fun getAllUnreadArticlesForFeed(feedId: Long): PagingSource<Int, ArticleWithFeed>
    @Query("SELECT * FROM articles WHERE feed_id=:feedId AND unread=1 ORDER BY last_time_update")
    @Transaction
    fun getAllUnreadArticlesForFeedOldestFirst(feedId: Long): PagingSource<Int, ArticleWithFeed>

    @Query("SELECT * FROM articles WHERE last_time_update>=:time AND unread=1 AND feed_id=:feedId " +
        "ORDER BY RANDOM()")
    @Transaction
    suspend fun getAllUnreadArticlesForFeedUpdatedAfterTimeRandomized(feedId: Long, time: Long): List<Article>

    @Query("SELECT articles.* FROM articles " +
        " JOIN articles_tags ON (articles_tags.article_id = articles._id)" +
        " WHERE articles_tags.tag=:tag ORDER BY last_time_update DESC")
    @Transaction
    fun getAllArticlesForTag(tag: String): PagingSource<Int, ArticleWithFeed>
    @Query("SELECT articles.* FROM articles " +
        " JOIN articles_tags ON (articles_tags.article_id = articles._id)" +
        " WHERE articles_tags.tag=:tag ORDER BY last_time_update")
    @Transaction
    fun getAllArticlesForTagOldestFirst(tag: String): PagingSource<Int, ArticleWithFeed>

    @Query("SELECT articles.* FROM articles " +
        " JOIN articles_tags ON (articles_tags.article_id = articles._id)" +
        " WHERE articles_tags.tag=:tag AND unread=1 ORDER BY last_time_update DESC")
    @Transaction
    fun getAllUnreadArticlesForTag(tag: String): PagingSource<Int, ArticleWithFeed>
    @Query("SELECT articles.* FROM articles " +
        " JOIN articles_tags ON (articles_tags.article_id = articles._id)" +
        " WHERE articles_tags.tag=:tag AND unread=1 ORDER BY last_time_update")
    @Transaction
    fun getAllUnreadArticlesForTagOldestFirst(tag: String): PagingSource<Int, ArticleWithFeed>

    @Query("SELECT * FROM articles WHERE marked=1 ORDER BY last_time_update DESC")
    @Transaction
    fun getAllStarredArticles(): PagingSource<Int, ArticleWithFeed>
    @Query("SELECT * FROM articles WHERE marked=1 ORDER BY last_time_update")
    @Transaction
    fun getAllStarredArticlesOldestFirst(): PagingSource<Int, ArticleWithFeed>

    @Query("SELECT * FROM articles WHERE marked=1 AND unread=1 ORDER BY last_time_update DESC")
    @Transaction
    fun getAllUnreadStarredArticles(): PagingSource<Int, ArticleWithFeed>
    @Query("SELECT * FROM articles WHERE marked=1 AND unread=1 ORDER BY last_time_update")
    @Transaction
    fun getAllUnreadStarredArticlesOldestFirst(): PagingSource<Int, ArticleWithFeed>

    @Query("SELECT * FROM articles WHERE published=1 ORDER BY last_time_update DESC")
    @Transaction
    fun getAllPublishedArticles(): PagingSource<Int, ArticleWithFeed>
    @Query("SELECT * FROM articles WHERE published=1 ORDER BY last_time_update")
    @Transaction
    fun getAllPublishedArticlesOldestFirst(): PagingSource<Int, ArticleWithFeed>

    @Query("SELECT * FROM articles WHERE published=1 AND unread=1 ORDER BY last_time_update DESC")
    @Transaction
    fun getAllUnreadPublishedArticles(): PagingSource<Int, ArticleWithFeed>
    @Query("SELECT * FROM articles WHERE published=1 AND unread=1 ORDER BY last_time_update")
    @Transaction
    fun getAllUnreadPublishedArticlesOldestFirst(): PagingSource<Int, ArticleWithFeed>

    @Query("SELECT * FROM articles WHERE last_time_update>=:time ORDER BY last_time_update DESC")
    @Transaction
    fun getAllArticlesUpdatedAfterTime(time: Long): PagingSource<Int, ArticleWithFeed>
    @Query("SELECT * FROM articles WHERE last_time_update>=:time ORDER BY last_time_update")
    @Transaction
    fun getAllArticlesUpdatedAfterTimeOldestFirst(time: Long): PagingSource<Int, ArticleWithFeed>

    @Query("SELECT * FROM articles WHERE last_time_update>=:time AND unread=1 ORDER BY last_time_update DESC")
    @Transaction
    fun getAllUnreadArticlesUpdatedAfterTime(time: Long): PagingSource<Int, ArticleWithFeed>
    @Query("SELECT * FROM articles WHERE last_time_update>=:time AND unread=1 ORDER BY last_time_update")
    @Transaction
    fun getAllUnreadArticlesUpdatedAfterTimeOldestFirst(time: Long): PagingSource<Int, ArticleWithFeed>

    @Query("UPDATE articles SET transiant_unread=:isUnread WHERE _id=:articleId")
    suspend fun updateArticleTransientUnread(articleId: Long, isUnread: Boolean)

    @Query("UPDATE articles SET transiant_unread=:isUnread, unread=:isUnread WHERE _id=:articleId")
    suspend fun updateArticleUnread(articleId: Long, isUnread: Boolean): Int

    @Query("UPDATE articles SET marked=:isMarked WHERE _id=:articleId")
    suspend fun updateArticleMarked(articleId: Long, isMarked: Boolean): Int

    @Query("SELECT articles.* FROM ArticleFTS JOIN articles ON (ArticleFTS.rowid = _id) "
        + "WHERE ArticleFTS MATCH :query "
        + "ORDER BY last_time_update DESC")
    @Transaction
    fun searchArticles(query: String?): PagingSource<Int, ArticleWithFeed>

    @Query("SELECT tag  FROM articles_tags " +
        " JOIN articles ON (articles_tags.article_id = articles._id)" +
        " WHERE articles.unread=1" +
        " GROUP BY tag" +
        " ORDER BY COUNT(article_id) DESC" +
        " LIMIT :count ")
    suspend fun getMostUnreadTags(count: Int): List<String>

    @Query("SELECT articles.* FROM articles " +
        " JOIN articles_tags ON (articles_tags.article_id = articles._id)" +
        " WHERE articles_tags.tag=:tag AND unread=1 ORDER BY last_time_update DESC LIMIT :count"
    )
    @Transaction
    suspend fun getUnreadArticlesForTag(tag: String, count: Int): List<ArticleWithFeed>

}
