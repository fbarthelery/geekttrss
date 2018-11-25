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

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.geekorum.ttrss.providers.ArticlesProvidersDao;
import com.geekorum.ttrss.room_migration.RoomMigrationDao;

@Database(entities = {Article.class, Category.class, Feed.class, Transaction.class},
        version = 4)
public abstract class ArticlesDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "room_articles.db";

    public abstract ArticleDao articleDao();

    public abstract RoomMigrationDao roomMigrationDao();

    public abstract TransactionsDao transactionsDao();

    public abstract SynchronizationDao synchronizationDao();

    public abstract ArticlesProvidersDao articlesProvidersDao();

    public abstract FeedsDao feedsDao();
}
