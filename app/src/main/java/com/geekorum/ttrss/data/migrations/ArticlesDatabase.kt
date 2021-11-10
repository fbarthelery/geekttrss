/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2021 by Frederic-Charles Barthelery.
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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migrations for the ArticleDatabase
 */

/**
 * Architecture components alpha8 adds NOT NULL constraints for column with primitive types.
 * This needs a schema migration.
 * This works by creating a table with the new schema and copying all the previous data into it,
 * then renaming the table to the original name.
 */
object MigrationFrom1To2 : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        // Migrate primitive field to add not null constraint
        migrateCategoriesTable(database)
        migrateFeedsTable(database)
        migrateTransactionTable(database)
        migrateArticlesTable(database)
    }

    private fun migrateCategoriesTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("""CREATE TABLE `categories_new` (
                |`_id` INTEGER NOT NULL,
                |`title` TEXT,
                |`unread_count` INTEGER NOT NULL,
                | PRIMARY KEY(`_id`));""".trimMargin())

            execSQL("INSERT INTO categories_new SELECT * FROM categories;")
            execSQL("DROP TABLE categories;")
            execSQL("ALTER TABLE categories_new RENAME TO categories;")
        }
    }

    private fun migrateTransactionTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("""CREATE TABLE `transactions_new` (
                |`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                |`article_id` INTEGER NOT NULL,
                |`field` TEXT, `value` INTEGER NOT NULL,
                |FOREIGN KEY(`article_id`) REFERENCES `articles`(`_id`)
                |ON UPDATE NO ACTION ON DELETE NO ACTION );""".trimMargin())

            execSQL("DROP INDEX IF EXISTS `index_transactions_article_id`;")
            execSQL("CREATE  INDEX `index_transactions_article_id` ON `transactions_new` (`article_id`);")

            execSQL("INSERT INTO transactions_new SELECT * FROM transactions;")
            execSQL("DROP TABLE transactions;")
            execSQL("ALTER TABLE transactions_new RENAME TO transactions;")
        }
    }

    private fun migrateFeedsTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("""CREATE TABLE `feeds_new` (
                |`_id` INTEGER NOT NULL,
                |`url` TEXT,
                |`title` TEXT,
                |`cat_id` INTEGER NOT NULL,
                |`display_title` TEXT,
                |`last_time_update` INTEGER NOT NULL,
                |`unread_count` INTEGER NOT NULL,
                |PRIMARY KEY(`_id`),
                |FOREIGN KEY(`cat_id`) REFERENCES `categories`(`_id`)
                |ON UPDATE NO ACTION ON DELETE NO ACTION )""".trimMargin())

            execSQL("DROP INDEX IF EXISTS `index_feeds_cat_id`;")
            execSQL("CREATE  INDEX `index_feeds_cat_id` ON `feeds_new` (`cat_id`);")

            execSQL("INSERT INTO feeds_new SELECT * FROM feeds;")
            execSQL("DROP TABLE feeds;")
            execSQL("ALTER TABLE feeds_new RENAME TO feeds;")
        }
    }

    private fun migrateArticlesTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("CREATE TABLE `articles_new` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`title` TEXT, "
                    + "`unread` INTEGER NOT NULL, "
                    + "`transiant_unread` INTEGER NOT NULL, "
                    + "`marked` INTEGER NOT NULL, "
                    + "`published` INTEGER NOT NULL, "
                    + "`score` INTEGER NOT NULL, "
                    + "`last_time_update` INTEGER NOT NULL, "
                    + "`is_updated` INTEGER NOT NULL, "
                    + "`link` TEXT, "
                    + "`feed_id` INTEGER NOT NULL, "
                    + "`tags` TEXT, "
                    + "`content` TEXT, "
                    + "`author` TEXT, "
                    + "`flavor_image_uri` TEXT, "
                    + "`content_excerpt` TEXT, "
                    + "FOREIGN KEY(`feed_id`) REFERENCES `feeds`(`_id`) "
                    + "ON UPDATE NO ACTION ON DELETE NO ACTION )")

            execSQL("DROP INDEX IF EXISTS `index_articles_feed_id`;")
            execSQL("CREATE  INDEX `index_articles_feed_id` ON `articles_new` (`feed_id`);")

            execSQL("INSERT INTO articles_new SELECT * FROM articles;")
            execSQL("DROP TABLE articles;")
            execSQL("ALTER TABLE articles_new RENAME TO articles;")
        }
    }
}


