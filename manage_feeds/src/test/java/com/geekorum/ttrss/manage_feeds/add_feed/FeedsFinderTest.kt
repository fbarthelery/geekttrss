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

import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.htmlparsers.FeedExtractor
import com.geekorum.ttrss.htmlparsers.FeedInformation
import com.geekorum.ttrss.manage_feeds.add_feed.FeedsFinder.FeedResult
import com.geekorum.ttrss.manage_feeds.add_feed.FeedsFinder.Source
import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@UseExperimental(ExperimentalCoroutinesApi::class)
class FeedsFinderTest {

    private lateinit var subject: FeedsFinder
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var feedExtractor: FeedExtractor
    private lateinit var testDispatcher: TestCoroutineDispatcher

    @BeforeTest
    fun setup() {
        okHttpClient = mockk()
        feedExtractor = mockk()
        testDispatcher = TestCoroutineDispatcher()
        val dispatchers = CoroutineDispatchersProvider(testDispatcher, testDispatcher, testDispatcher)
        subject = FeedsFinder(dispatchers, okHttpClient, feedExtractor)
    }

    @AfterTest
    fun tearDown() {
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun testThatUrlOfFeedGetResultFromFeed(): Unit = runBlocking {
        val contentType = "application/rss+xml"
        val url = "https://curiosity.com/feeds/atom/daily_curiosity.atom"

        val response = getHttpResponse(url, 200, contentType)
        every { okHttpClient.newCall(any()).execute() } returns response

        val result = subject.findFeeds(url.toHttpUrl())

        val expected = FeedResult(source = Source.URL, href = url, type = contentType )
        Truth.assertThat(result).containsExactly(expected)
        Unit
    }

    @Test
    fun testThatErrorUrlGetNoFeeds(): Unit = runBlocking {
        val url = "https://curiosity.com/feeds/atom/daily_curiosity.atom"
        val response = getHttpResponse(url, 404)

        every { okHttpClient.newCall(any()).execute() } returns response

        val result = subject.findFeeds(url.toHttpUrl())

        Truth.assertThat(result).isEmpty()
        Unit
    }


    @Test
    fun testThatUrlOfHtmlGetResultFromHtml(): Unit = runBlocking {
        val contentType = "text/html"
        val url = "https://curiosity.com/feeds/atom/daily_curiosity.atom"
        val response = getHttpResponse(url, 200, contentType)

        every { okHttpClient.newCall(any()).execute() } returns response

        val feedsFound = listOf(
            FeedInformation("url1", "application/rss+xml", "feed 1"),
            FeedInformation("url2", "application/atom+xml", "feed 2")
        )
        every { feedExtractor.extract(any<String>()) } returns feedsFound

        val expected = feedsFound.map {
            FeedResult(source = Source.HTML, href = it.href, type = it.type, title = it.title )
        }

        val result = subject.findFeeds(url.toHttpUrl())

        Truth.assertThat(result).isEqualTo(expected)
        Unit
    }

    private fun getRequestForUrl(url: String) = Request.Builder()
        .url(url)
        .build()

    private fun getHttpResponse(url: String, code: Int = 200, contentType: String = "text/html") = Response.Builder()
        .request(getRequestForUrl(url))
        .code(code)
        .protocol(Protocol.HTTP_1_1)
        .message("")
        .body(
            "".toResponseBody(contentType.toMediaType())
        ).build()

}
