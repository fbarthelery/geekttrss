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

import androidx.room.Database
import androidx.room.RoomDatabase
import com.geekorum.ttrss.providers.PurgeArticlesDao

@Database(entities = [Article::class, ArticleFTS::class, Attachment::class,
    Category::class, Feed::class, Transaction::class, AccountInfo::class],
        version = 11)
abstract class ArticlesDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun accountInfoDao(): AccountInfoDao
    abstract fun transactionsDao(): TransactionsDao
    abstract fun synchronizationDao(): SynchronizationDao
    abstract fun articlesProvidersDao(): PurgeArticlesDao
    abstract fun feedsDao(): FeedsDao
    abstract fun manageFeedsDao(): ManageFeedsDao

    companion object {
        const val DATABASE_NAME = "room_articles.db"
    }

    object Tables {
        const val ARTICLES = "articles"
        const val TRANSACTIONS = "transactions"
        const val FEEDS = "feeds"
        const val CATEGORIES = "categories"
    }
}
