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
package com.geekorum.ttrss.data.plugins

import androidx.room.withTransaction
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticlesDatabase
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.SynchronizationDao
import com.geekorum.ttrss.data.Transaction
import com.geekorum.ttrss.sync.DatabaseService
import javax.inject.Inject

/**
 * Provides database access for the synchronization process.
 */
class SynchronizationFacade @Inject constructor(
    private val database: ArticlesDatabase,
    private val synchronizationDao: SynchronizationDao
) : DatabaseService {

    override suspend fun updateArticleMetadata(
        id: Long, unread: Boolean, transientUnread: Boolean, starred: Boolean, published: Boolean,
        lastTimeUpdated: Long, isUpdated: Boolean
    ) {
      synchronizationDao.updateArticleMetadata(id, unread, transientUnread, starred, published, lastTimeUpdated, isUpdated)
    }

    override suspend fun getTransactions(): List<Transaction> = synchronizationDao.getAllTransactions()

    override suspend fun deleteTransaction(transaction: Transaction) = synchronizationDao.deleteTransaction(transaction)

    override suspend fun updateArticle(article: Article) = synchronizationDao.updateArticle(article)

    override suspend fun getLatestArticleId(): Long? = synchronizationDao.getLatestArticleId()

    override suspend fun getArticle(id: Long): Article? = synchronizationDao.getArticleById(id)

    override suspend fun insertArticles(articles: List<Article>) = synchronizationDao.insertArticles(articles)

    override suspend fun getCategories(): List<Category> = synchronizationDao.getAllCategories()

    override suspend fun getFeeds(): List<Feed> = synchronizationDao.getAllFeeds()

    override suspend fun deleteCategories(categories: List<Category>) {
        synchronizationDao.deleteCategories(categories)
    }

    override suspend fun insertCategories(categories: List<Category>) {
        synchronizationDao.insertCategories(categories)
    }

    override suspend fun <R> runInTransaction(block: suspend () -> R) {
        database.withTransaction(block)
    }

    override suspend fun insertFeeds(feeds: List<Feed>) {
        synchronizationDao.insertFeeds(feeds)
    }

    override suspend fun deleteFeedsAndArticles(feeds: List<Feed>) {
        synchronizationDao.deleteFeedsAndArticles(feeds)
    }


}
