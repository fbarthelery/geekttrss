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
package com.geekorum.ttrss.data.migrations

import android.database.sqlite.SQLiteDatabase
import androidx.core.content.contentValuesOf
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.geekorum.ttrss.data.ArticlesDatabase
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.providers.DbHelper
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test

/**
 * Testsuite for [ArticlesDatabase] migrations.
 */
@RunWith(AndroidJUnit4::class)
class ArticlesDatabaseMigrationTest {

    @get:Rule
    var helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ArticlesDatabase::class.java.canonicalName)

    @Test
    fun migrate1To2() {
        helper.createDatabase(TEST_DB, 1).use {
            // db has schema version 1. insert some data using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            createSomeArticles(it)
        }

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        helper.runMigrationsAndValidate(TEST_DB, 2, true, MigrationFrom1To2).use {
            // MigrationTestHelper automatically verifies the schema changes,
            // but you need to validate that the data was migrated properly.
            assertMigration1To2DataIntegrity(it)
        }
    }

    private fun createSomeArticles(db: SupportSQLiteDatabase) {
        var values = contentValuesOf(
            ArticlesContract.Category.TITLE to "category",
            ArticlesContract.Category.UNREAD_COUNT to 2
        )
        db.insert(DbHelper.TABLE_CATEGORIES, SQLiteDatabase.CONFLICT_NONE, values)

        values = contentValuesOf(
            ArticlesContract.Feed.TITLE to "feed title",
            ArticlesContract.Feed.URL to "feed url",
            ArticlesContract.Feed.CAT_ID to 0,
            ArticlesContract.Feed.UNREAD_COUNT to 2,
            ArticlesContract.Feed.LAST_TIME_UPDATE to 0,
            ArticlesContract.Feed.DISPLAY_TITLE to "display title"
        )
        db.insert(DbHelper.TABLE_FEEDS, SQLiteDatabase.CONFLICT_NONE, values)

        values = contentValuesOf(
            ArticlesContract.Article.TITLE to "article title",
            ArticlesContract.Article.CONTENT to "a content",
            ArticlesContract.Article.SCORE to 0,
            ArticlesContract.Article.PUBLISHED to 1,
            ArticlesContract.Article.LAST_TIME_UPDATE to 0,
            ArticlesContract.Article.UNREAD to 1,
            ArticlesContract.Article.TRANSIENT_UNREAD to 1,
            ArticlesContract.Article.STARRED to 1,
            ArticlesContract.Article.IS_UPDATED to 1,
            ArticlesContract.Article.FEED_ID to 1,
            ArticlesContract.Article.LINK to "article links",
            ArticlesContract.Article.TAGS to "article tags",
            ArticlesContract.Article.AUTHOR to "article author",
            ArticlesContract.Article.FLAVOR_IMAGE_URI to "article flavor image uri",
            ArticlesContract.Article.CONTENT_EXCERPT to "a content excerpt"
        )
        db.insert(DbHelper.TABLE_ARTICLES, SQLiteDatabase.CONFLICT_NONE, values)


        values = contentValuesOf(
            ArticlesContract.Transaction.FIELD to "transaction field",
            ArticlesContract.Transaction.VALUE to 1,
            ArticlesContract.Transaction.ARTICLE_ID to 1
        )
        db.insert(DbHelper.TABLE_TRANSACTIONS, SQLiteDatabase.CONFLICT_NONE, values)
    }

    private fun assertMigration1To2DataIntegrity(db: SupportSQLiteDatabase) {
        db.query("SELECT * FROM " + DbHelper.TABLE_CATEGORIES).use {
            assertThat(it.count).isEqualTo(1)
        }

        db.query("SELECT * FROM " + DbHelper.TABLE_FEEDS).use {
            assertThat(it.count).isEqualTo(1)
        }

        db.query("SELECT * FROM " + DbHelper.TABLE_ARTICLES).use {
            assertThat(it.count).isEqualTo(1)
        }

        db.query("SELECT * FROM " + DbHelper.TABLE_TRANSACTIONS).use {
            assertThat(it.count).isEqualTo(1)
        }
    }

    @Test
    fun migrate2To3() {
        helper.createDatabase(TEST_DB, 2).use {
            // db has schema version 2. insert some data using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            // as our schema for this migration doesn't change much from the previous
            // we can reuse the same function
            createSomeArticles(it)
        }

        helper.runMigrationsAndValidate(TEST_DB, 3, true,
            MigrationFrom1To2, MigrationFrom2To3).use {
            // MigrationTestHelper automatically verifies the schema changes,
            // but you need to validate that the data was migrated properly.
            assertMigration1To2DataIntegrity(it)
        }
    }


    @Test
    @Throws(Exception::class)
    fun migrate3To4() {
        helper.createDatabase(TEST_DB, 3).use {
            // db has schema version 3. insert some data using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            // as our schema for this migration doesn't change much from the previous
            // we can reuse the same function
            createSomeArticles(it)
        }

        helper.runMigrationsAndValidate(TEST_DB, 4, true,
            MigrationFrom1To2, MigrationFrom2To3, MigrationFrom3To4).use {
            // MigrationTestHelper automatically verifies the schema changes,
            // but you need to validate that the data was migrated properly.
            assertMigration1To2DataIntegrity(it)
        }
    }

    @Test
    fun migrate4To5() {
        helper.createDatabase(TEST_DB, 4).use {
            // db has schema version 4. insert some contentData using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            // as our schema for this migration doesn't change much from the previous
            // we can reuse the same function
            createSomeArticles(it)
        }

        helper.runMigrationsAndValidate(TEST_DB, 5, true,
            MigrationFrom1To2, MigrationFrom2To3, MigrationFrom3To4, MigrationFrom4To5).use {
            // MigrationTestHelper automatically verifies the schema changes,
            // but you need to validate that the contentData was migrated properly.
            assertMigration1To2DataIntegrity(it)
        }
    }

    @Test
    fun migrate5To6() {
        helper.createDatabase(TEST_DB, 5).use {
            // db has schema version 5. insert some contentData using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            // as our schema for this migration doesn't change much from the previous
            // we can reuse the same function
            createSomeArticles(it)
        }

        helper.runMigrationsAndValidate(TEST_DB, 6, true,
            MigrationFrom1To2, MigrationFrom2To3, MigrationFrom3To4, MigrationFrom4To5,
            MigrationFrom5To6).use {
            // MigrationTestHelper automatically verifies the schema changes,
            // but you need to validate that the contentData was migrated properly.
            assertMigration1To2DataIntegrity(it)
        }
    }

    @Test
    fun migrate6To7() {
        helper.createDatabase(TEST_DB, 6).use {
            // db has schema version 6. insert some contentData using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            // as our schema for this migration doesn't change much from the previous
            // we can reuse the same function
            createSomeArticles(it)
        }

        helper.runMigrationsAndValidate(TEST_DB, 7, true,
            MigrationFrom1To2, MigrationFrom2To3, MigrationFrom3To4, MigrationFrom4To5,
            MigrationFrom5To6, MigrationFrom6To7).use {
            // MigrationTestHelper automatically verifies the schema changes,
            // but you need to validate that the contentData was migrated properly.
            assertMigration1To2DataIntegrity(it)
        }
    }

    companion object {
        private val TEST_DB = "migration-test"
    }
}