/** This migration makes fields non nullable **/
object MigrationFrom2To3 : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        migrateCategoriesTables(database)
        migrateTransactionTable(database)
        migrateFeedsTable(database)
        migrateArticlesTable(database)
    }

    private fun migrateCategoriesTables(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("""CREATE TABLE `categories_new` (
                |`_id` INTEGER NOT NULL,
                |`title` TEXT NOT NULL,
                |`unread_count` INTEGER NOT NULL,
                |PRIMARY KEY(`_id`));""".trimMargin())

            execSQL("INSERT INTO categories_new SELECT * FROM categories WHERE title is NOT NULL;")
            execSQL("DROP TABLE categories;")
            execSQL("ALTER TABLE categories_new RENAME TO categories;")
        }
    }

    private fun migrateTransactionTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("""CREATE TABLE `transactions_new` (
                |`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                |`article_id` INTEGER NOT NULL,
                |`field` TEXT NOT NULL,
                |`value` INTEGER NOT NULL,
                |FOREIGN KEY(`article_id`) REFERENCES `articles`(`_id`)
                |ON UPDATE NO ACTION ON DELETE NO ACTION );""".trimMargin())

            execSQL("DROP INDEX IF EXISTS `index_transactions_article_id`;")
            execSQL("CREATE  INDEX `index_transactions_article_id` ON `transactions_new` (`article_id`);")

            execSQL("INSERT INTO transactions_new SELECT * FROM transactions WHERE field is NOT NULL;")
            execSQL("DROP TABLE transactions;")
            execSQL("ALTER TABLE transactions_new RENAME TO transactions;")
        }
    }

    private fun migrateFeedsTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("""CREATE TABLE `feeds_new` (
                |`_id` INTEGER NOT NULL,
                |`url` TEXT NOT NULL,
                |`title` TEXT NOT NULL,
                |`cat_id` INTEGER NOT NULL,
                |`display_title` TEXT NOT NULL,
                |`last_time_update` INTEGER NOT NULL,
                |`unread_count` INTEGER NOT NULL,
                |PRIMARY KEY(`_id`),
                |FOREIGN KEY(`cat_id`) REFERENCES `categories`(`_id`)
                |ON UPDATE NO ACTION ON DELETE NO ACTION )""".trimMargin())

            execSQL("DROP INDEX IF EXISTS `index_feeds_cat_id`;")
            execSQL("CREATE  INDEX `index_feeds_cat_id` ON `feeds_new` (`cat_id`);")

            execSQL("""INSERT INTO feeds_new
                |SELECT * FROM feeds
                |WHERE url IS NOT NULL
                |AND title IS NOT NULL
                |AND display_title IS NOT NULL;""".trimMargin())
            execSQL("DROP TABLE feeds;")
            execSQL("ALTER TABLE feeds_new RENAME TO feeds;")
        }
    }

    private fun migrateArticlesTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("""CREATE TABLE `articles_new` (
                |`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                |`title` TEXT NOT NULL,
                |`unread` INTEGER NOT NULL,
                |`transiant_unread` INTEGER NOT NULL,
                |`marked` INTEGER NOT NULL,
                |`published` INTEGER NOT NULL,
                |`score` INTEGER NOT NULL,
                |`last_time_update` INTEGER NOT NULL,
                |`is_updated` INTEGER NOT NULL,
                |`link` TEXT NOT NULL,
                |`feed_id` INTEGER NOT NULL,
                |`tags` TEXT NOT NULL,
                |`content` TEXT NOT NULL,
                |`author` TEXT NOT NULL,
                |`flavor_image_uri` TEXT NOT NULL,
                |`content_excerpt` TEXT NOT NULL,
                |FOREIGN KEY(`feed_id`) REFERENCES `feeds`(`_id`)
                |ON UPDATE NO ACTION ON DELETE NO ACTION )""".trimMargin())

            execSQL("DROP INDEX IF EXISTS `index_articles_feed_id`;")
            execSQL("CREATE  INDEX `index_articles_feed_id` ON `articles_new` (`feed_id`);")

            execSQL("""INSERT INTO articles_new
                |SELECT * FROM articles
                |WHERE title IS NOT NULL
                |AND link IS NOT NULL
                |AND tags IS NOT NULL
                |AND content IS NOT NULL
                |AND author IS NOT NULL
                |AND flavor_image_uri is NOT NULL
                |AND content_excerpt IS NOT NULL;""".trimMargin())
            execSQL("DROP TABLE articles;")
            execSQL("ALTER TABLE articles_new RENAME TO articles;")
        }
    }

}


