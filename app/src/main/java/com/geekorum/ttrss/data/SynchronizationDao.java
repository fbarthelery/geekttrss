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
package com.geekorum.ttrss.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Dao to access the database when synchronizing articles
 */
@Dao
public abstract class SynchronizationDao {
    @Query("SELECT * FROM transactions")
    public abstract List<Transaction> getAllTransactions();

    @Delete
    public abstract void deleteTransaction(Transaction transaction);

    @Query("SELECT * FROM articles WHERE _id=:id")
    public abstract Article getArticleById(long id);

    @Query("SELECT _id FROM articles ORDER BY _id DESC LIMIT 1")
    public abstract long getLatestArticleId();

    @Update
    public abstract void updateArticle(Article article);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertCategories(List<Category> categories);

    @Query("DELETE FROM ARTICLES where feed_id=:feedId")
    abstract void deleteArticleFromFeed(long feedId);

    @Query("SELECT * FROM categories")
    public abstract List<Category> getAllCategories();

    @Delete
    public abstract void deleteCategories(List<Category> categories);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertFeeds(List<Feed> feeds);

    @Query("SELECT * FROM feeds")
    public abstract List<Feed> getAllFeeds();

    @Delete
    abstract void deleteFeeds(List<Feed> toBeDelete);

    @androidx.room.Transaction
    public void deleteFeedsAndArticles(List<Feed> toBeDelete) {
        for (Feed feed : toBeDelete) {
            deleteArticleFromFeed(feed.getId());
        }
        deleteFeeds(toBeDelete);
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertArticles(List<Article> dataArticles);

    @Query("UPDATE articles SET unread=:unread, transiant_unread=:transientUnread, marked=:starred, published=:published,"
            + " last_time_update=:lastTimeUpdated, is_updated=:isUpdated"
            + " WHERE _id=:id")
    public abstract void updateArticleMetadata(long id, boolean unread, boolean transientUnread, boolean starred,
                                        boolean published, long lastTimeUpdated, boolean isUpdated);
}
