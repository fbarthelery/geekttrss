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
package com.geekorum.ttrss.data;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Query;

/**
 * Dao to read and modify articles.
 */
@Dao
public interface ArticleDao {
    @Query("SELECT * FROM articles WHERE _id=:id ORDER BY last_time_update DESC")
    LiveData<Article> getArticleById(long id);

    @Query("SELECT * FROM articles ORDER BY last_time_update DESC")
    DataSource.Factory<Integer, Article> getAllArticles();

    @Query("SELECT * FROM articles WHERE unread=1 ORDER BY last_time_update DESC")
    DataSource.Factory<Integer, Article> getAllUnreadArticles();

    @Query("SELECT * FROM articles WHERE feed_id=:feedId ORDER BY last_time_update DESC ")
    DataSource.Factory<Integer, Article> getAllArticlesForFeed(long feedId);

    @Query("SELECT * FROM articles WHERE feed_id=:feedId AND unread=1 ORDER BY last_time_update DESC")
    DataSource.Factory<Integer, Article> getAllUnreadArticlesForFeed(long feedId);

    @Query("SELECT * FROM articles WHERE marked=1 ORDER BY last_time_update DESC")
    DataSource.Factory<Integer, Article> getAllStarredArticles();

    @Query("SELECT * FROM articles WHERE marked=1 AND unread=1 ORDER BY last_time_update DESC")
    DataSource.Factory<Integer, Article> getAllUnreadStarredArticles();

    @Query("SELECT * FROM articles WHERE published=1 ORDER BY last_time_update DESC")
    DataSource.Factory<Integer, Article> getAllPublishedArticles();

    @Query("SELECT * FROM articles WHERE published=1 AND unread=1 ORDER BY last_time_update DESC")
    DataSource.Factory<Integer, Article> getAllUnreadPublishedArticles();

    @Query("SELECT * FROM articles WHERE last_time_update>=:time ORDER BY last_time_update DESC")
    DataSource.Factory<Integer, Article> getAllArticlesUpdatedAfterTime(long time);

    @Query("SELECT * FROM articles WHERE last_time_update>=:time AND unread=1 ORDER BY last_time_update DESC")
    DataSource.Factory<Integer, Article> getAllUnreadArticlesUpdatedAfterTime(long time);

    @Query("UPDATE articles SET transiant_unread=:isUnread WHERE _id=:articleId")
    void updateArticleTransientUnread(long articleId, boolean isUnread);

    @Query("UPDATE articles SET transiant_unread=:isUnread, unread=:isUnread WHERE _id=:articleId")
    int updateArticleUnread(long articleId, boolean isUnread);

    @Query("UPDATE articles SET marked=:isMarked WHERE _id=:articleId")
    int updateArticleMarked(long articleId, boolean isMarked);

    @Query("SELECT articles.* FROM ArticleFTS JOIN articles ON (ArticleFTS.rowid = _id) "
            + "WHERE ArticleFTS MATCH :query "
            + "ORDER BY last_time_update DESC")
    DataSource.Factory<Integer, Article> searchArticles(String query);
}
