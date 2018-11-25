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
package com.geekorum.ttrss.room_migration;

import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import com.geekorum.ttrss.data.ArticlesDatabase;
import com.geekorum.ttrss.data.Article;
import com.geekorum.ttrss.data.Category;
import com.geekorum.ttrss.data.Feed;
import com.geekorum.ttrss.data.Transaction;
import com.geekorum.ttrss.providers.ArticlesContract;
import com.geekorum.ttrss.providers.DbHelper;

import java.util.Arrays;

import javax.inject.Inject;

/**
 * Quick and dirty process to migrate all database data into a Room database.
 */
public class RoomMigration {

    private static final String TAG = RoomMigration.class.getSimpleName();
    private final ProgressListener progressListener;
    private final CompletionListener completionListener;
    private final DbHelper dbHelper;
    private final ArticlesDatabase articlesDatabase;
    private final RoomMigrationDao roomMigrationDao;
    private final TableMigration[] tableMigrations = new TableMigration[]{
            new CategoryTableMigration(),
            new FeedTableMigration(),
            new ArticleTableMigration(),
            new TransactionTableMigration()
    };
    private int totalRows;
    private int inserted;

    private RoomMigration(DbHelper dbHelper, ArticlesDatabase articlesDatabase, ProgressListener progressListener, CompletionListener completionListener) {
        this.dbHelper = dbHelper;
        this.articlesDatabase = articlesDatabase;
        this.progressListener = progressListener;
        this.completionListener = completionListener;
        roomMigrationDao = articlesDatabase.roomMigrationDao();
    }

    public void execute() {
        Log.i(TAG, "Start migration");
        totalRows = getTotalRows();
        articlesDatabase.beginTransaction();
        boolean success = false;
        try {
            Arrays.stream(tableMigrations).forEach(TableMigration::execute);
            dbHelper.selfDelete();
            articlesDatabase.setTransactionSuccessful();
            success = true;
            Log.i(TAG, "End of migration");
        } catch (Exception e) {
            Log.e(TAG, "unable to complete migration", e);
            success = false;
        } finally {
            articlesDatabase.endTransaction();
            completionListener.onComplete(success);
        }
    }

    private int getTotalRows() {
        return Arrays.stream(tableMigrations)
                .map(TableMigration::getCount)
                .reduce(Integer::sum).orElse(0);
    }

    public static class Factory {
        private ProgressListener progressListener;
        private CompletionListener completionListener;
        private final DbHelper dbHelper;
        private final ArticlesDatabase articlesDatabase;

        @Inject
        public Factory(DbHelper dbHelper, ArticlesDatabase articlesDatabase) {
            this.dbHelper = dbHelper;
            this.articlesDatabase = articlesDatabase;
        }

        Factory setProgressListener(ProgressListener progressListener) {
            this.progressListener = progressListener;
            return this;
        }

        Factory setCompletionListener(CompletionListener completionListener) {
            this.completionListener = completionListener;
            return this;
        }

        public RoomMigration build() {
            return new RoomMigration(dbHelper, articlesDatabase, progressListener, completionListener);
        }
    }

    /** Migrate a table into a Room table of Entity */
    private abstract class TableMigration<Entity> {

        private final String tableName;

        TableMigration(String tableName) {
            this.tableName = tableName;
        }

        void execute() {
            try (Cursor cursor = getOldData(tableName)) {
                while (cursor.moveToNext()) {
                    Entity item = mapData(cursor);
                    insertItem(item);
                    inserted++;
                    progressListener.onProgress(inserted, totalRows);
                }
            }
        }

        int getCount() {
            try (Cursor cursor = getOldData(tableName)) {
                return cursor.getCount();
            }
        }

        private Cursor getOldData(String tableName) {
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
            queryBuilder.setTables(tableName);
            return queryBuilder.query(dbHelper.getReadableDatabase(), null,
                    null, null, null, null, null);
        }

        abstract Entity mapData(Cursor cursor);

        abstract void insertItem(Entity item);
    }

    private class CategoryTableMigration extends TableMigration<Category> {
        CategoryTableMigration() {
            super(DbHelper.TABLE_CATEGORIES);
        }