/** This migration add some cascade on delete constraints **/
object MigrationFrom3To4 : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        migrateTransactionTable(database)
    }

    private fun migrateTransactionTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("""CREATE TABLE `transactions_new` (
                |`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                |`article_id` INTEGER NOT NULL,
                |`field` TEXT NOT NULL,
                |`value` INTEGER NOT NULL,
                |FOREIGN KEY(`article_id`) REFERENCES `articles`(`_id`)
                |ON UPDATE NO ACTION ON DELETE CASCADE );""".trimMargin())

            execSQL("DROP INDEX IF EXISTS `index_transactions_article_id`;")
            execSQL("CREATE  INDEX `index_transactions_article_id` ON `transactions_new` (`article_id`);")

            execSQL("INSERT INTO transactions_new SELECT * FROM transactions WHERE field is NOT NULL;")
            execSQL("DROP TABLE transactions;")
            execSQL("ALTER TABLE transactions_new RENAME TO transactions;")
        }
    }
}


/**
 * This migration add virtual FTS tables
 */
object MigrationFrom4To5 : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        migrateArticleFTSTable(database)
    }

    private fun migrateArticleFTSTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("""CREATE VIRTUAL TABLE IF NOT EXISTS `ArticleFTS` USING FTS4(
                |`title` TEXT NOT NULL,
                |`tags` TEXT NOT NULL,
                |`content` TEXT NOT NULL,
                |`author` TEXT NOT NULL,
                |content=`articles`)""".trimMargin())
        }
    }
}

/**
 * This migration is caused by an upgrade of room. No changes are necessary
 */
object MigrationFrom5To6 : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // nothing to do here
    }
}

/**
 * This migration changes some foreign keys constraints
 */
