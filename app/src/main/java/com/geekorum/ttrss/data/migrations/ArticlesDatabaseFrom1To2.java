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

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.migration.Migration;

/**
 * Architecture components alpha8 adds NOT NULL constraints for column with primitive types.
 * This needs a schema migration.
 * This works by creating a table with the new schema and copying all the previous data into it,
 * then renaming the table to the original name.
 */
public class ArticlesDatabaseFrom1To2 extends Migration {

    /**
     * Creates the {@link ArticlesDatabaseFrom1To2} migration.
     */
    public ArticlesDatabaseFrom1To2() {
        super(1, 2);
    }

    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Migrate primitive field to add not null constraint
        database.beginTransaction();
        try {
            migrateCategoriesTable(database);
            migrateFeedsTable(database);
            migrateTransactionTable(database);
            migrateArticlesTable(database);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    private void migrateCategoriesTable(SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `categories_new` (`_id` INTEGER NOT NULL,"
                + "`title` TEXT, "
                + "`unread_count` INTEGER NOT NULL, "
                + "PRIMARY KEY(`_id`));");

        database.execSQL("INSERT INTO categories_new SELECT * FROM categories;");
        database.execSQL("DROP TABLE categories;");
        database.execSQL("ALTER TABLE categories_new RENAME TO categories;");
    }

    private void migrateTransactionTable(SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `transactions_new` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + "`article_id` INTEGER NOT NULL, "
                + "`field` TEXT, "
                + "`value` INTEGER NOT NULL, "
                + "FOREIGN KEY(`article_id`) REFERENCES `articles`(`_id`) "
                + "ON UPDATE NO ACTION ON DELETE NO ACTION );");

        database.execSQL("DROP INDEX IF EXISTS `index_transactions_article_id`;");
        database.execSQL("CREATE  INDEX `index_transactions_article_id` ON `transactions_new` (`article_id`);");

        database.execSQL("INSERT INTO transactions_new SELECT * FROM transactions;");
        database.execSQL("DROP TABLE transactions;");
        database.execSQL("ALTER TABLE transactions_new RENAME TO transactions;");
    }

    private void migrateFeedsTable(SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `feeds_new` (`_id` INTEGER NOT NULL, "
                + "`url` TEXT, "
                + "`title` TEXT, "
                + "`cat_id` INTEGER NOT NULL, "
                + "`display_title` TEXT, "
                + "`last_time_update` INTEGER NOT NULL, "
                + "`unread_count` INTEGER NOT NULL, "
                + "PRIMARY KEY(`_id`), "
                + "FOREIGN KEY(`cat_id`) REFERENCES `categories`(`_id`) "
                + "ON UPDATE NO ACTION ON DELETE NO ACTION )");

        database.execSQL("DROP INDEX IF EXISTS `index_feeds_cat_id`;");
        database.execSQL("CREATE  INDEX `index_feeds_cat_id` ON `feeds_new` (`cat_id`);");

        database.execSQL("INSERT INTO feeds_new SELECT * FROM feeds;");
        database.execSQL("DROP TABLE feeds;");
        database.execSQL("ALTER TABLE feeds_new RENAME TO feeds;");
    }

    private void migrateArticlesTable(SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `articles_new` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
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
                + "ON UPDATE NO ACTION ON DELETE NO ACTION )");

        database.execSQL("DROP INDEX IF EXISTS `index_articles_feed_id`;");
        database.execSQL("CREATE  INDEX `index_articles_feed_id` ON `articles_new` (`feed_id`);");

        database.execSQL("INSERT INTO articles_new SELECT * FROM articles;");
        database.execSQL("DROP TABLE articles;");
        database.execSQL("ALTER TABLE articles_new RENAME TO articles;");
    }

}
