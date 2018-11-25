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
package com.geekorum.ttrss.data.migrations;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.room.migration.Migration;
import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.geekorum.ttrss.data.ArticlesDatabase;
import com.geekorum.ttrss.providers.ArticlesContract;
import com.geekorum.ttrss.providers.DbHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

/**
 * Testsuite for {@link ArticlesDatabase} migrations.
 */
@RunWith(AndroidJUnit4.class)
public class ArticlesDatabaseMigrationTest {

    private static final String TEST_DB = "migration-test";
    private static final Migration MIGRATION_1_2 = new ArticlesDatabaseFrom1To2();
    private static final Migration MIGRATION_2_3 = new ArticlesDatabaseFrom2To3();

    @Rule
    public MigrationTestHelper helper;

    public ArticlesDatabaseMigrationTest() {
        helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                ArticlesDatabase.class.getCanonicalName(),
                new FrameworkSQLiteOpenHelperFactory());
    }

    @Test
    public void migrate1To2() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 1);

        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        createSomeArticles(db);

        // Prepare for the next version.
        db.close();

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2);

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
        assertMigration1To2DataIntegrity(db);
    }

    private void createSomeArticles(SupportSQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(ArticlesContract.Category.TITLE, "category");
        values.put(ArticlesContract.Category.UNREAD_COUNT, 2);
        db.insert(DbHelper.TABLE_CATEGORIES, SQLiteDatabase.CONFLICT_NONE, values);

        values.clear();
        values.put(ArticlesContract.Feed.TITLE, "feed title");
        values.put(ArticlesContract.Feed.URL, "feed url");
        values.put(ArticlesContract.Feed.CAT_ID, 0);
        values.put(ArticlesContract.Feed.UNREAD_COUNT, 2);
        values.put(ArticlesContract.Feed.LAST_TIME_UPDATE, 0);
        values.put(ArticlesContract.Feed.DISPLAY_TITLE, "display title");
        db.insert(DbHelper.TABLE_FEEDS, SQLiteDatabase.CONFLICT_NONE, values);


        values.clear();
        values.put(ArticlesContract.Article.TITLE, "article title");
        values.put(ArticlesContract.Article.CONTENT, "a content");
        values.put(ArticlesContract.Article.SCORE, 0);
        values.put(ArticlesContract.Article.PUBLISHED, 1);
        values.put(ArticlesContract.Article.LAST_TIME_UPDATE, 0);
        values.put(ArticlesContract.Article.UNREAD, 1);
        values.put(ArticlesContract.Article.TRANSIENT_UNREAD, 1);
        values.put(ArticlesContract.Article.STARRED, 1);
        values.put(ArticlesContract.Article.IS_UPDATED, 1);
        values.put(ArticlesContract.Article.FEED_ID, 1);
        values.put(ArticlesContract.Article.LINK, "article links");
        values.put(ArticlesContract.Article.TAGS, "article tags");
        values.put(ArticlesContract.Article.AUTHOR, "article author");
        values.put(ArticlesContract.Article.FLAVOR_IMAGE_URI, "article flavor image uri");
        values.put(ArticlesContract.Article.CONTENT_EXCERPT, "a content excerpt");


        db.insert(DbHelper.TABLE_ARTICLES, SQLiteDatabase.CONFLICT_NONE, values);


        values.clear();
        values.put(ArticlesContract.Transaction.FIELD, "transaction field");
        values.put(ArticlesContract.Transaction.VALUE, 1);
        values.put(ArticlesContract.Transaction.ARTICLE_ID, 1);
        db.insert(DbHelper.TABLE_TRANSACTIONS, SQLiteDatabase.CONFLICT_NONE, values);
    }

    private void assertMigration1To2DataIntegrity(SupportSQLiteDatabase db) {
        Cursor cursor = db.query("SELECT * FROM " + DbHelper.TABLE_CATEGORIES);
        int count = cursor.getCount();
        assertThat(count).isEqualTo(1);
        cursor.close();

        cursor = db.query("SELECT * FROM " + DbHelper.TABLE_FEEDS);
        count = cursor.getCount();
        assertThat(count).isEqualTo(1);
        cursor.close();

        cursor = db.query("SELECT * FROM " + DbHelper.TABLE_ARTICLES);
        count = cursor.getCount();
        assertThat(count).isEqualTo(1);
        cursor.close();

        cursor = db.query("SELECT * FROM " + DbHelper.TABLE_TRANSACTIONS);
        count = cursor.getCount();
        assertThat(count).isEqualTo(1);
        cursor.close();
    }

    @Test
    public void migrate2To3() throws Exception {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 2);

        // db has schema version 2. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        // as our schema for this migration doesn't change much from the previous
        // we can reuse the same function
        createSomeArticles(db);

        // Prepare for the next version.
        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_1_2, MIGRATION_2_3);

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
        assertMigration1To2DataIntegrity(db);
    }


    @Test
    public void migrate3To4() throws Exception {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 3);

        // db has schema version 3. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        // as our schema for this migration doesn't change much from the previous
        // we can reuse the same function
        createSomeArticles(db);

        // Prepare for the next version.
        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_1_2, MIGRATION_2_3);

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
        assertMigration1To2DataIntegrity(db);
    }
}
