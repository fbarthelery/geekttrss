/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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

import com.geekorum.favikonsnoop.AdaptiveDimension
import com.geekorum.favikonsnoop.FaviKonSnoop
import com.geekorum.favikonsnoop.FaviconInfo
import com.geekorum.favikonsnoop.FixedDimension
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.Feed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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

private const val NB_LOOKUP_COROUTINES = 5

/**
 * Update icon's url for every feed.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedIconSynchronizer @Inject constructor(
    private val dispatchers: CoroutineDispatchersProvider,
    private val databaseService: DatabaseService,
    private val faviKonSnoop: FaviKonSnoop,
    private val okHttpClient: OkHttpClient,
    private val httpCacher: HttpCacher
) {

    suspend fun synchronizeFeedIcons() {
        coroutineScope {
            val feedChannel = Channel<Feed>()
            databaseService.getFeeds().asFlow()
                .onEach { feedChannel.send(it) }
                .onCompletion { feedChannel.close() }
                .launchIn(this)

            dispatchUpdateFeedIcons(feedChannel)
        }

        // now update cache
        databaseService.getFeeds().forEach {
            httpCacher.cacheHttpRequest(it.feedIconUrl)
        }
    }

    private fun CoroutineScope.dispatchUpdateFeedIcons(feedChannel: Channel<Feed>) {
        repeat(NB_LOOKUP_COROUTINES) {
            launch(dispatchers.io) {
                for (feed in feedChannel) {
                    try {
                        updateFeedIcon(feed)
                    } catch (e: IOException) {
                        Timber.w(e, "Unable to update feed icon for feed ${feed.title}")
                    }
                }
            }
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
