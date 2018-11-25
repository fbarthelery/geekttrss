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

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

/**
 * Migrations for the ArticleDatabase
 */

/** This migration makes fields non nullable **/
class ArticlesDatabaseFrom2To3 : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.beginTransaction()
        try {
            migrateCategoriesTables(database)
            migrateTransactionTable(database)
            migrateFeedsTable(database)
            migrateArticlesTable(database)
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    private fun migrateCategoriesTables(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("CREATE TABLE `categories_new` (`_id` INTEGER NOT NULL,"
                    + "`title` TEXT NOT NULL, "
                    + "`unread_count` INTEGER NOT NULL, "
                    + "PRIMARY KEY(`_id`));")

            execSQL("INSERT INTO categories_new SELECT * FROM categories WHERE title is NOT NULL;")
            execSQL("DROP TABLE categories;")
            execSQL("ALTER TABLE categories_new RENAME TO categories;")
        }
    }

    private fun migrateTransactionTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("CREATE TABLE `transactions_new` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`article_id` INTEGER NOT NULL, "
                    + "`field` TEXT NOT NULL, "
                    + "`value` INTEGER NOT NULL, "
                    + "FOREIGN KEY(`article_id`) REFERENCES `articles`(`_id`) "
                    + "ON UPDATE NO ACTION ON DELETE NO ACTION );")

            execSQL("DROP INDEX IF EXISTS `index_transactions_article_id`;")
            execSQL("CREATE  INDEX `index_transactions_article_id` ON `transactions_new` (`article_id`);")

            execSQL("INSERT INTO transactions_new SELECT * FROM transactions WHERE field is NOT NULL;")
            execSQL("DROP TABLE transactions;")
            execSQL("ALTER TABLE transactions_new RENAME TO transactions;")
        }
    }

    private fun migrateFeedsTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("CREATE TABLE `feeds_new` (`_id` INTEGER NOT NULL, "
                    + "`url` TEXT NOT NULL, "
                    + "`title` TEXT NOT NULL, "
                    + "`cat_id` INTEGER NOT NULL, "
                    + "`display_title` TEXT NOT NULL, "
                    + "`last_time_update` INTEGER NOT NULL, "
                    + "`unread_count` INTEGER NOT NULL, "
                    + "PRIMARY KEY(`_id`), "
                    + "FOREIGN KEY(`cat_id`) REFERENCES `categories`(`_id`) "
                    + "ON UPDATE NO ACTION ON DELETE NO ACTION )")

            execSQL("DROP INDEX IF EXISTS `index_feeds_cat_id`;")
            execSQL("CREATE  INDEX `index_feeds_cat_id` ON `feeds_new` (`cat_id`);")

            execSQL("INSERT INTO feeds_new SELECT * FROM feeds WHERE url IS NOT NULL AND title IS NOT NULL AND display_title IS NOT NULL;")
            execSQL("DROP TABLE feeds;")
            execSQL("ALTER TABLE feeds_new RENAME TO feeds;")
        }
    }

    private fun migrateArticlesTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("CREATE TABLE `articles_new` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`title` TEXT NOT NULL, "
                    + "`unread` INTEGER NOT NULL, "
                    + "`transiant_unread` INTEGER NOT NULL, "
                    + "`marked` INTEGER NOT NULL, "
                    + "`published` INTEGER NOT NULL, "
                    + "`score` INTEGER NOT NULL, "
                    + "`last_time_update` INTEGER NOT NULL, "
                    + "`is_updated` INTEGER NOT NULL, "
                    + "`link` TEXT NOT NULL, "
                    + "`feed_id` INTEGER NOT NULL, "
                    + "`tags` TEXT NOT NULL, "
                    + "`content` TEXT NOT NULL, "
                    + "`author` TEXT NOT NULL, "
                    + "`flavor_image_uri` TEXT NOT NULL, "
                    + "`content_excerpt` TEXT NOT NULL, "
                    + "FOREIGN KEY(`feed_id`) REFERENCES `feeds`(`_id`) "
                    + "ON UPDATE NO ACTION ON DELETE NO ACTION )")

            execSQL("DROP INDEX IF EXISTS `index_articles_feed_id`;")
            execSQL("CREATE  INDEX `index_articles_feed_id` ON `articles_new` (`feed_id`);")


            execSQL("INSERT INTO articles_new SELECT * FROM articles WHERE title IS NOT NULL AND link IS NOT NULL "
                    + "AND tags IS NOT NULL AND content IS NOT NULL AND author IS NOT NULL "
                    + "AND flavor_image_uri is NOT NULL AND content_excerpt IS NOT NULL;")
            execSQL("DROP TABLE articles;")
            execSQL("ALTER TABLE articles_new RENAME TO articles;")
        }
    }

}



/** This migration add some cascade on delete constraints **/
class ArticlesDatabaseFrom3To4 : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.beginTransaction()
        try {
            migrateTransactionTable(database)
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    private fun migrateTransactionTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("CREATE TABLE `transactions_new` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`article_id` INTEGER NOT NULL, "
                    + "`field` TEXT NOT NULL, "
                    + "`value` INTEGER NOT NULL, "
                    + "FOREIGN KEY(`article_id`) REFERENCES `articles`(`_id`)"
                    + "ON UPDATE NO ACTION ON DELETE CASCADE );")

            execSQL("DROP INDEX IF EXISTS `index_transactions_article_id`;")
            execSQL("CREATE  INDEX `index_transactions_article_id` ON `transactions_new` (`article_id`);")

            execSQL("INSERT INTO transactions_new SELECT * FROM transactions WHERE field is NOT NULL;")
            execSQL("DROP TABLE transactions;")
            execSQL("ALTER TABLE transactions_new RENAME TO transactions;")
        }
    }

}
