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

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.geekorum.ttrss.Application;
import com.geekorum.ttrss.data.ArticlesDatabase;
import com.geekorum.ttrss.di.AndroidComponentsModule;
import com.geekorum.ttrss.di.ApplicationComponent;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Manage tt-rss content data, mostly articles
 */
public class ArticlesProvider extends ContentProvider {

    private static final int MATCH_ROOT = 0;
    private static final UriMatcher uriMatcher = new UriMatcher(MATCH_ROOT);
    private static final int MATCH_ARTICLES_ID = 1;
    private static final int MATCH_ARTICLES = 2;
    private static final int MATCH_TRANSACTIONS_ID = 3;
    private static final int MATCH_TRANSACTIONS = 4;
    private static final int MATCH_FEEDS_ID = 5;
    private static final int MATCH_FEEDS = 6;
    private static final int MATCH_CATEGORIES_ID = 7;
    private static final int MATCH_CATEGORIES = 8;

    @Inject ArticlesDatabase articlesDatabase;
    @Inject ContentResolver contentResolver;
    @Inject SupportSQLiteOpenHelper dbHelper;

    static {
        uriMatcher.addURI(ArticlesContract.AUTHORITY, "articles/#", MATCH_ARTICLES_ID);
        uriMatcher.addURI(ArticlesContract.AUTHORITY, "articles", MATCH_ARTICLES);
        uriMatcher.addURI(ArticlesContract.AUTHORITY, "transactions/#", MATCH_TRANSACTIONS_ID);
        uriMatcher.addURI(ArticlesContract.AUTHORITY, "transactions", MATCH_TRANSACTIONS);
        uriMatcher.addURI(ArticlesContract.AUTHORITY, "feeds/#", MATCH_FEEDS_ID);
        uriMatcher.addURI(ArticlesContract.AUTHORITY, "feeds", MATCH_FEEDS);
        uriMatcher.addURI(ArticlesContract.AUTHORITY, "categories/#", MATCH_CATEGORIES_ID);
        uriMatcher.addURI(ArticlesContract.AUTHORITY, "categories", MATCH_CATEGORIES);
    }

    @Override
    public boolean onCreate() {
        createArticleProviderComponent()
                .inject(this);
        return true;
    }

    private ApplicationComponent getApplicationComponent() {
        return ((Application) getContext().getApplicationContext()).getApplicationComponent();
    }

