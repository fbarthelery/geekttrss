/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
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
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asFlow
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.htmlparsers.FeedExtractor
import com.geekorum.ttrss.htmlparsers.FeedInformation
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@OptIn(ExperimentalCoroutinesApi::class)
class SubscribeToFeedViewModelTest {
    @get:Rule
    val archRule = InstantTaskExecutorRule()

    private lateinit var subject: SubscribeToFeedViewModel
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var feedExtractor: FeedExtractor
    private lateinit var workManager: WorkManager
    private lateinit var feedsFinder: FeedsFinder
    private val testDispatcher = StandardTestDispatcher()


    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        feedsFinder = mockk()
        okHttpClient = mockk()
        feedExtractor = mockk()
        workManager = mockk(relaxed = true)
        val account: Account = mockk()
        val dispatchers = CoroutineDispatchersProvider(testDispatcher, testDispatcher, testDispatcher)
        subject = SubscribeToFeedViewModel(dispatchers, feedsFinder, workManager, account)

    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testThatWhenUrlIsInvalidInvalidUrlEventIsRaised() = runTest {
        val invalidUrlEvent = async {
            subject.invalidUrlEvent.asFlow().first()
        }
        subject.checkUrl("u")
        assertThat(invalidUrlEvent.await()).isNotNull()
    }

    @Test
    fun testThatWhenUrlIsValidCorrectHttpUrlIsReturned() {
        val result = subject.checkUrl("https://google.com")

        val expected = "https://google.com".toHttpUrl()
        assertThat(result).isEqualTo(expected)
        assertThat(subject.invalidUrlEvent.value).isNull()
    }


    @Test
    fun testThatWhenUrlIsFeedASubscribeJobIsEnqueued() {
        val feedInfo = FeedInformation("https://google.com/", "type")

        subject.subscribeToFeed(feedInfo)

        verify { workManager.enqueue(any<WorkRequest>()) }
    }


}
