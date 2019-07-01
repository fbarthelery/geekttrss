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
package com.geekorum.ttrss.manage_feeds.add_feed

import com.geekorum.ttrss.htmlparsers.FeedExtractor
import com.geekorum.ttrss.htmlparsers.FeedInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import javax.inject.Inject


private val FEED_MIMETYPES = listOf(
    MediaType.get("application/rss+xml"),
    MediaType.get("application/atom+xml"),
    // unfortunately most of web servers are not properly configured. so accept xml
    MediaType.get("application/xml")
)


class FeedsFinder @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val feedExtractor: FeedExtractor
){

    suspend fun findFeeds(url: HttpUrl): Collection<FeedResult> {
        val response = getHttpResponse(url)
        if (!response.isSuccessful) {
            return emptyList()
        }

        return response.use { getResultFromResponse(it) }
    }

    private fun getResultFromResponse(response: Response): List<FeedResult> {
        val body = response.body()
        val contentType = body?.contentType()?.let {
            // remove charset if any
            MediaType.get("${it.type()}/${it.subtype()}")
        }
        return if (contentType in FEED_MIMETYPES) {
            val url = response.request().url().toString()
            listOf(FeedResult(source = Source.URL, href = url,
                type = contentType.toString()))
        } else { // assume html
            getResultFromHtml(body)
        }
    }

    private fun getResultFromHtml(body: ResponseBody?): List<FeedResult> {
        val document = body?.string()
        val feeds = document?.let {
            feedExtractor.extract(it)
        } ?: emptyList()

        return feeds.map { it.toFeedResult() }
    }


    private suspend fun getHttpResponse(url: HttpUrl) = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).get().build()
        okHttpClient.newCall(request).execute()
    }

    data class FeedResult(
        val source: Source,
        val href: String,
        val type: String = "",
        val title: String = ""
    )

    enum class Source {
        URL,
        HTML
    }
}

internal fun FeedsFinder.FeedResult.toFeedInformation() : FeedInformation {
    return FeedInformation(href, type, title)
}

internal fun FeedInformation.toFeedResult() : FeedsFinder.FeedResult {
    return FeedsFinder.FeedResult(FeedsFinder.Source.HTML, href, type, title)
}
