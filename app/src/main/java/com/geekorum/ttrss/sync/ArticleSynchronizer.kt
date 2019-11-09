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

import android.accounts.Account
import android.app.Application
import android.content.OperationApplicationException
import android.content.SharedPreferences
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import androidx.lifecycle.asFlow
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.await
import androidx.work.workDataOf
import com.geekorum.geekdroid.accounts.CancellableSyncAdapter
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Metadata
import com.geekorum.ttrss.htmlparsers.ImageUrlExtractor
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.sync.SyncContract.EXTRA_FEED_ID
import com.geekorum.ttrss.sync.SyncContract.EXTRA_NUMBER_OF_LATEST_ARTICLES_TO_REFRESH
import com.geekorum.ttrss.sync.SyncContract.EXTRA_UPDATE_FEED_ICONS
import com.geekorum.ttrss.sync.workers.CollectNewArticlesWorker
import com.geekorum.ttrss.sync.workers.SendTransactionsWorker
import com.geekorum.ttrss.sync.workers.SyncFeedsIconWorker
import com.geekorum.ttrss.sync.workers.SyncFeedsWorker
import com.geekorum.ttrss.sync.workers.SyncWorkerFactory
import com.geekorum.ttrss.sync.workers.UpdateAccountInfoWorker
import com.geekorum.ttrss.webapi.ApiCallException
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.util.UUID

private const val PREF_LATEST_ARTICLE_SYNCED_ID = "latest_article_sync_id"

/**
 * Synchronize Articles from the network.
 */
class ArticleSynchronizer @AssistedInject constructor(
        application: Application,
        private val dispatchers: CoroutineDispatchersProvider,
        private val apiService: ApiService,
        @Assisted params: Bundle,
        private val account: Account,
        private val accountPreferences: SharedPreferences,
        private val databaseService: DatabaseService
) : CancellableSyncAdapter.CancellableSync() {

    @AssistedInject.Factory
    interface Factory {
        fun create(params: Bundle): ArticleSynchronizer
    }

    private val workManager = WorkManager.getInstance(application)

    private val numberOfLatestArticlesToRefresh = params.getInt(EXTRA_NUMBER_OF_LATEST_ARTICLES_TO_REFRESH, 500)
    private val updateFeedIcons = params.getBoolean(EXTRA_UPDATE_FEED_ICONS, false)
    private val feedId = params.getLong(EXTRA_FEED_ID, ApiService.ALL_ARTICLES_FEED_ID)

    private var syncInfoAndFeedWorkId: UUID? = null
    private var collectNewArticlesJobsIds: List<UUID> = emptyList()

    private suspend fun getLatestArticleId(): Long {
        var result = accountPreferences.getLong(PREF_LATEST_ARTICLE_SYNCED_ID, 0)
        if (result == 0L) {
            result = databaseService.getLatestArticleId() ?: 0
        }
        return result
    }

    override suspend fun sync() {
        try {
            syncInfoAndFeeds()
            collectNewArticles()
            updateArticlesStatus()
        } catch (e: ApiCallException) {
            Timber.w(e, "unable to synchronize articles")
        } catch (e: RemoteException) {
            Timber.e(e, "unable to synchronize articles")
        } catch (e: OperationApplicationException) {
            Timber.e(e, "unable to synchronize articles")
        } catch (e: RuntimeException) {
            Timber.log(if (e is CancellationException) Log.WARN else Log.ERROR,
                e,"unable to synchronize articles")
        }
    }

    private suspend fun syncInfoAndFeeds() {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val inputData = workDataOf(
                SyncWorkerFactory.PARAM_ACCOUNT_NAME to account.name,
                SyncWorkerFactory.PARAM_ACCOUNT_TYPE to account.type
        )

        val updateAccountInfo = OneTimeWorkRequestBuilder<UpdateAccountInfoWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        syncInfoAndFeedWorkId = updateAccountInfo.id

        val sendTransactions = OneTimeWorkRequestBuilder<SendTransactionsWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

        val syncFeeds = OneTimeWorkRequestBuilder<SyncFeedsWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

        val syncFeedsIcons = OneTimeWorkRequestBuilder<SyncFeedsIconWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

        val work = workManager.beginWith(listOf(updateAccountInfo, sendTransactions))
                .then(syncFeeds)
                .apply {
                    if (updateFeedIcons)
                        then(syncFeedsIcons)
                }

        work.enqueue().await()

        work.workInfosLiveData.asFlow()
                .takeWhile { workInfos ->
                    workInfos.any { !it.state.isFinished }
                }
                .collect()
    }

    private suspend fun collectNewArticles() {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val jobRequests = databaseService.getFeeds().map { feed ->
            val inputData = workDataOf(
                    SyncWorkerFactory.PARAM_ACCOUNT_NAME to account.name,
                    SyncWorkerFactory.PARAM_ACCOUNT_TYPE to account.type,
                    CollectNewArticlesWorker.PARAM_FEED_ID to feed.id
            )
            OneTimeWorkRequestBuilder<CollectNewArticlesWorker>()
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .build()
        }

        collectNewArticlesJobsIds = jobRequests.map { it.id }

        workManager.enqueue(jobRequests).await()

        jobRequests.asFlow()
                .onEach { it.waitForCompletion() }
                .collect()
    }

    private suspend fun WorkRequest.waitForCompletion() {
        workManager.getWorkInfoByIdLiveData(id).asFlow()
                .takeWhile {
                    !it.state.isFinished
                }
                .collect()
    }


    override fun onSyncCancelled() {
        super.onSyncCancelled()
        syncInfoAndFeedWorkId?.let {
            workManager.cancelWorkById(it)
        }
        collectNewArticlesJobsIds.forEach {
            workManager.cancelWorkById(it)
        }
        Timber.i("Synchronization was cancelled")
    }


    private suspend fun updateArticleMetadata(articles: List<Article>) {
        val metadatas = articles.map { Metadata.fromArticle(it) }
        databaseService.updateArticlesMetadata(metadatas)
    }

    @Throws(ApiCallException::class)
    private suspend fun updateArticlesStatus() = coroutineScope {
        Timber.i("Updating old articles status")
        val capacity = 5
        val newRequestChannel = Channel<Int>(capacity)
        val orchestratorChannel = Channel<Int>()

        launch {
            var offset = 0
            fun shouldGetMore(): Boolean {
                return if (numberOfLatestArticlesToRefresh < 0) true
                else offset < numberOfLatestArticlesToRefresh
            }

            while (newRequestChannel.offer(offset)) {
                offset += 50
            }

            for (articlesFetched in orchestratorChannel) {
                if (articlesFetched == 0 || !shouldGetMore()) {
                    newRequestChannel.close()
                } else {
                    newRequestChannel.send(offset)
                    offset += 50
                }
            }
        }

        coroutineScope {
            repeat(capacity) {
                launch(dispatchers.io) {
                    for (offsetForRequest in newRequestChannel) {
                        val articles = getArticles(feedId, 0, offsetForRequest, showExcerpt = false,
                            showContent = false)
                        updateArticleMetadata(articles)
                        orchestratorChannel.send(articles.size)
                    }
                }
            }
        }
        orchestratorChannel.close()
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

    private fun augmentArticle(article: Article): Article {
        val augmenter = ArticleAugmenter(article)
        article.contentExcerpt = augmenter.getContentExcerpt()
        article.flavorImageUri = augmenter.getFlavorImageUri()
        return article
    }

}

