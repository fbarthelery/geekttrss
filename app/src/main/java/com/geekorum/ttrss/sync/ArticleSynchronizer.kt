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
package com.geekorum.ttrss.sync

import android.content.OperationApplicationException
import android.content.SharedPreferences
import android.os.Bundle
import android.os.RemoteException
import android.security.NetworkSecurityPolicy
import com.geekorum.geekdroid.accounts.CancellableSyncAdapter
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.html.ImageUrlExtractor
import com.geekorum.ttrss.network.ApiCallException
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.sync.SyncContract.EXTRA_FEED_ID
import com.geekorum.ttrss.sync.SyncContract.EXTRA_NUMBER_OF_LATEST_ARTICLES_TO_REFRESH
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import okhttp3.HttpUrl
import timber.log.Timber
import java.io.IOException

/**
 * Synchronize Articles from the network.
 */
class ArticleSynchronizer @AssistedInject constructor(
    private val apiService: ApiService,
    @Assisted params: Bundle,
    private val backgroundDataUsageManager: BackgroundDataUsageManager,
    private val accountPreferences: SharedPreferences,
    private val databaseService: DatabaseService,
    private val httpCacher: HttpCacher,
    private val imageUrlExtractor: ImageUrlExtractor
) : CancellableSyncAdapter.CancellableSync() {

    @AssistedInject.Factory
    interface Factory {
        fun create(params: Bundle): ArticleSynchronizer
    }

    private val PREF_LATEST_ARTICLE_SYNCED_ID = "latest_article_sync_id"

    private val numberOfLatestArticlesToRefresh = params.getInt(EXTRA_NUMBER_OF_LATEST_ARTICLES_TO_REFRESH, 500)
    private val feedId = params.getLong(EXTRA_FEED_ID, ApiService.ALL_ARTICLES_FEED_ID)

    private suspend fun getLatestArticleId(): Long {
        var result = accountPreferences.getLong(PREF_LATEST_ARTICLE_SYNCED_ID, 0)
        if (result == 0L) {
            result = databaseService.getLatestArticleId() ?: 0
        }
        return result
    }

    override suspend fun sync() {
        try {
            sendTransactions()
            synchronizeFeeds()
            collectNewArticles()
            updateArticlesStatus()
        } catch (e: ApiCallException) {
            Timber.e(e, "unable to synchronize articles")
        } catch (e: RemoteException) {
            Timber.e(e, "unable to synchronize articles")
        } catch (e: OperationApplicationException) {
            Timber.e(e, "unable to synchronize articles")
        } catch (e: RuntimeException) {
            Timber.e(e, "unable to synchronize articles")
        }
    }

    @Throws(ApiCallException::class, RemoteException::class, OperationApplicationException::class)
    private suspend fun sendTransactions() {
        val transactions = databaseService.getTransactions()
        Timber.i("Sending ${transactions.size} pending transactions")
        transactions.forEach { transaction ->
            databaseService.runInTransaction {
                val field = ArticlesContract.Transaction.Field.valueOf(transaction.field)
                val article = checkNotNull(databaseService.getArticle(transaction.articleId)) {
                    "article ${transaction.articleId} does not exists"
                }
                val value = transaction.value
                updateArticleField(transaction.articleId, field, value)
                when (field) {
                    ArticlesContract.Transaction.Field.PUBLISHED -> article.isPublished = value
                    ArticlesContract.Transaction.Field.UNREAD -> {
                        article.isUnread = value
                        article.isTransientUnread = value
                    }
                    ArticlesContract.Transaction.Field.STARRED -> article.isStarred = value
                    else -> throw IllegalArgumentException("Unknown field type")
                }
                databaseService.updateArticle(article)
                databaseService.deleteTransaction(transaction)
            }
            yield()
        }
    }

    @Throws(ApiCallException::class, RemoteException::class, OperationApplicationException::class)
    private suspend fun collectNewArticles() {
        var latestId = getLatestArticleId()
        if (latestId < 0) {
            latestId = 0
        }

        val articles = getArticles(ApiService.ALL_ARTICLES_FEED_ID, latestId, 0)
        val latestReceivedId = articles.sortedBy { it.id }.lastOrNull()?.id
        val difference = (latestReceivedId ?: latestId) - latestId
        if (difference > 1000) {
            collectNewArticlesGradually()
        } else {
            collectNewArticlesFully()
        }
    }

    /**
     * Attempt to collect all new articles before committing the transaction.
     * Good if there are not too many of them
     */
    @Throws(ApiCallException::class, RemoteException::class, OperationApplicationException::class)
    private suspend fun collectNewArticlesFully() {
        Timber.i("Collecting new articles fully")
        var latestId = getLatestArticleId()
        if (latestId < 0) {
            latestId = 0
        }

        val feedsIds = databaseService.getFeeds().map { it.id }
        var offset = 0
        var articles = getArticles(ApiService.ALL_ARTICLES_FEED_ID, latestId, offset)

        // we need to make every operations from the transaction in the same thread
        runBlocking {
            try {
                databaseService.beginTransaction()
                while (articles.isNotEmpty()) {
                    insertArticles(articles, feedsIds)
                    cacheArticlesImages(articles)
                    val latestIdInserted = articles.maxBy { it.id }?.id ?: -1
                    updateLatestArticleSyncedId(latestIdInserted)

                    offset += articles.size
                    articles = getArticles(ApiService.ALL_ARTICLES_FEED_ID, latestId, offset)
                }
                databaseService.setTransactionSuccessful()
            } finally {
                databaseService.endTransaction()
            }
        }
    }

    /**
     * Attempt to collect all new articles gradually.
     * The transaction will be commit for each network request.
     * This allows to save them but they must be collected from id 0 to latest, in order to
     * maintain the contract around getLatestArticleId()
     * As the web api doesn't allow to sort by id, we sort by reverse date which is the closest approximation
     */
    @Throws(ApiCallException::class, RemoteException::class, OperationApplicationException::class)
    private suspend fun collectNewArticlesGradually() = coroutineScope {
        Timber.i("Collecting new articles gradually")
        var latestId = getLatestArticleId()
        if (latestId < 0) {
            latestId = 0
        }

        val feedsIds = databaseService.getFeeds().map { it.id }
        var offset = 0
        var articles = getArticles(ApiService.ALL_ARTICLES_FEED_ID, latestId, offset, gradually = true)
        while (articles.isNotEmpty()) {
            databaseService.runInTransaction {
                insertArticles(articles, feedsIds)
                val latestIdInserted = articles.maxBy { it.id }?.id ?: -1
                updateLatestArticleSyncedId(latestIdInserted)
            }
            cacheArticlesImages(articles)
            offset += articles.size
            articles = getArticles(ApiService.ALL_ARTICLES_FEED_ID, latestId, offset, gradually = true)
        }
    }

    private suspend fun insertArticles(articles: List<Article>, feedsIds: List<Long>) {
        // it is possible to receive articles not associated to a feed (feedId==0)
        val toInsert = articles.filter { it.feedId in feedsIds }
        databaseService.insertArticles(toInsert)
    }

    private fun updateLatestArticleSyncedId(latestId: Long) {
        require(latestId >= 0) { "latest article id collected is<0" }
        accountPreferences.edit().putLong(PREF_LATEST_ARTICLE_SYNCED_ID, latestId).apply()
    }

    private fun CoroutineScope.cacheArticlesImages(articles: List<Article>) {
        articles.filter {
            it.isUnread
        }.forEach { cacheArticleImages(it) }
    }

    private fun CoroutineScope.cacheArticleImages(article: Article) = launch(Dispatchers.IO) {
        if (!backgroundDataUsageManager.canDownloadArticleImages()) {
            return@launch
        }
        imageUrlExtractor.extract(article.content)
            .forEach {
                launch {
                    try {
                        cacheHttpRequest(it)
                    } catch (e: IOException) {
                        Timber.w(e,"Unable to cache request $it")
                    }
                }
            }
    }

    @Throws(IOException::class)
    private fun cacheHttpRequest(it: HttpUrl) {
        if (it.canBeCache()) {
            httpCacher.cacheHttpRequest(it)
        }
    }

    private fun HttpUrl.canBeCache(): Boolean {
        if (scheme() == "http" && !NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted(host())) {
            Timber.d("Can't cache $this, clear text traffic not permitted")
            return false
        }
        return true
    }

    private suspend fun updateArticleMetadata(articles: List<Article>) {
        databaseService.runInTransaction {
            articles.forEach {
                databaseService.updateArticleMetadata(it.id, it.isUnread, it.isUnread, it.isStarred,
                    it.isPublished, it.lastTimeUpdate, it.isUpdated)
            }
        }
    }

    @Throws(ApiCallException::class)
    private suspend fun updateArticlesStatus() {
        Timber.i("Updating old articles status")
        var offset = 0
        fun shouldGetMore(): Boolean {
            return if (numberOfLatestArticlesToRefresh < 0) true
            else offset < numberOfLatestArticlesToRefresh
        }

        var articles = getArticles(feedId, 0, offset, showExcerpt = false, showContent = false)
        while (articles.isNotEmpty() && shouldGetMore()) {
            updateArticleMetadata(articles)
            offset += articles.size
            articles = getArticles(feedId, 0, offset, showExcerpt = false, showContent = false)
        }
    }

    @Throws(ApiCallException::class)
    private suspend fun getArticles(
        feedId: Long, sinceId: Long, offset: Int,
        showExcerpt: Boolean = true,
        showContent: Boolean = true,
        gradually: Boolean = false
    ): List<Article> {
        val articles = if (gradually) {
            apiService.getArticlesOrderByDateReverse(feedId,
                sinceId, offset, showExcerpt, showContent)
        } else {
            apiService.getArticles(feedId,
                sinceId, offset, showExcerpt, showContent)
        }

        if (showContent) {
            articles.forEach {
                augmentArticle(it)
            }
        }
        return articles
    }

    private fun augmentArticle(article: com.geekorum.ttrss.data.Article): com.geekorum.ttrss.data.Article {
        val augmenter = article.createAugmenter()
        article.contentExcerpt = augmenter.getContentExcerpt()
        article.flavorImageUri = augmenter.getFlavorImageUri()
        return article
    }

    @Throws(ApiCallException::class, RemoteException::class, OperationApplicationException::class)
    private suspend fun synchronizeFeeds() {
        Timber.i("Synchronizing feeds list")
        val categories = apiService.getCategories()
        val feeds = apiService.getFeeds()
        databaseService.runInTransaction {
            insertCategories(categories)
            deleteOldCategories(categories)
            insertFeeds(feeds)
            deleteOldFeeds(feeds)
        }
    }

    @Throws(RemoteException::class, OperationApplicationException::class)
    private suspend fun deleteOldFeeds(feeds: List<com.geekorum.ttrss.data.Feed>) {
        val feedsIds: List<Long> = feeds.map { it.id }
        val toBeDelete = databaseService.getFeeds().filter { it.id !in feedsIds }

        databaseService.deleteFeedsAndArticles(toBeDelete)
    }

    @Throws(RemoteException::class, OperationApplicationException::class)
    private suspend fun deleteOldCategories(categories: List<Category>) {
        val feedCategoriesId: List<Long> = categories.map { category -> category.id }
        val toDelete = databaseService.getCategories().filter { it.id !in feedCategoriesId }

        databaseService.deleteCategories(toDelete)
    }

    @Throws(RemoteException::class, OperationApplicationException::class)
    private suspend fun insertFeeds(feeds: List<com.geekorum.ttrss.data.Feed>) {
        databaseService.insertFeeds(feeds)
    }

    @Throws(RemoteException::class, OperationApplicationException::class)
    private suspend fun insertCategories(categories: List<Category>) {
        // remove virtual categories
        val realCategories = categories.filter { it.id >= 0 }
        databaseService.insertCategories(realCategories)
    }

    @Throws(ApiCallException::class)
    private fun updateArticleField(id: Long, field: ArticlesContract.Transaction.Field, value: Boolean) = runBlocking {
        apiService.updateArticleField(id, field, value)
    }

}