        @Override
        Category mapData(Cursor cursor) {
            Category category = new Category();
            category.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ArticlesContract.Category._ID)));
            category.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(ArticlesContract.Category.TITLE)));
            category.setUnreadCount(cursor.getInt(cursor.getColumnIndexOrThrow(ArticlesContract.Category.UNREAD_COUNT)));
            return category;
        }

        @Override
        void insertItem(Category item) {
            roomMigrationDao.insertCategories(item);
        }

    }

    private class FeedTableMigration extends TableMigration<Feed> {
        FeedTableMigration() {
            super(DbHelper.TABLE_FEEDS);
        }

        @Override
        Feed mapData(Cursor cursor) {
            Feed feed = new Feed();
            feed.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ArticlesContract.Feed._ID)));
            feed.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(ArticlesContract.Feed.TITLE)));
            feed.setUnreadCount(cursor.getInt(cursor.getColumnIndexOrThrow(ArticlesContract.Category.UNREAD_COUNT)));
            feed.setDisplayTitle(cursor.getString(cursor.getColumnIndexOrThrow(ArticlesContract.Category.TITLE)));
            feed.setCatId(cursor.getLong(cursor.getColumnIndexOrThrow(ArticlesContract.Feed.CAT_ID)));
            feed.setLastTimeUpdate(cursor.getLong(cursor.getColumnIndexOrThrow(ArticlesContract.Feed.LAST_TIME_UPDATE)));
            feed.setUnreadCount(cursor.getInt(cursor.getColumnIndexOrThrow(ArticlesContract.Feed.UNREAD_COUNT)));
            feed.setUrl(cursor.getString(cursor.getColumnIndexOrThrow(ArticlesContract.Feed.URL)));
            return feed;
        }

        @Override
        void insertItem(Feed item) {
            roomMigrationDao.insertFeeds(item);
        }
    }

    private class ArticleTableMigration extends TableMigration<Article> {

        private ArticleTableMigration() {
            super(DbHelper.TABLE_ARTICLES);
        }

        @Override
        Article mapData(Cursor cursor) {
            Article article = new Article();
            article.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ArticlesContract.Article._ID)));
            article.setAuthor(cursor.getString(cursor.getColumnIndexOrThrow(ArticlesContract.Article.AUTHOR)));
            article.setContent(cursor.getString(cursor.getColumnIndexOrThrow(ArticlesContract.Article.CONTENT)));
            article.setContentExcerpt(cursor.getString(cursor.getColumnIndexOrThrow(ArticlesContract.Article.CONTENT_EXCERPT)));
            article.setFeedId(cursor.getLong(cursor.getColumnIndexOrThrow(ArticlesContract.Article.FEED_ID)));
            article.setFlavorImageUri(cursor.getString(cursor.getColumnIndexOrThrow(ArticlesContract.Article.FLAVOR_IMAGE_URI)));
            article.setLastTimeUpdate(cursor.getLong(cursor.getColumnIndexOrThrow(ArticlesContract.Article.LAST_TIME_UPDATE)));
            article.setLink(cursor.getString(cursor.getColumnIndexOrThrow(ArticlesContract.Article.LINK)));
            article.setScore(cursor.getInt(cursor.getColumnIndexOrThrow(ArticlesContract.Article.SCORE)));
            article.setTags(cursor.getString(cursor.getColumnIndexOrThrow(ArticlesContract.Article.TAGS)));
            article.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(ArticlesContract.Article.TITLE)));
            article.setPublished(cursor.getInt(cursor.getColumnIndexOrThrow(ArticlesContract.Article.PUBLISHED)) != 0);
            article.setStarred(cursor.getInt(cursor.getColumnIndexOrThrow(ArticlesContract.Article.STARRED)) != 0);
            article.setTransientUnread(
                    cursor.getInt(cursor.getColumnIndexOrThrow(ArticlesContract.Article.TRANSIENT_UNREAD)) != 0);
            article.setUnread(cursor.getInt(cursor.getColumnIndexOrThrow(ArticlesContract.Article.UNREAD)) != 0);
            article.setUpdated(cursor.getInt(cursor.getColumnIndexOrThrow(ArticlesContract.Article.IS_UPDATED)) != 0);
            return article;
        }

        @Override
        void insertItem(Article item) {
            roomMigrationDao.insertArticles(item);
        }
    }

    private class TransactionTableMigration extends TableMigration<Transaction> {
        TransactionTableMigration() {
            super(DbHelper.TABLE_TRANSACTIONS);
        }

        @Override
        Transaction mapData(Cursor cursor) {
            Transaction transaction = new Transaction();
            transaction.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ArticlesContract.Transaction._ID)));
            transaction.setField(cursor.getString(cursor.getColumnIndexOrThrow(ArticlesContract.Transaction.FIELD)));
            transaction.setValue(cursor.getInt(cursor.getColumnIndexOrThrow(ArticlesContract.Transaction.VALUE)) != 0);
            transaction.setArticleId(cursor.getLong(cursor.getColumnIndexOrThrow(ArticlesContract.Transaction.ARTICLE_ID)));
            return transaction;
        }

        @Override
        void insertItem(Transaction item) {
            roomMigrationDao.insertTranscations(item);
        }
    }

    interface ProgressListener {
        void onProgress(int progress, int max);
    }

    interface CompletionListener {
        void onComplete(boolean success);
    }
}