object MigrationFrom6To7 : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        migrateArticlesTable(database)
        migrateFeedsTable(database)
    }

    private fun migrateArticlesTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL(
                """CREATE TABLE IF NOT EXISTS `articles_new` (
                    |`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    |`unread` INTEGER NOT NULL,
                    |`transiant_unread` INTEGER NOT NULL,
                    |`marked` INTEGER NOT NULL,
                    |`published` INTEGER NOT NULL,
                    |`score` INTEGER NOT NULL,
                    |`last_time_update` INTEGER NOT NULL,
                    |`is_updated` INTEGER NOT NULL,
                    |`link` TEXT NOT NULL,
                    |`feed_id` INTEGER NOT NULL,
                    |`flavor_image_uri` TEXT NOT NULL,
                    |`content_excerpt` TEXT NOT NULL,
                    |`title` TEXT NOT NULL,
                    |`tags` TEXT NOT NULL,
                    |`content` TEXT NOT NULL,
                    |`author` TEXT NOT NULL,
                    |FOREIGN KEY(`feed_id`) REFERENCES `feeds`(`_id`)
                    |ON UPDATE NO ACTION ON DELETE NO ACTION
                    |)""".trimMargin())

            execSQL("DROP INDEX IF EXISTS `index_articles_feed_id`;")
            execSQL("CREATE  INDEX `index_articles_feed_id` ON `articles_new` (`feed_id`);")

            execSQL("INSERT INTO articles_new SELECT * FROM articles;")
            execSQL("DROP TABLE articles;")
            execSQL("ALTER TABLE articles_new RENAME TO articles;")
        }
    }

    private fun migrateFeedsTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL(
                """CREATE TABLE IF NOT EXISTS `feeds_new` (
                    |`_id` INTEGER NOT NULL,
                    |`url` TEXT NOT NULL,
                    |`title` TEXT NOT NULL,
                    |`cat_id` INTEGER NOT NULL,
                    |`display_title` TEXT NOT NULL,
                    |`last_time_update` INTEGER NOT NULL,
                    |`unread_count` INTEGER NOT NULL,
                    |PRIMARY KEY(`_id`),
                    |FOREIGN KEY(`cat_id`) REFERENCES `categories`(`_id`)
                    |ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED
                    |)""".trimMargin())

            execSQL("DROP INDEX IF EXISTS `index_feeds_cat_id`;")
            execSQL("CREATE  INDEX `index_feeds_cat_id` ON `feeds_new` (`cat_id`);")

            execSQL("INSERT INTO feeds_new SELECT * FROM feeds;")
            execSQL("DROP TABLE feeds;")
            execSQL("ALTER TABLE feeds_new RENAME TO feeds;")
        }
    }

}

/**
 * This migration changes Feed table to add subscribed column
 */
object MigrationFrom7To8 : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        migrateFeedsTable(database)
    }

    private fun migrateFeedsTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL(
                """CREATE TABLE IF NOT EXISTS `feeds_new` (
                    |`_id` INTEGER NOT NULL,
                    |`url` TEXT NOT NULL,
                    |`title` TEXT NOT NULL,
                    |`cat_id` INTEGER NOT NULL,
                    |`display_title` TEXT NOT NULL,
                    |`last_time_update` INTEGER NOT NULL,
                    |`unread_count` INTEGER NOT NULL,
                    |`is_subscribed` INTEGER NOT NULL,
                    |PRIMARY KEY(`_id`),
                    |FOREIGN KEY(`cat_id`) REFERENCES `categories`(`_id`)
                    |ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED
                    |)""".trimMargin())

            execSQL("DROP INDEX IF EXISTS `index_feeds_cat_id`;")
            execSQL("CREATE  INDEX `index_feeds_cat_id` ON `feeds_new` (`cat_id`);")

            execSQL("INSERT INTO feeds_new SELECT *, 1 FROM feeds;")
            execSQL("DROP TABLE feeds;")
            execSQL("ALTER TABLE feeds_new RENAME TO feeds;")
        }
    }

}

/**
 * This migration adds the AccountInfo table
 */
object MigrationFrom8To9 : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        createAccountInfoTable(database)
    }

    private fun createAccountInfoTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL(
                """CREATE TABLE IF NOT EXISTS `account_info` (
                    |`server_version` TEXT NOT NULL,
                    |`api_level` INTEGER NOT NULL,
                    |`account_username` TEXT NOT NULL,
                    |`account_url` TEXT NOT NULL,
                    |PRIMARY KEY(`account_username`, `account_url`)
                    |)""".trimMargin())
        }
    }
}

/**
 * This migration adds the Feed.feed_icon_url column
 */
