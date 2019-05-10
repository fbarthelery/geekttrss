/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2019 by Frederic-Charles Barthelery.
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

import android.app.Application;
import androidx.room.Room;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.geekorum.ttrss.data.migrations.MigrationFrom1To2;
import com.geekorum.ttrss.data.migrations.MigrationFrom2To3;
import com.geekorum.ttrss.data.migrations.MigrationFrom3To4;
import com.geekorum.ttrss.data.migrations.MigrationFrom4To5;
import com.geekorum.ttrss.data.migrations.MigrationFrom5To6;
import com.geekorum.ttrss.data.migrations.MigrationFrom6To7;
import com.geekorum.ttrss.providers.ArticlesProvidersDao;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Provides some dependencies needed to access the room database.
 */
@Module
public class ArticlesDatabaseModule {

    @Provides
    @Singleton
    ArticlesDatabase providesAppDatabase(Application application) {
        return Room.databaseBuilder(application, ArticlesDatabase.class, ArticlesDatabase.DATABASE_NAME)
                .addMigrations(MigrationFrom1To2.INSTANCE,
                        MigrationFrom2To3.INSTANCE,
                        MigrationFrom3To4.INSTANCE,
                        MigrationFrom4To5.INSTANCE,
                        MigrationFrom5To6.INSTANCE,
                        MigrationFrom6To7.INSTANCE)
                .build();
    }

    @Provides
    SupportSQLiteOpenHelper providesRoomDbHelper(ArticlesDatabase database) {
        return database.getOpenHelper();
    }

    @Provides
    ArticleDao providesArticleDao(ArticlesDatabase database) {
        return database.articleDao();
    }

    @Provides
    TransactionsDao providestransactionsDao(ArticlesDatabase database) {
        return database.transactionsDao();
    }

    @Provides
    SynchronizationDao providesSynchronizationDao(ArticlesDatabase database) {
        return database.synchronizationDao();
    }

    @Provides
    ArticlesProvidersDao providesArticlesProvidersDao(ArticlesDatabase database) {
        return database.articlesProvidersDao();
    }

    @Provides
    FeedsDao providesFeedsDao(ArticlesDatabase database) {
        return database.feedsDao();
    }

}
