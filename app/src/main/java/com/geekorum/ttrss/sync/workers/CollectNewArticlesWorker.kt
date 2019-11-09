package com.geekorum.ttrss.sync.workers

import android.content.Context
import android.content.OperationApplicationException
import android.os.RemoteException
import android.security.NetworkSecurityPolicy
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.htmlparsers.ImageUrlExtractor
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.sync.ArticleAugmenter
import com.geekorum.ttrss.sync.BackgroundDataUsageManager
import com.geekorum.ttrss.sync.DatabaseService
import com.geekorum.ttrss.sync.HttpCacher
import com.geekorum.ttrss.webapi.ApiCallException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import timber.log.Timber
import java.io.IOException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject


/**
 * Collect all the new articles from a feed
 */
class CollectNewArticlesWorker(
        context: Context,
        workerParams: WorkerParameters,
        private val dispatchers: CoroutineDispatchersProvider,
        private val apiService: ApiService,
        private val databaseService: DatabaseService,
        private val backgroundDataUsageManager: BackgroundDataUsageManager,
        private val imageUrlExtractor: ImageUrlExtractor,
        private val httpCacher: HttpCacher

) : CoroutineWorker(context, workerParams) {

    companion object {
        const val PARAM_FEED_ID = "feed_id"
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
        val latestReceivedId = articles.maxBy { it.id }?.id
        val difference = (latestReceivedId ?: latestId) - latestId
        if (difference > 1000) {
            collectNewArticlesGradually()
        } else {
            collectNewArticlesFully()
        }
    }

    private suspend fun getLatestArticleId(): Long {
        return databaseService.getLatestArticleId() ?: 0
    }

    /**
     * Attempt to collect all new articles before committing the transaction.
     * Good if there are not too many of them
     */
    @Throws(ApiCallException::class, RemoteException::class, OperationApplicationException::class)
    private suspend fun collectNewArticlesFully() {
        Timber.i("Collecting new articles fully for feed $feedId")
        val latestId = getLatestArticleId()

        var offset = 0
        var articles = getArticles(feedId, latestId, offset)

        databaseService.runInTransaction {
            while (articles.isNotEmpty()) {
                insertArticles(articles)
                coroutineScope {
                    cacheArticlesImages(articles)
                }

                offset += articles.size
                articles = getArticles(feedId, latestId, offset)
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
        var articles = getArticles(feedId, latestId, offset, gradually = true)
        while (articles.isNotEmpty()) {
            databaseService.runInTransaction {
                insertArticles(articles)

            }
            cacheArticlesImages(articles)
            offset += articles.size
            articles = getArticles(feedId, latestId, offset, gradually = true)
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

    private fun augmentArticle(article: Article): Article {
        val augmenter = ArticleAugmenter(article)
        article.contentExcerpt = augmenter.getContentExcerpt()
        article.flavorImageUri = augmenter.getFlavorImageUri()
        return article
    }

    private suspend fun insertArticles(articles: List<Article>) {
        databaseService.insertArticles(articles)
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
        imageUrlExtractor.extract(article.content)
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


    class WorkerFactory @Inject constructor(
            syncWorkerComponentBuilder: SyncWorkerComponent.Builder
    ) : SyncWorkerFactory(syncWorkerComponentBuilder) {

        override fun createWorker(
                appContext: Context, workerClassName: String, workerParameters: WorkerParameters
        ): ListenableWorker? {
            if (workerClassName != CollectNewArticlesWorker::class.java.name) {
                return null
            }

            val syncWorkerComponent = createSyncWorkerComponent( workerParameters)
            return with(syncWorkerComponent) {
                CollectNewArticlesWorker(appContext, workerParameters,
                        dispatchers,
                        apiService,
                        databaseService,
                        backgroundDataUsageManager,
                        imageUrlExtractor,
                        httpCacher)
            }
        }
    }

}