    private ArticleProviderComponent createArticleProviderComponent() {
        return getApplicationComponent().createArticleProviderComponent()
                .androidComponentsModule(new AndroidComponentsModule(getContext()))
                .build();
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor result;
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case MATCH_ARTICLES_ID:
                queryBuilder.appendWhere(ArticlesContract.Article._ID + "=" + ContentUris.parseId(uri));
            case MATCH_ARTICLES:
                queryBuilder.setTables(DbHelper.TABLE_ARTICLES);
                result = doQuery(queryBuilder, dbHelper.getReadableDatabase(), projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case MATCH_TRANSACTIONS_ID:
                queryBuilder.appendWhere(ArticlesContract.Article._ID + "=" + ContentUris.parseId(uri));
            case MATCH_TRANSACTIONS:
                queryBuilder.setTables(DbHelper.TABLE_TRANSACTIONS);
                result = doQuery(queryBuilder, dbHelper.getReadableDatabase(), projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case MATCH_FEEDS_ID:
                queryBuilder.appendWhere(ArticlesContract.Feed._ID + "=" + ContentUris.parseId(uri));
            case MATCH_FEEDS:
                queryBuilder.setTables(DbHelper.TABLE_FEEDS);
                result = doQuery(queryBuilder, dbHelper.getReadableDatabase(), projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case MATCH_CATEGORIES_ID:
                queryBuilder.appendWhere(ArticlesContract.Category._ID + "=" + ContentUris.parseId(uri));
            case MATCH_CATEGORIES:
                queryBuilder.setTables(DbHelper.TABLE_CATEGORIES);
                result = doQuery(queryBuilder, dbHelper.getReadableDatabase(), projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unable to query data from " + uri);

        }
        if (result != null) {
            result.setNotificationUri(contentResolver, uri);
            return result;
        }
        return null;
    }

    private Cursor doQuery(SQLiteQueryBuilder sqLiteQueryBuilder, SupportSQLiteDatabase readableDatabase,
                           String[] projection, String selection, String[] selectionArgs,
                           String groupBy, String having, String sortOrder) {
        String query = sqLiteQueryBuilder.buildQuery(projection, selection, groupBy, having, sortOrder, null);
        return readableDatabase.query(query, selectionArgs);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case MATCH_ARTICLES:
                return ArticlesContract.Article.CONTENT_TYPE;
            case MATCH_ARTICLES_ID:
                return ArticlesContract.Article.CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        long id;
        SupportSQLiteDatabase database = articlesDatabase.getOpenHelper().getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case MATCH_ARTICLES:
                id = database.insert(DbHelper.TABLE_ARTICLES, SQLiteDatabase.CONFLICT_REPLACE, contentValues);
                break;
            case MATCH_TRANSACTIONS:
                id = database.insert(DbHelper.TABLE_TRANSACTIONS, SQLiteDatabase.CONFLICT_NONE, contentValues);
                break;
            case MATCH_FEEDS:
                id = database.insert(DbHelper.TABLE_FEEDS, SQLiteDatabase.CONFLICT_REPLACE, contentValues);
                break;
            case MATCH_CATEGORIES:
                id = database.insert(DbHelper.TABLE_CATEGORIES, SQLiteDatabase.CONFLICT_REPLACE, contentValues);
                break;
            default:
                throw new UnsupportedOperationException("Unable to insert data into " + uri);
        }
        if (id >= 0) {
            notifyChangesIfNotInBatch(uri);
            refreshInvalidationTrackerIfNotInBatch();
            return uri;
        }
        return null;
    }

    private void notifyChangesIfNotInBatch(Uri uri) {
        if (!articlesDatabase.inTransaction()) {
            contentResolver.notifyChange(uri, null, false);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int deleted;
        SupportSQLiteDatabase database = articlesDatabase.getOpenHelper().getWritableDatabase();
        String table;
        switch (uriMatcher.match(uri)) {
            case MATCH_ARTICLES_ID:
                selection = ArticlesContract.Article._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            case MATCH_ARTICLES:
                table = DbHelper.TABLE_ARTICLES;
                break;
            case MATCH_TRANSACTIONS_ID:
                selection = ArticlesContract.Transaction._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            case MATCH_TRANSACTIONS:
                table = DbHelper.TABLE_TRANSACTIONS;
                break;
            case MATCH_FEEDS_ID:
                selection = ArticlesContract.Feed._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            case MATCH_FEEDS:
                table = DbHelper.TABLE_FEEDS;
                break;
            case MATCH_CATEGORIES_ID:
                selection = ArticlesContract.Category._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            case MATCH_CATEGORIES:
                table = DbHelper.TABLE_CATEGORIES;
                break;
            default:
                throw new UnsupportedOperationException("Unable to delete data into " + uri);
        }
        deleted = database.delete(table, selection, selectionArgs);
        if (deleted > 0) {
            notifyChangesIfNotInBatch(uri);
            refreshInvalidationTrackerIfNotInBatch();
        }
        return deleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int updated;
        SupportSQLiteDatabase database = articlesDatabase.getOpenHelper().getWritableDatabase();
        String table;
        switch (uriMatcher.match(uri)) {
            case MATCH_ARTICLES_ID:
                selection = ArticlesContract.Article._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            case MATCH_ARTICLES:
                table = DbHelper.TABLE_ARTICLES;
                break;
            case MATCH_TRANSACTIONS_ID:
                selection = ArticlesContract.Transaction._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            case MATCH_TRANSACTIONS:
                table = DbHelper.TABLE_TRANSACTIONS;
                break;
            case MATCH_FEEDS_ID:
                selection = ArticlesContract.Feed._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            case MATCH_FEEDS:
                table = DbHelper.TABLE_FEEDS;
                break;
            case MATCH_CATEGORIES_ID:
                selection = ArticlesContract.Category._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            case MATCH_CATEGORIES:
                table = DbHelper.TABLE_FEEDS;
                break;
            default:
                throw new UnsupportedOperationException("Unable to update data into " + uri);
        }
        updated = database.update(table, SQLiteDatabase.CONFLICT_NONE, contentValues, selection, selectionArgs);
        if (updated > 0) {
            notifyChangesIfNotInBatch(uri);
            refreshInvalidationTrackerIfNotInBatch();
        }
        return updated;
    }

    private void refreshInvalidationTrackerIfNotInBatch() {
        if (!articlesDatabase.inTransaction()) {
            articlesDatabase.getInvalidationTracker().refreshVersionsAsync();
        }
    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        try {
            articlesDatabase.beginTransaction();
            ContentProviderResult[] contentProviderResults = super.applyBatch(operations);
            articlesDatabase.setTransactionSuccessful();
            // notify changes to all uri
            contentResolver.notifyChange(ArticlesContract.AUTHORITY_URI, null);
            return contentProviderResults;
        } finally {
            articlesDatabase.endTransaction();
        }
    }
}
