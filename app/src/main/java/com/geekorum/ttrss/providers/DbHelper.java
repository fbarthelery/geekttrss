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
package com.geekorum.ttrss.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import com.geekorum.ttrss.providers.ArticlesContract.Article;
import com.geekorum.ttrss.providers.ArticlesContract.Category;
import com.geekorum.ttrss.providers.ArticlesContract.Feed;
import com.geekorum.ttrss.providers.ArticlesContract.Transaction;

/**
 * Manage the TT-rss database.
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "articles.db";
    private static final int DATABASE_VERSION = 5;
    public static final String TABLE_ARTICLES = "articles";
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String TABLE_FEEDS = "feeds";
    public static final String TABLE_CATEGORIES = "categories";
    private final Context context;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ARTICLES + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Article.UNREAD + " BOOLEAN, " +
                Article.TRANSIENT_UNREAD + " BOOLEAN, " +
                Article.STARRED + " BOOLEAN, " +
                Article.PUBLISHED + " BOOLEAN, " +
                Article.SCORE + " INTEGER, " +
                Article.LAST_TIME_UPDATE + " INTEGER, " +
                Article.IS_UPDATED + " BOOLEAN, " +
                Article.TITLE + " TEXT, " +
                Article.LINK + " TEXT, " +
                Article.FEED_ID + " INTEGER, " +
                Article.TAGS + " TEXT, " +
                Article.CONTENT + " TEXT, " +
                Article.AUTHOR + " TEXT " +
//                "selected BOOLEAN, " +
//                "modified BOOLEAN" +
                ");");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TRANSACTIONS + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Transaction.ARTICLE_ID + " INTEGER, " +
                Transaction.FIELD + " TEXT, " +
                Transaction.VALUE + " BOOLEAN" +
                ");");
        addFeedTable(sqLiteDatabase);
        addCategoryTable(sqLiteDatabase);
        addFlavorImageColumn(sqLiteDatabase);
        addContentExcerptColumn(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            addFeedTable(sqLiteDatabase);
            oldVersion = 2;
        }
        if (oldVersion == 2) {
            addCategoryTable(sqLiteDatabase);
            oldVersion = 3;
        }
        if (oldVersion == 3) {
            addFlavorImageColumn(sqLiteDatabase);
            oldVersion = 4;
        }
        if (oldVersion == 4) {
            addContentExcerptColumn(sqLiteDatabase);
            oldVersion = 5;
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // do nothing
    }

    private void addFeedTable(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FEEDS + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE," +
                Feed.URL + " TEXT, " +
                Feed.TITLE + " TEXT, " +
                Feed.CAT_ID + " INTEGER, " +
                Feed.DISPLAY_TITLE + " TEXT, " +
                Feed.LAST_TIME_UPDATE + " INTEGER, " +
                Feed.UNREAD_COUNT + " INTEGER" +
                ");");
    }

    private void addCategoryTable(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE," +
                Category.TITLE + " TEXT, " +
                Category.UNREAD_COUNT + " INTEGER" +
                ");");
    }

    private void addFlavorImageColumn(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_ARTICLES +
                " ADD COLUMN " +
                Article.FLAVOR_IMAGE_URI + " TEXT; ");
    }

    private void addContentExcerptColumn(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_ARTICLES +
                " ADD COLUMN " +
                Article.CONTENT_EXCERPT + " TEXT; ");
    }

    public void selfDelete() {
        close();
        context.deleteDatabase(DATABASE_NAME);
    }

}
