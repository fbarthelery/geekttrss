/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2023 by Frederic-Charles Barthelery.
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
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Metadata
import com.geekorum.ttrss.sync.DatabaseService
import com.geekorum.ttrss.webapi.ApiCallException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Update the status of the latest articles of a Feed
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltWorker
class UpdateArticleStatusWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    syncWorkerComponentBuilder: SyncWorkerComponent.Builder,
    private val dispatchers: CoroutineDispatchersProvider
) : FeedArticlesWorker(context, workerParams, syncWorkerComponentBuilder) {

    private val databaseService: DatabaseService = syncWorkerComponent.databaseService

    companion object {
        const val PARAM_FEED_ID = "feed_id"
        const val PARAM_NUMBER_OF_LATEST_ARTICLES_TO_REFRESH = "number_of_articles_to_refresh"

        fun getInputData(account: Account, feedId: Long, numberOfArticlesToRefresh: Int = 500): Data {
            return workDataOf(
                    SyncWorkerFactory.PARAM_ACCOUNT_NAME to account.name,
                    SyncWorkerFactory.PARAM_ACCOUNT_TYPE to account.type,
                    PARAM_FEED_ID to feedId,
                    PARAM_NUMBER_OF_LATEST_ARTICLES_TO_REFRESH to numberOfArticlesToRefresh
            )
        }
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

            while (newRequestChannel.trySend(offset).isSuccess) {
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
                        val articlesWithAttachments = getArticles(feedId, 0, offsetForRequest,
                                showExcerpt = false, showContent = false)
                        val articles = articlesWithAttachments.map { it.article }
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

}
