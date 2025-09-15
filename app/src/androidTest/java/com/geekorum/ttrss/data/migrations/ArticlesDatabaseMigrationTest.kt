/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
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

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.content.contentValuesOf
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.geekorum.ttrss.data.ArticlesDatabase
import com.geekorum.ttrss.data.ArticlesDatabase.Tables
import com.geekorum.ttrss.providers.ArticlesContract
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test

private const val TEST_DB = "migration-test"

/**
 * Testsuite for [ArticlesDatabase] migrations.
 */
@RunWith(AndroidJUnit4::class)
class ArticlesDatabaseMigrationTest {

    @get:Rule
    var helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ArticlesDatabase::class.java,
        listOf(MigrationFrom13To14())
    )

    @Test
    fun migrate1To2() {
        helper.createDatabase(TEST_DB, 1).use {
            // db has schema version 1. insert some data using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            createSomeArticles(it)
        }

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        helper.runMigrationsAndValidate(TEST_DB, 2, true,
                *ALL_MIGRATIONS.toTypedArray()).use {
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
        db.insert(Tables.CATEGORIES, SQLiteDatabase.CONFLICT_NONE, values)

        values = contentValuesOf(
            ArticlesContract.Feed.TITLE to "feed title",
            ArticlesContract.Feed.URL to "feed url",
            ArticlesContract.Feed.CAT_ID to 1,
            ArticlesContract.Feed.UNREAD_COUNT to 2,
            ArticlesContract.Feed.LAST_TIME_UPDATE to 0,
            ArticlesContract.Feed.DISPLAY_TITLE to "display title"
        )
        db.insert(Tables.FEEDS, SQLiteDatabase.CONFLICT_NONE, values)

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
        db.insert(Tables.ARTICLES, SQLiteDatabase.CONFLICT_NONE, values)


        values = contentValuesOf(
            ArticlesContract.Transaction.FIELD to "transaction field",
            ArticlesContract.Transaction.VALUE to 1,
            ArticlesContract.Transaction.ARTICLE_ID to 1
        )
        db.insert(Tables.TRANSACTIONS, SQLiteDatabase.CONFLICT_NONE, values)
    }

    private fun assertMigration1To2DataIntegrity(db: SupportSQLiteDatabase) {
        db.query("SELECT * FROM ${Tables.CATEGORIES}").use {
            assertThat(it.count).isEqualTo(1)
        }

        db.query("SELECT * FROM ${Tables.FEEDS}" ).use {
            assertThat(it.count).isEqualTo(1)
        }

        db.query("SELECT * FROM ${Tables.ARTICLES}").use {
            assertThat(it.count).isEqualTo(1)
        }

        db.query("SELECT * FROM ${Tables.TRANSACTIONS}").use {
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
                *ALL_MIGRATIONS.toTypedArray()).use {
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
                *ALL_MIGRATIONS.toTypedArray()).use {
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
                *ALL_MIGRATIONS.toTypedArray()).use {
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
                *ALL_MIGRATIONS.toTypedArray()).use {
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

    @Test
    fun migrate7To8() {
        helper.createDatabase(TEST_DB, 7).use {
            // db has schema version 7. insert some contentData using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            // as our schema for this migration doesn't change much from the previous
            // we can reuse the same function
            createSomeArticles(it)
        }

        helper.runMigrationsAndValidate(TEST_DB, 8, true,
                *ALL_MIGRATIONS.toTypedArray()).use {
            // MigrationTestHelper automatically verifies the schema changes,
            // but you need to validate that the contentData was migrated properly.
            assertMigration7To8DataIntegrity(it)
        }
    }

    private fun assertMigration7To8DataIntegrity(db: SupportSQLiteDatabase) {
        assertMigration1To2DataIntegrity(db)
        db.query("SELECT * FROM ${Tables.FEEDS}").use {
            assertThat(it.count).isEqualTo(1)
            it.moveToFirst()
            assertThat(it.getValue<Boolean>("is_subscribed")).isTrue()
        }
    }

    private fun assertMigration12To13DataIntegrity(db: SupportSQLiteDatabase) {
        assertMigration7To8DataIntegrity(db)
        db.query("SELECT * FROM ${Tables.ARTICLES_TAGS}").use {
            assertThat(it.count).isEqualTo(1)
            it.moveToFirst()
            assertThat(it.getValue<String>("tag")).isEqualTo("article tags")
        }
    }

    private fun createSomeArticlesFromVersion8(db: SupportSQLiteDatabase) {
        var values = contentValuesOf(
            ArticlesContract.Category.TITLE to "category",
            ArticlesContract.Category.UNREAD_COUNT to 2
        )
        db.insert(Tables.CATEGORIES, SQLiteDatabase.CONFLICT_NONE, values)

        values = contentValuesOf(
            ArticlesContract.Feed.TITLE to "feed title",
            ArticlesContract.Feed.URL to "feed url",
            ArticlesContract.Feed.CAT_ID to 0,
            ArticlesContract.Feed.UNREAD_COUNT to 2,
            ArticlesContract.Feed.LAST_TIME_UPDATE to 0,
            ArticlesContract.Feed.DISPLAY_TITLE to "display title",
            ArticlesContract.Feed.IS_SUBSCRIBED to 1
            )
        db.insert(Tables.FEEDS, SQLiteDatabase.CONFLICT_NONE, values)

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
        db.insert(Tables.ARTICLES, SQLiteDatabase.CONFLICT_NONE, values)


        values = contentValuesOf(
            ArticlesContract.Transaction.FIELD to "transaction field",
            ArticlesContract.Transaction.VALUE to 1,
            ArticlesContract.Transaction.ARTICLE_ID to 1
        )
        db.insert(Tables.TRANSACTIONS, SQLiteDatabase.CONFLICT_NONE, values)
    }


    @Test
    fun migrate8To9() {
        helper.createDatabase(TEST_DB, 8).use {
            // db has schema version 8. insert some contentData using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            // as our schema for this migration doesn't change much from the previous
            // we can reuse the same function
            createSomeArticlesFromVersion8(it)
        }

        helper.runMigrationsAndValidate(TEST_DB, 9, true,
                *ALL_MIGRATIONS.toTypedArray()).use {
            // MigrationTestHelper automatically verifies the schema changes,
            // but you need to validate that the contentData was migrated properly.
            assertMigration7To8DataIntegrity(it)
        }
    }

    @Test
    fun migrate9To10() {
        helper.createDatabase(TEST_DB, 9).use {
            // db has schema version 8. insert some contentData using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            // as our schema for this migration doesn't change much from the previous
            // we can reuse the same function
            createSomeArticlesFromVersion8(it)
        }

        helper.runMigrationsAndValidate(TEST_DB, 10, true,
                *ALL_MIGRATIONS.toTypedArray()).use {
            // MigrationTestHelper automatically verifies the schema changes,
            // but you need to validate that the contentData was migrated properly.
            assertMigration7To8DataIntegrity(it)
        }
    }

    private fun createSomeArticlesFromVersion10(db: SupportSQLiteDatabase) {
        var values = contentValuesOf(
                ArticlesContract.Category.TITLE to "category",
                ArticlesContract.Category.UNREAD_COUNT to 2
        )
        db.insert(Tables.CATEGORIES, SQLiteDatabase.CONFLICT_NONE, values)

        @Suppress("DEPRECATION")
        values = contentValuesOf(
                ArticlesContract.Feed.TITLE to "feed title",
                ArticlesContract.Feed.URL to "feed url",
                ArticlesContract.Feed.CAT_ID to 1,
                ArticlesContract.Feed.UNREAD_COUNT to 2,
                ArticlesContract.Feed.LAST_TIME_UPDATE to 0,
                ArticlesContract.Feed.DISPLAY_TITLE to "display title",
                ArticlesContract.Feed.IS_SUBSCRIBED to 1,
                ArticlesContract.Feed.ICON_URL to "icon_url"
        )
        db.insert(Tables.FEEDS, SQLiteDatabase.CONFLICT_NONE, values)

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
        db.insert(Tables.ARTICLES, SQLiteDatabase.CONFLICT_NONE, values)


        values = contentValuesOf(
                ArticlesContract.Transaction.FIELD to "transaction field",
                ArticlesContract.Transaction.VALUE to 1,
                ArticlesContract.Transaction.ARTICLE_ID to 1
        )
        db.insert(Tables.TRANSACTIONS, SQLiteDatabase.CONFLICT_NONE, values)
    }


    @Test
    fun migrate10To11() {
        helper.createDatabase(TEST_DB, 10).use {
            // db has schema version 8. insert some contentData using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            // as our schema for this migration doesn't change much from the previous
            // we can reuse the same function
            createSomeArticlesFromVersion10(it)
        }

        helper.runMigrationsAndValidate(TEST_DB, 11, true,
                *ALL_MIGRATIONS.toTypedArray()).use {
            // MigrationTestHelper automatically verifies the schema changes,
            // but you need to validate that the contentData was migrated properly.
            assertMigration7To8DataIntegrity(it)
        }
    }

    @Test
    fun migrate11To12() {
        helper.createDatabase(TEST_DB, 11).use {
            // db has schema version 8. insert some contentData using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            // as our schema for this migration doesn't change much from the previous
            // we can reuse the same function
            createSomeArticlesFromVersion10(it)
        }

        helper.runMigrationsAndValidate(TEST_DB, 11, true,
            *ALL_MIGRATIONS.toTypedArray()).use {
            // MigrationTestHelper automatically verifies the schema changes,
            // but you need to validate that the contentData was migrated properly.
            assertMigration7To8DataIntegrity(it)
        }
    }

    @Test
    fun migrate12To13() {
        helper.createDatabase(TEST_DB, 12).use {
            // db has schema version 8. insert some contentData using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            // as our schema for this migration doesn't change much from the previous
            // we can reuse the same function
            createSomeArticlesFromVersion10(it)
        }

        helper.runMigrationsAndValidate(TEST_DB, 13, true,
            *ALL_MIGRATIONS.toTypedArray()).use {
            // MigrationTestHelper automatically verifies the schema changes,
            // but you need to validate that the contentData was migrated properly.
            assertMigration12To13DataIntegrity(it)
        }
    }

    private fun createSomeArticlesFromVersion13(db: SupportSQLiteDatabase) {
        var values = contentValuesOf(
            ArticlesContract.Category.TITLE to "category",
            ArticlesContract.Category.UNREAD_COUNT to 2
        )
        db.insert(Tables.CATEGORIES, SQLiteDatabase.CONFLICT_NONE, values)

        @Suppress("DEPRECATION")
        values = contentValuesOf(
            ArticlesContract.Feed.TITLE to "feed title",
            ArticlesContract.Feed.URL to "feed url",
            ArticlesContract.Feed.CAT_ID to 1,
            ArticlesContract.Feed.UNREAD_COUNT to 2,
            ArticlesContract.Feed.LAST_TIME_UPDATE to 0,
            ArticlesContract.Feed.DISPLAY_TITLE to "display title",
            ArticlesContract.Feed.IS_SUBSCRIBED to 1,
            ArticlesContract.Feed.ICON_URL to "icon_url"
        )
        db.insert(Tables.FEEDS, SQLiteDatabase.CONFLICT_NONE, values)

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
        db.insert(Tables.ARTICLES, SQLiteDatabase.CONFLICT_NONE, values)

        values = contentValuesOf(
            ArticlesContract.ArticleTags.ARTICLE_ID to 1,
            ArticlesContract.ArticleTags.TAG to "article tags"
        )
        db.insert(Tables.ARTICLES_TAGS, SQLiteDatabase.CONFLICT_NONE, values)

        values = contentValuesOf(
            ArticlesContract.Transaction.FIELD to "transaction field",
            ArticlesContract.Transaction.VALUE to 1,
            ArticlesContract.Transaction.ARTICLE_ID to 1
        )
        db.insert(Tables.TRANSACTIONS, SQLiteDatabase.CONFLICT_NONE, values)
    }

    @Test
    fun migrate13To14() {
        helper.createDatabase(TEST_DB, 13).use {
            // db has schema version 8. insert some contentData using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            // as our schema for this migration doesn't change much from the previous
            // we can reuse the same function
            createSomeArticlesFromVersion13(it)
        }

        helper.runMigrationsAndValidate(TEST_DB, 14, true,
            *ALL_MIGRATIONS.toTypedArray()).use {
            // MigrationTestHelper automatically verifies the schema changes,
            // but you need to validate that the contentData was migrated properly.
            // no data changes from 13 to 14 so only test that data is still there
            // using assertMigration12To13DataIntegrity
            assertMigration12To13DataIntegrity(it)
        }
    }


    private inline fun <reified T> Cursor.getValue(columnName: String) : T {
        val index = getColumnIndexOrThrow(columnName)
        @Suppress("IMPLICIT_CAST_TO_ANY")
        return when (T::class) {
            String::class -> getString(index)
            Int::class -> getInt(index)
            ByteArray::class -> getBlob(index)
            Double::class -> getDouble(index)
            Float::class -> getFloat(index)
            Long::class -> getLong(index)
            Short::class -> getShort(index)
            Boolean::class -> getInt(index) != 0
            else -> throw IllegalArgumentException()
        } as T
    }

}
