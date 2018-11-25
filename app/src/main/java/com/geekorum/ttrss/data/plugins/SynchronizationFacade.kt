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
package com.geekorum.ttrss.data.plugins

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

    override fun updateArticleMetadata(
        id: Long, unread: Boolean, transientUnread: Boolean, starred: Boolean, published: Boolean,
        lastTimeUpdated: Long, isUpdated: Boolean
    ) {
      synchronizationDao.updateArticleMetadata(id, unread, transientUnread, starred, published, lastTimeUpdated, isUpdated)
    }

    override fun getTransactions(): List<Transaction> = synchronizationDao.allTransactions

    override fun deleteTransaction(transaction: Transaction) = synchronizationDao.deleteTransaction(transaction)

    override fun updateArticle(article: Article) = synchronizationDao.updateArticle(article)

    override fun getLatestArticleId(): Long = synchronizationDao.latestArticleId

    override fun getArticle(id: Long): Article? = synchronizationDao.getArticleById(id)

    override fun insertArticles(articles: List<Article>) = synchronizationDao.insertArticles(articles)

    override fun getCategories(): List<Category> = synchronizationDao.allCategories

    override fun getFeeds(): List<Feed> = synchronizationDao.allFeeds

    override fun deleteCategories(categories: List<Category>) {
        synchronizationDao.deleteCategories(categories)
    }

    override fun insertCategories(categories: List<Category>) {
        synchronizationDao.insertCategories(categories)
    }

    override fun runInTransaction(block: () -> Any) {
        database.runInTransaction(block)
    }

    override fun beginTransaction() {
        database.beginTransaction()
    }

    override fun endTransaction() {
        database.endTransaction()
    }

    override fun setTransactionSuccessful() {
        database.setTransactionSuccessful()
    }

    override fun insertFeeds(feeds: List<Feed>) {
        synchronizationDao.insertFeeds(feeds)
    }

    override fun deleteFeedsAndArticles(feeds: List<Feed>) {
        synchronizationDao.deleteFeedsAndArticles(feeds)
    }


}