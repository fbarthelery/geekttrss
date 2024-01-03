/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
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

import android.accounts.Account
import android.content.Context
import android.content.OperationApplicationException
import android.os.RemoteException
import android.security.NetworkSecurityPolicy
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleWithAttachments
import com.geekorum.ttrss.data.ArticlesTags
import com.geekorum.ttrss.htmlparsers.ImageUrlExtractor
import com.geekorum.ttrss.sync.BackgroundDataUsageManager
import com.geekorum.ttrss.sync.DatabaseService
import com.geekorum.ttrss.sync.HttpCacher
import com.geekorum.ttrss.webapi.ApiCallException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import timber.log.Timber
import java.io.IOException

/**
 * Collect all the new articles from a feed
 */
@HiltWorker
class CollectNewArticlesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    syncWorkerComponentBuilder: SyncWorkerComponent.Builder,
    private val dispatchers: CoroutineDispatchersProvider,
    private val backgroundDataUsageManager: BackgroundDataUsageManager,
    private val imageUrlExtractor: ImageUrlExtractor,
    private val httpCacher: HttpCacher
) : FeedArticlesWorker(context, workerParams, syncWorkerComponentBuilder) {

    private val databaseService: DatabaseService = syncWorkerComponent.databaseService

    companion object {
        const val PARAM_FEED_ID = "feed_id"

        fun getInputData(account: Account, feedId: Long): Data {
            return workDataOf(
                    SyncWorkerFactory.PARAM_ACCOUNT_NAME to account.name,
                    SyncWorkerFactory.PARAM_ACCOUNT_TYPE to account.type,
                    PARAM_FEED_ID to feedId
            )
        }
    }

    private var feedId: Long = Long.MIN_VALUE

    override suspend fun doWork(): Result = withContext(dispatchers.io) {
        feedId = inputData.getLong(PARAM_FEED_ID, Long.MIN_VALUE)
        if (feedId == Long.MIN_VALUE) {
            Timber.w("No feed_id was specified. Skip work")
            return@withContext Result.success()
        }
        collectNewArticles()
        Result.success()
    }

    @Throws(ApiCallException::class, RemoteException::class, OperationApplicationException::class)
    private suspend fun collectNewArticles() {
        val latestId = getLatestArticleId()

        val articles = getArticles(feedId, latestId, 0)
        val latestReceivedId = articles.maxByOrNull { it.article.id }?.article?.id
        val difference = (latestReceivedId ?: latestId) - latestId
        if (difference > 1000) {
            collectNewArticlesGradually()
        } else {
            collectNewArticlesFully()
        }
    }

    private suspend fun getLatestArticleId(): Long {
        return databaseService.getLatestArticleIdFromFeed(feedId) ?: 0
    }

    /**
     * Attempt to collect all new articles before committing the transaction.
     * Good if there are not too many of them
     */
    @Throws(ApiCallException::class, RemoteException::class, OperationApplicationException::class)
    private suspend fun collectNewArticlesFully() = coroutineScope {
        Timber.i("Collecting new articles fully for feed $feedId")
        val latestId = getLatestArticleId()

        var offset = 0
        var articles = getArticles(feedId, latestId, offset, includeAttachments = true)

        databaseService.runInTransaction {
            while (articles.isNotEmpty()) {
                insertArticles(articles)
                offset += articles.size
                cacheArticlesImages(articles.map { it.article } )
                articles = getArticles(feedId, latestId, offset, includeAttachments = true)
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
        Timber.i("Collecting new articles gradually for feed $feedId")
        val latestId = getLatestArticleId()

        var offset = 0
        var articles = getArticles(feedId, latestId, offset,
            includeAttachments = true, gradually = true)
        while (articles.isNotEmpty()) {
            databaseService.runInTransaction {
                insertArticles(articles)
            }
            cacheArticlesImages(articles.map { it.article })
            offset += articles.size
            articles = getArticles(feedId, latestId, offset, includeAttachments = true, gradually = true)
        }
    }

    private suspend fun insertArticles(articles: List<ArticleWithAttachments>) {
        val articlesOnly = articles.map { it.article }
        databaseService.insertArticles(articlesOnly)
        val articlesTags = articlesOnly.flatMap {
            val tags = it.tags.split(",")
                .map(String::trim)
                .filter(String::isNotEmpty)
            tags.map {tag ->
                ArticlesTags(it.id, tag)
            }
        }
        databaseService.insertArticleTags(articlesTags)
        val attachments = articles.flatMap { it.attachments }
        databaseService.insertAttachments(attachments)
    }

    private fun CoroutineScope.cacheArticlesImages(articles: List<Article>) {
        articles.filter {
            it.isUnread
        }.forEach { cacheArticleImages(it) }
    }

    private fun CoroutineScope.cacheArticleImages(article: Article) {
        if (!backgroundDataUsageManager.canDownloadArticleImages()) {
            return
        }
        imageUrlExtractor.extract(article.content, baseUri = article.link)
                .mapNotNull { it.toHttpUrlOrNull() }
                .filter { it.canBeCache() }
                .forEach {
                    launch(dispatchers.io) {
                        try {
                            httpCacher.cacheHttpRequest(it)
                        } catch (e: IOException) {
                            Timber.w(e, "Unable to cache request $it")
                        }
                    }
                }
    }

    private fun HttpUrl.canBeCache(): Boolean {
        if (scheme == "http" && !NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted(host)) {
            Timber.d("Can't cache $this, clear text traffic not permitted")
            return false
        }
        return true
    }

}
