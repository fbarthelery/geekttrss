package com.geekorum.ttrss.sync.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Metadata
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.sync.DatabaseService
import com.geekorum.ttrss.webapi.ApiCallException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Update the status of the latest articles of a Feed
 */
class UpdateArticleStatusWorker(
        context: Context,
        workerParams: WorkerParameters,
        private val dispatchers: CoroutineDispatchersProvider,
        private val apiService: ApiService,
        private val databaseService: DatabaseService
) : CoroutineWorker(context, workerParams) {

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
                        val articles = getArticles(feedId, 0, offsetForRequest, showExcerpt = false)
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
            gradually: Boolean = false
    ): List<Article> {
        val showContent: Boolean = false

        return if (gradually) {
            apiService.getArticlesOrderByDateReverse(feedId,
                    sinceId, offset, showExcerpt, showContent)
        } else {
            apiService.getArticles(feedId,
                    sinceId, offset, showExcerpt, showContent)
        }
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