object MigrationFrom9To10 : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        migrateFeedsTable(database)
    }

    private fun migrateFeedsTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL(
                """CREATE TABLE IF NOT EXISTS `feeds_new` (
                    |`_id` INTEGER NOT NULL,
                    |`url` TEXT NOT NULL,
                    |`title` TEXT NOT NULL,
                    |`cat_id` INTEGER NOT NULL,
                    |`display_title` TEXT NOT NULL,
                    |`last_time_update` INTEGER NOT NULL,
                    |`unread_count` INTEGER NOT NULL,
                    |`is_subscribed` INTEGER NOT NULL,
                    |`feed_icon_url` TEXT NOT NULL,
                    |PRIMARY KEY(`_id`),
                    |FOREIGN KEY(`cat_id`) REFERENCES `categories`(`_id`)
                    |ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED
                    |)""".trimMargin())

            execSQL("DROP INDEX IF EXISTS `index_feeds_cat_id`;")
            execSQL("CREATE  INDEX `index_feeds_cat_id` ON `feeds_new` (`cat_id`);")

            execSQL("INSERT INTO feeds_new SELECT *, '' FROM feeds;")
            execSQL("DROP TABLE feeds;")
            execSQL("ALTER TABLE feeds_new RENAME TO feeds;")
        }
    }
}

/**
 * This migration adds the attachments table
 */
object MigrationFrom10To11 : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        addAttachmentsTable(database)
    }

    private fun addAttachmentsTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL(
                    """CREATE TABLE IF NOT EXISTS `attachments` (
                    |`_id` INTEGER NOT NULL,
                    |`content_url` TEXT NOT NULL,
                    |`content_type` TEXT NOT NULL,
                    |`post_id` INTEGER NOT NULL,
                    |`title` TEXT NOT NULL,
                    |`duration` INTEGER NOT NULL,
                    |`width` INTEGER NOT NULL,
                    |`height` INTEGER NOT NULL,
                    |PRIMARY KEY(`_id`),
                    |FOREIGN KEY(`post_id`) REFERENCES `articles`(`_id`)
                    |ON UPDATE NO ACTION ON DELETE CASCADE
                    |)""".trimMargin())
        }
    }
}

/**
 * This migration adds an index on post_id of the attachments table
 */
object MigrationFrom11To12 : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        addPostIdIndex(database)
    }

    private fun addPostIdIndex(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("CREATE INDEX IF NOT EXISTS `index_attachments_post_id` ON `attachments` (`post_id`)")
        }
    }
}


/**
 * This migration adds a relation table for articles_tags
 */
object MigrationFrom12To13 : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        addArticleTagsTable(database)
        migrateArticleTags(database)
    }

    private fun addArticleTagsTable(database: SupportSQLiteDatabase) {
        with(database) {
            execSQL("""CREATE TABLE IF NOT EXISTS `articles_tags` (
                |`article_id` INTEGER NOT NULL,
                |`tag` TEXT NOT NULL,
                |PRIMARY KEY(`tag`, `article_id`)
                |FOREIGN KEY(`article_id`) REFERENCES `articles`(`_id`) ON UPDATE NO ACTION ON DELETE CASCADE
                |)""".trimMargin())
            execSQL("CREATE INDEX IF NOT EXISTS `index_articles_tags_article_id` ON `articles_tags` (`article_id`)")
            execSQL("CREATE INDEX IF NOT EXISTS `index_articles_tags_tag` ON `articles_tags` (`tag`)")
        }
    }

    private fun migrateArticleTags(database: SupportSQLiteDatabase) {
        with(database) {
            query("SELECT _id, tags FROM articles").use {
                while (it.moveToNext()) {
                    val articleId = it.getLong(0)
                    val tags = it.getString(1)
                    val tagsList = tags.split(",")
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                        .distinct()
                    for (tag in tagsList) {
                        execSQL("INSERT INTO `articles_tags` (`article_id`, `tag`) VALUES (?, ?)", arrayOf(articleId, tag))
                    }
                }
            }
        }
    }
}

internal val ALL_MIGRATIONS = listOf(MigrationFrom1To2,
        MigrationFrom2To3,
        MigrationFrom3To4,
        MigrationFrom4To5,
        MigrationFrom5To6,
        MigrationFrom6To7,
        MigrationFrom7To8,
        MigrationFrom8To9,
        MigrationFrom9To10,
        MigrationFrom10To11,
        MigrationFrom11To12,
        MigrationFrom12To13)
