package com.geekorum.ttrss.sync

import com.geekorum.favikonsnoop.AdaptiveDimension
import com.geekorum.favikonsnoop.FaviKonSnoop
import com.geekorum.favikonsnoop.FaviconInfo
import com.geekorum.favikonsnoop.FixedDimension
import com.geekorum.ttrss.data.Feed
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Update icon's url for every feed.
 */
class FeedIconSynchronizer @Inject constructor(
    private val databaseService: DatabaseService,
    private val faviKonSnoop: FaviKonSnoop,
    private val okHttpClient: OkHttpClient,
    private val httpCacher: HttpCacher
) {

    suspend fun synchronizeFeedIcons() {
        databaseService.getFeeds().forEach {
            try {
                updateFeedIcon(it)
            } catch (e: IOException) {
                Timber.w(e,"Unable to update feed icon for feed ${it.title}")
            }
        }
        // now update cache
        databaseService.getFeeds().forEach {
            httpCacher.cacheHttpRequest(it.feedIconUrl)
        }
    }

    private suspend fun updateFeedIcon(feed: Feed) {
        // TODO this doesn't really work with planet or aggregator
        // we should read the feed xml to get the feed's website and then the icon
        val article = databaseService.getRandomArticleFromFeed(feed.id)
        val articleUrl = (article?.link ?: feed.url).toHttpUrlOrNull()

        val favIconInfos = articleUrl?.newBuilder()?.encodedPath("/")?.build()?.let { url ->
            faviKonSnoop.findFavicons(url)
        } ?: emptyList()

        val selectedIcon = selectBestIcon(favIconInfos)

        selectedIcon?.url?.let { url ->
            // if no icon is selected, don't update and let the default url
            databaseService.updateFeedIconUrl(feed.id, url.toString())
        }

    }

    private suspend fun selectBestIcon(favIconInfos: Collection<FaviconInfo>): FaviconInfo? {
        val sortedIcons = favIconInfos.sortedByDescending {
            when (val dimension = it.dimension) {
                is AdaptiveDimension -> Int.MAX_VALUE
                is FixedDimension -> dimension.height * dimension.width
                null -> Int.MIN_VALUE
            }
        }

        return sortedIcons.asSequence().firstOrNull {
            try {
                val request = Request.Builder()
                    .head()
                    .url(it.url)
                    .build()
                okHttpClient.newCall(request).suspendExecute().use { resp ->
                    resp.isSuccessful
                }
            } catch (e: IOException) {
                Timber.w(e, "Unable to get feed icon")
                false
            }
        }
    }


    private suspend fun Call.suspendExecute(): Response {
        return suspendCancellableCoroutine { cont ->
            enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    cont.resume(response)
                }
            })
            cont.invokeOnCancellation { cancel() }
        }
    }

}
