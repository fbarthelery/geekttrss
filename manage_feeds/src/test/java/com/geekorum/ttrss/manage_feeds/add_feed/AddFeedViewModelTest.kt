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

import android.accounts.Account
import android.accounts.AccountManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.manage_feeds.add_feed.FeedsFinder.FeedResult
import com.geekorum.ttrss.manage_feeds.add_feed.FeedsFinder.Source.HTML
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import org.junit.Rule
import java.io.IOException
import java.util.Collections
import kotlin.test.BeforeTest
import kotlin.test.Test


class AddFeedViewModelTest {

    @get:Rule
    val archRule = InstantTaskExecutorRule()


    private lateinit var target: AddFeedViewModel
    private lateinit var workManager: WorkManager
    private lateinit var accountManager: AccountManager
    private lateinit var feedsFinder: FeedsFinder


    @BeforeTest
    fun setup() {
        workManager = mockk(relaxed = true)
        accountManager = mockk(relaxed = true)
        feedsFinder = mockk()
        target = AddFeedViewModel(feedsFinder, workManager, accountManager)
    }

    @Test
    fun testThatInitUrlReturnsCorrectFeeds() {
        val feeds = listOf(
            FeedResult(HTML,"https://google.com", "type", "title"),
            FeedResult(HTML, "https://apple.com", "type2", "title2"))
        coEvery { feedsFinder.findFeeds(any()) }.returns( feeds)

        var result: Collection<FeedResult>? = null
        val observer = Observer<Collection<FeedResult>> {
            result = it
        }
        target.availableFeeds.observeForever(observer)
        runBlocking {
            target.initWithUrl(HttpUrl.parse("https://some.google.com/")!!)
        }

        val expected = feeds
        assertThat(result).isEqualTo(expected)
    }


    @Test
    fun testThatInitUrlWithExceptionReturnsEmptyFeeds() {
        coEvery { feedsFinder.findFeeds(any()) } throws IOException("No network")

        var result: Collection<FeedResult>? = null
        val observer = Observer<Collection<FeedResult>> {
            result = it
        }
        target.availableFeeds.observeForever(observer)
        runBlocking {
            target.initWithUrl(HttpUrl.parse("https://some.google.com/")!!)
        }

        assertThat(result).isEmpty()
    }

    @Test
    fun testThatSubscribeFeedEnqueueAWorkRequest() {
        val account: Account = mockk()
        target.selectedFeed = FeedResult(HTML,"https://google.com", "type", "title")
        target.selectedAccount = account

        target.subscribeToFeed()

        verify { workManager.enqueue(any<WorkRequest>()) }
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
        target.selectedFeed = FeedResult(HTML,"https://google.com", "type", "title")
        target.selectedAccount = mockk()

        var called = false
        val observer = EventObserver<Any> {
            called = true
        }
        target.complete.observeForever(observer)
        target.subscribeToFeed()

        assertThat(called).isTrue()
    }

    @Test
    fun testThatCancelEmitCompleteEvent() {
        var called = false
        val observer = EventObserver<Any> {
            called = true
        }
        target.complete.observeForever(observer)
        target.cancel()

        assertThat(called).isTrue()
    }
}
