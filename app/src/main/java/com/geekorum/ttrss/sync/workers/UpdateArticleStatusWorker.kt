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

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Metadata
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.sync.DatabaseService
import com.geekorum.ttrss.webapi.ApiCallException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Update the status of the latest articles of a Feed
 */
@UseExperimental(ExperimentalCoroutinesApi::class)
class UpdateArticleStatusWorker(
        context: Context,
        workerParams: WorkerParameters,
        private val dispatchers: CoroutineDispatchersProvider,
        apiService: ApiService,
        private val databaseService: DatabaseService
) : FeedArticlesWorker(context, workerParams, apiService) {

    companion object {
        const val PARAM_FEED_ID = "feed_id"
        const val PARAM_NUMBER_OF_LATEST_ARTICLES_TO_REFRESH = "number_of_articles_to_refresh"
    }

    private var feedId: Long = Long.MIN_VALUE
    private var numberOfLatestArticlesToRefresh: Int = 500

    override suspend fun doWork(): Result = withContext(dispatchers.io) {
        feedId = inputData.getLong(PARAM_FEED_ID, Long.MIN_VALUE)
        numberOfLatestArticlesToRefresh = inputData.getInt(PARAM_NUMBER_OF_LATEST_ARTICLES_TO_REFRESH, 500)
        if (feedId == Long.MIN_VALUE) {
            Timber.w("No feed_id was specified. Skip work")
            return@withContext Result.success()
        }
        updateArticlesStatus()
        Result.success()
    }

    @Throws(ApiCallException::class)
    private suspend fun updateArticlesStatus() = coroutineScope {
        Timber.i("Updating old articles status for feed $feedId")
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
                } else if (!newRequestChannel.isClosedForSend){
                    newRequestChannel.send(offset)
                    offset += 50
                }
            }
        }

        coroutineScope {
            repeat(capacity) {
                launch(dispatchers.io) {
                    for (offsetForRequest in newRequestChannel) {
                        val articles = getArticles(feedId, 0, offsetForRequest,
                                showExcerpt = false, showContent = false)
                        updateArticleMetadata(articles)
                        orchestratorChannel.send(articles.size)
                    }
                }
            }
        }
        orchestratorChannel.close()
    }


    private suspend fun updateArticleMetadata(articles: List<Article>) {
        val metadatas = articles.map { Metadata.fromArticle(it) }
        databaseService.updateArticlesMetadata(metadatas)
    }


    class WorkerFactory @Inject constructor(
            syncWorkerComponentBuilder: SyncWorkerComponent.Builder
    ) : SyncWorkerFactory(syncWorkerComponentBuilder) {

        override fun createWorker(
                appContext: Context, workerClassName: String, workerParameters: WorkerParameters
        ): ListenableWorker? {
            if (workerClassName != UpdateArticleStatusWorker::class.java.name) {
                return null
            }

            val syncWorkerComponent = createSyncWorkerComponent( workerParameters)
            return with(syncWorkerComponent) {
                UpdateArticleStatusWorker(appContext, workerParameters,
                        dispatchers,
                        apiService,
                        databaseService)
            }
        }
    }

}
