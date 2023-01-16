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
package com.geekorum.ttrss.manage_feeds.add_feed

import android.accounts.Account
import android.accounts.AccountManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asFlow
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.manage_feeds.add_feed.FeedsFinder.FeedResult
import com.geekorum.ttrss.manage_feeds.add_feed.FeedsFinder.Source.HTML
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Rule
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddFeedViewModelTest {

    @get:Rule
    val archRule = InstantTaskExecutorRule()


    private lateinit var target: AddFeedViewModel
    private lateinit var workManager: WorkManager
    private lateinit var accountManager: AccountManager
    private lateinit var feedsFinder: FeedsFinder
    private val testDispatcher = StandardTestDispatcher()


    @BeforeTest
    fun setup() {
        workManager = mockk(relaxed = true)
        accountManager = mockk(relaxed = true)
        feedsFinder = mockk()
        Dispatchers.setMain(testDispatcher)
        val dispatchers = CoroutineDispatchersProvider(testDispatcher, testDispatcher, testDispatcher)
        target = AddFeedViewModel(dispatchers, feedsFinder, workManager, accountManager)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testThatInitUrlReturnsCorrectFeeds() = runTest {
        val feeds = listOf(
            FeedResult(HTML,"https://google.com", "type", "title"),
            FeedResult(HTML, "https://apple.com", "type2", "title2"))
        coEvery { feedsFinder.findFeeds(any()) }.returns( feeds)

        val result = async {
            target.availableFeeds.asFlow()
                    .first()
        }
        target.initWithUrl("https://some.google.com/".toHttpUrl())

        val expected = feeds
        assertThat(result.await()).isEqualTo(expected)
    }


    @Test
    fun testThatInitUrlWithExceptionReturnsEmptyFeeds() = runTest {
        coEvery { feedsFinder.findFeeds(any()) } throws IOException("No network")

        val result = async {
            target.availableFeeds.asFlow()
                    .first()
        }
        target.initWithUrl("https://some.google.com/".toHttpUrl())

        assertThat(result.await()).isEmpty()
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
    fun testThatSubscribeEmitCompleteEvent() = runTest {
        target.selectedFeed = FeedResult(HTML,"https://google.com", "type", "title")
        target.selectedAccount = mockk()

        val completeEvent = async {
            target.complete.asFlow()
                    .first()
        }
        target.subscribeToFeed()

        assertThat(completeEvent.await()).isNotNull()
    }

    @Test
    fun testThatCancelEmitCompleteEvent() = runTest {
        val completeEvent = async {
            target.complete.asFlow()
                    .first()
        }
        target.cancel()

        assertThat(completeEvent.await()).isNotNull()
    }
}
