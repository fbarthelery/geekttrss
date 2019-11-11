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
package com.geekorum.ttrss.sync.workers

import com.geekorum.ttrss.data.AccountInfo
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.Metadata
import com.geekorum.ttrss.data.Transaction
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.network.ServerInfo
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.sync.BackgroundDataUsageManager
import com.geekorum.ttrss.sync.DatabaseService


internal open class MockApiService : ApiService {

    override suspend fun getArticles(feedId: Long, sinceId: Long, offset: Int, showExcerpt: Boolean, showContent: Boolean): List<Article> {
        return if (sinceId == 0L) {
            listOf(Article(id = 1, isUnread = true))
        } else {
            emptyList()
        }
    }

    override suspend fun getArticlesOrderByDateReverse(feedId: Long, sinceId: Long, offset: Int, showExcerpt: Boolean, showContent: Boolean): List<Article> {
        TODO("not implemented")
    }

    override suspend fun getCategories(): List<Category> {
        TODO("not implemented")
    }

    override suspend fun getFeeds(): List<Feed> {
        TODO("not implemented")
    }

    override suspend fun getServerInfo(): ServerInfo {
        TODO("not implemented")
    }

    override suspend fun updateArticleField(id: Long, field: ArticlesContract.Transaction.Field, value: Boolean) {
        TODO("not implemented")
    }

}

internal open class MockDatabaseService: DatabaseService {

    private var accountInfo: AccountInfo? = null

    private val articles = mutableListOf<Article>()
    private val transactions = mutableListOf<Transaction>()

    override suspend fun <R> runInTransaction(block: suspend () -> R) {
        block()
    }

    override suspend fun insertFeeds(feeds: List<Feed>) {
        TODO("not implemented")
    }

    override suspend fun deleteFeedsAndArticles(feeds: List<Feed>) {
        TODO("not implemented")
    }

    override suspend fun getFeeds(): List<Feed> {
        TODO("not implemented")
    }

    override suspend fun updateFeedIconUrl(feedId: Long, url: String) {
        TODO("not implemented")
    }

    override suspend fun insertCategories(categories: List<Category>) {
        TODO("not implemented")
    }

    override suspend fun deleteCategories(categories: List<Category>) {
        TODO("not implemented")
    }

    override suspend fun getCategories(): List<Category> {
        TODO("not implemented")
    }

    override suspend fun getTransactions(): List<Transaction> {
        return transactions.toList()
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactions.remove(transaction)
    }

    internal fun insertTransaction(transaction: Transaction) {
        transactions.add(transaction)
    }

    override suspend fun getArticle(id: Long): Article? {
        return articles.find { it.id == id }
    }

    override suspend fun getRandomArticleFromFeed(feedId: Long): Article? {
        TODO("not implemented")
    }

    override suspend fun insertArticles(articles: List<Article>) {
        this.articles.addAll(articles)
    }

    override suspend fun updateArticle(article: Article) {
        val present = articles.first {
            it.id == article.id
        }
        articles.remove(present)
        articles.add(article)
    }

    override suspend fun getLatestArticleId(): Long? {
        return articles.maxBy { it.id }?.id
    }

    override suspend fun getLatestArticleIdFromFeed(feedId: Long): Long? {
        TODO("not implemented")
    }

    override suspend fun updateArticlesMetadata(metadata: List<Metadata>) {
        metadata.forEach { meta->
            val present = articles.find { article->
                article.id == meta.id
            }
            articles.remove(present)
            present?.copy(isUnread= meta.isUnread)?.let {
                articles.add(it)
            }
        }
    }

    override suspend fun getAccountInfo(username: String, apiUrl: String): AccountInfo? {
        return accountInfo
    }

    override suspend fun insertAccountInfo(accountInfo: AccountInfo) {
        this.accountInfo = accountInfo
    }

}

internal open class MockBackgroundDataUsageManager : BackgroundDataUsageManager(null) {
    override fun canDownloadArticleImages(): Boolean = false
}
