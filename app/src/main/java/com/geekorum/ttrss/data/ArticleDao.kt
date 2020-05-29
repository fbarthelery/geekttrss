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
package com.geekorum.ttrss.data

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Dao to read and modify articles.
 */
@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE _id=:id")
    fun getArticleById(id: Long): Flow<Article?>

    @Query("SELECT * FROM articles ORDER BY last_time_update DESC")
    fun getAllArticles(): DataSource.Factory<Int, Article>
    @Query("SELECT * FROM articles ORDER BY last_time_update")
    fun getAllArticlesOldestFirst(): DataSource.Factory<Int, Article>

    @Query("SELECT * FROM articles WHERE unread=1 ORDER BY last_time_update DESC")
    fun getAllUnreadArticles(): DataSource.Factory<Int, Article>
    @Query("SELECT * FROM articles WHERE unread=1 ORDER BY last_time_update")
    fun getAllUnreadArticlesOldestFirst(): DataSource.Factory<Int, Article>

    @Query("SELECT * FROM articles WHERE feed_id=:feedId ORDER BY last_time_update DESC ")
    fun getAllArticlesForFeed(feedId: Long): DataSource.Factory<Int, Article>
    @Query("SELECT * FROM articles WHERE feed_id=:feedId ORDER BY last_time_update")
    fun getAllArticlesForFeedOldestFirst(feedId: Long): DataSource.Factory<Int, Article>

    @Query("SELECT * FROM articles WHERE feed_id=:feedId AND unread=1 ORDER BY last_time_update DESC")
    fun getAllUnreadArticlesForFeed(feedId: Long): DataSource.Factory<Int, Article>
    @Query("SELECT * FROM articles WHERE feed_id=:feedId AND unread=1 ORDER BY last_time_update")
    fun getAllUnreadArticlesForFeedOldestFirst(feedId: Long): DataSource.Factory<Int, Article>

    @Query("SELECT articles.* FROM articles " +
        " JOIN articles_tags ON (articles_tags.article_id = articles._id)" +
        " WHERE articles_tags.tag=:tag ORDER BY last_time_update DESC")
    fun getAllArticlesForTag(tag: String): DataSource.Factory<Int, Article>
    @Query("SELECT articles.* FROM articles " +
        " JOIN articles_tags ON (articles_tags.article_id = articles._id)" +
        " WHERE articles_tags.tag=:tag ORDER BY last_time_update")
    fun getAllArticlesForTagOldestFirst(tag: String): DataSource.Factory<Int, Article>

    @Query("SELECT articles.* FROM articles " +
        " JOIN articles_tags ON (articles_tags.article_id = articles._id)" +
        " WHERE articles_tags.tag=:tag AND unread=1 ORDER BY last_time_update DESC")
    fun getAllUnreadArticlesForTag(tag: String): DataSource.Factory<Int, Article>
    @Query("SELECT articles.* FROM articles " +
        " JOIN articles_tags ON (articles_tags.article_id = articles._id)" +
        " WHERE articles_tags.tag=:tag AND unread=1 ORDER BY last_time_update")
    fun getAllUnreadArticlesForTagOldestFirst(tag: String): DataSource.Factory<Int, Article>

    @Query("SELECT * FROM articles WHERE marked=1 ORDER BY last_time_update DESC")
    fun getAllStarredArticles(): DataSource.Factory<Int, Article>
    @Query("SELECT * FROM articles WHERE marked=1 ORDER BY last_time_update")
    fun getAllStarredArticlesOldestFirst(): DataSource.Factory<Int, Article>

    @Query("SELECT * FROM articles WHERE marked=1 AND unread=1 ORDER BY last_time_update DESC")
    fun getAllUnreadStarredArticles(): DataSource.Factory<Int, Article>
    @Query("SELECT * FROM articles WHERE marked=1 AND unread=1 ORDER BY last_time_update")
    fun getAllUnreadStarredArticlesOldestFirst(): DataSource.Factory<Int, Article>

    @Query("SELECT * FROM articles WHERE published=1 ORDER BY last_time_update DESC")
    fun getAllPublishedArticles(): DataSource.Factory<Int, Article>
    @Query("SELECT * FROM articles WHERE published=1 ORDER BY last_time_update")
    fun getAllPublishedArticlesOldestFirst(): DataSource.Factory<Int, Article>

    @Query("SELECT * FROM articles WHERE published=1 AND unread=1 ORDER BY last_time_update DESC")
    fun getAllUnreadPublishedArticles(): DataSource.Factory<Int, Article>
    @Query("SELECT * FROM articles WHERE published=1 AND unread=1 ORDER BY last_time_update")
    fun getAllUnreadPublishedArticlesOldestFirst(): DataSource.Factory<Int, Article>

    @Query("SELECT * FROM articles WHERE last_time_update>=:time ORDER BY last_time_update DESC")
    fun getAllArticlesUpdatedAfterTime(time: Long): DataSource.Factory<Int, Article>
    @Query("SELECT * FROM articles WHERE last_time_update>=:time ORDER BY last_time_update")
    fun getAllArticlesUpdatedAfterTimeOldestFirst(time: Long): DataSource.Factory<Int, Article>

    @Query("SELECT * FROM articles WHERE last_time_update>=:time AND unread=1 ORDER BY last_time_update DESC")
    fun getAllUnreadArticlesUpdatedAfterTime(time: Long): DataSource.Factory<Int, Article>
    @Query("SELECT * FROM articles WHERE last_time_update>=:time AND unread=1 ORDER BY last_time_update")
    fun getAllUnreadArticlesUpdatedAfterTimeOldestFirst(time: Long): DataSource.Factory<Int, Article>

    @Query("UPDATE articles SET transiant_unread=:isUnread WHERE _id=:articleId")
    suspend fun updateArticleTransientUnread(articleId: Long, isUnread: Boolean)

    @Query("UPDATE articles SET transiant_unread=:isUnread, unread=:isUnread WHERE _id=:articleId")
    suspend fun updateArticleUnread(articleId: Long, isUnread: Boolean): Int

    @Query("UPDATE articles SET marked=:isMarked WHERE _id=:articleId")
    suspend fun updateArticleMarked(articleId: Long, isMarked: Boolean): Int

    @Query("SELECT articles.* FROM ArticleFTS JOIN articles ON (ArticleFTS.rowid = _id) "
        + "WHERE ArticleFTS MATCH :query "
        + "ORDER BY last_time_update DESC")
    fun searchArticles(query: String?): DataSource.Factory<Int, Article>

    @Query("SELECT tag  FROM articles_tags " +
        " JOIN articles ON (articles_tags.article_id = articles._id)" +
        " WHERE articles.unread=1" +
        " GROUP BY tag" +
        " ORDER BY COUNT(article_id) DESC" +
        " LIMIT :count ")
    suspend fun getMostUnreadTags(count: Int): List<String>

}
