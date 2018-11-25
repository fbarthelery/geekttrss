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
package com.geekorum.ttrss.sync

import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.Transaction

/**
 * Database access interface for the synchronization process.
 */
interface DatabaseService {
    fun beginTransaction()
    fun endTransaction()
    fun setTransactionSuccessful()
    fun runInTransaction(block: () -> Any)

    fun insertFeeds(feeds: List<Feed>)
    fun deleteFeedsAndArticles(feeds: List<Feed>)
    fun getFeeds(): List<Feed>

    fun insertCategories(categories: List<Category>)
    fun deleteCategories(categories: List<Category>)
    fun getCategories(): List<Category>

    fun getTransactions(): List<Transaction>
    fun deleteTransaction(transaction: Transaction)

    fun getArticle(id: Long): Article?
    fun insertArticles(articles: List<Article>)
    fun updateArticle(article: Article)
    fun getLatestArticleId(): Long

    fun updateArticleMetadata(
        id: Long, unread: Boolean, transientUnread: Boolean, starred: Boolean,
        published: Boolean, lastTimeUpdated: Long, isUpdated: Boolean)
}