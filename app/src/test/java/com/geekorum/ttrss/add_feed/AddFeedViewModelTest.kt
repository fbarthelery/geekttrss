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
package com.geekorum.ttrss.add_feed

import android.accounts.Account
import android.accounts.AccountManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.BackgroundJobManager
import com.geekorum.ttrss.html.FeedExtractor
import com.geekorum.ttrss.html.FeedInformation
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.junit.Rule
import java.io.IOException
import kotlin.test.BeforeTest
import kotlin.test.Test


class AddFeedViewModelTest {

    @get:Rule
    val archRule = InstantTaskExecutorRule()


    private lateinit var target: AddFeedViewModel
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var feedExtractor: FeedExtractor
    private lateinit var backgroundJobManager: BackgroundJobManager
    private lateinit var accountManager: AccountManager

    @BeforeTest
    fun setup() {
        okHttpClient = mockk()
        feedExtractor = mockk()
        backgroundJobManager = mockk()
        accountManager = mockk(relaxed = true)
        target = AddFeedViewModel(feedExtractor, okHttpClient, backgroundJobManager, accountManager)
    }

    @Test
    fun testThatInitDocumentReturnsCorrectFeeds() {
        val feeds = listOf(
            FeedInformation(HttpUrl.parse("https://google.com")!!, "type", "title"),
            FeedInformation(HttpUrl.parse("https://apple.com")!!, "type2", "title2"))
        every { feedExtractor.extract(any<String>()) }.returns( feeds)

        var result: List<FeedInformation>? = null
        val observer = Observer<List<FeedInformation>> {
            result = it
        }
        target.availableFeeds.observeForever(observer)
        target.init("long html document")

        assertThat(result).isEqualTo(feeds)
    }

    @Test
    fun testThatInitUrlReturnsCorrectFeeds() {
        val feeds = listOf(
            FeedInformation(HttpUrl.parse("https://google.com")!!, "type", "title"),
            FeedInformation(HttpUrl.parse("https://apple.com")!!, "type2", "title2"))
        every { feedExtractor.extract(any<String>()) }.returns( feeds)
        coEvery { okHttpClient.newCall(any()).execute().body()!!.string() } returns("anything")
        coEvery { okHttpClient.newCall(any()).execute().close() } just Runs

        var result: List<FeedInformation>? = null
        val observer = Observer<List<FeedInformation>> {
            result = it
        }
        target.availableFeeds.observeForever(observer)
        runBlocking {
            target.initWithUrl(HttpUrl.parse("https://some.google.com/")!!)
        }

        assertThat(result).isEqualTo(feeds)
    }


    @Test
    fun testThatInitUrlWithExceptionReturnsEmptyFeeds() {
        coEvery { okHttpClient.newCall(any()).execute() } throws IOException("No network")

        var result: List<FeedInformation>? = null
        val observer = Observer<List<FeedInformation>> {
            result = it
        }
        target.availableFeeds.observeForever(observer)
        runBlocking {
            target.initWithUrl(HttpUrl.parse("https://some.google.com/")!!)
        }

        assertThat(result).isEmpty()
    }

    @Test
    fun testThatSubscribeFeedCallsBackgroundJobManager() {
        val account: Account = mockk()
        target.selectedFeed = FeedInformation(HttpUrl.parse("https://google.com/")!!, "type", "title")
        target.selectedAccount = account
        every { backgroundJobManager.subscribeToFeed(any(), any(), any(), any(), any()) } just Runs

        target.subscribeToFeed()

        verify { backgroundJobManager.subscribeToFeed(any(), "https://google.com/", any(), any(), any()) }
    }

    @Test
    fun testThatCanSubscribeOnceSelectionIsDone() {
        assertThat(target.canSubscribe.get()).isFalse()

        target.selectedAccount = mockk()
        target.selectedFeed = mockk()

        assertThat(target.canSubscribe.get()).isTrue()
    }


    @Test
    fun testThatSubscribeEmitCompleteEvent() {
        target.selectedFeed = FeedInformation(HttpUrl.parse("https://google.com/")!!, "type", "title")
        target.selectedAccount = mockk()
        every { backgroundJobManager.subscribeToFeed(any(), any(), any(), any(), any()) } just Runs

        var called = false
        val observer = EventObserver<Any> {
            called = true
        }
        target.complete.observeForever(observer)
        target.subscribeToFeed()

        assertThat(called).isTrue()
    }

    @Test
    fun testThatCanelEmitCompleteEvent() {
        var called = false
        val observer = EventObserver<Any> {
            called = true
        }
        target.complete.observeForever(observer)
        target.cancel()

        assertThat(called).isTrue()
    }
}
