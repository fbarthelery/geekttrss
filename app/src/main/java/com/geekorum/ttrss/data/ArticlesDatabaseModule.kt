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
package com.geekorum.ttrss.data

import android.app.Application
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.geekorum.ttrss.data.migrations.ALL_MIGRATIONS
import com.geekorum.ttrss.data.migrations.MigrationFrom1To2
import com.geekorum.ttrss.data.migrations.MigrationFrom2To3
import com.geekorum.ttrss.data.migrations.MigrationFrom3To4
import com.geekorum.ttrss.data.migrations.MigrationFrom4To5
import com.geekorum.ttrss.data.migrations.MigrationFrom5To6
import com.geekorum.ttrss.data.migrations.MigrationFrom6To7
import com.geekorum.ttrss.data.migrations.MigrationFrom7To8
import com.geekorum.ttrss.data.migrations.MigrationFrom8To9
import com.geekorum.ttrss.data.migrations.MigrationFrom9To10
import com.geekorum.ttrss.providers.PurgeArticlesDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Provides some dependencies needed to access the room database.
 */
@Module
object ArticlesDatabaseModule {
    @Provides
    @Singleton
    internal fun providesAppDatabase(application: Application?): ArticlesDatabase {
        return Room.databaseBuilder(application!!, ArticlesDatabase::class.java, ArticlesDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigrationOnDowngrade()
                .addMigrations(*ALL_MIGRATIONS.toTypedArray())
                .build()
    }

    @Provides
    internal fun providesRoomDbHelper(database: ArticlesDatabase): SupportSQLiteOpenHelper {
        return database.openHelper
    }

    @Provides
    internal fun providesArticleDao(database: ArticlesDatabase): ArticleDao {
        return database.articleDao()
    }

    @Provides
    internal fun providesTransactionsDao(database: ArticlesDatabase): TransactionsDao {
        return database.transactionsDao()
    }

    @Provides
    internal fun providesSynchronizationDao(database: ArticlesDatabase): SynchronizationDao {
        return database.synchronizationDao()
    }

    @Provides
    internal fun providesPurgeArticlesDao(database: ArticlesDatabase): PurgeArticlesDao {
        return database.articlesProvidersDao()
    }

    @Provides
    internal fun providesFeedsDao(database: ArticlesDatabase): FeedsDao {
        return database.feedsDao()
    }

    @Provides
    internal fun providesAccountInfoDao(database: ArticlesDatabase): AccountInfoDao {
        return database.accountInfoDao()
    }
}
