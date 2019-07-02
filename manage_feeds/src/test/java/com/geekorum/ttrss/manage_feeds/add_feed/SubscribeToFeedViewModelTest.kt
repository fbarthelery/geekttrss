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
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.geekorum.geekdroid.app.lifecycle.Event
import com.geekorum.ttrss.htmlparsers.FeedExtractor
import com.geekorum.ttrss.htmlparsers.FeedInformation
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.Test
import kotlin.test.BeforeTest

class SubscribeToFeedViewModelTest {
    @get:Rule
    val archRule = InstantTaskExecutorRule()

    private lateinit var subject: SubscribeToFeedViewModel
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var feedExtractor: FeedExtractor
    private lateinit var workManager: WorkManager
    private lateinit var feedsFinder: FeedsFinder

    @BeforeTest
    fun setup() {
        feedsFinder = mockk()
        okHttpClient = mockk()
        feedExtractor = mockk()
        workManager = mockk(relaxed = true)
        val account: Account = mockk()
        subject = SubscribeToFeedViewModel(feedsFinder, workManager, account)
    }


    @Test
    fun testThatWhenUrlIsInvalidInvalidUrlEventIsRaised() {
        val observer: Observer<Event<String>> = mockObserver()
        subject.invalidUrlEvent.observeForever(observer)
        subject.checkUrl("u")
        verify { observer.onChanged(any()) }
    }

    @Test
    fun testThatWhenUrlIsValidCorrectHttpUrlIsReturned() {
        val observer: Observer<Event<String>> = mockObserver()
        subject.invalidUrlEvent.observeForever(observer)
        val result = subject.checkUrl("https://google.com")
        val expected = HttpUrl.parse("https://google.com")
        assertThat(result).isEqualTo(expected)
        verify(inverse = true) { observer.onChanged(any()) }
    }


    @Test
    fun testThatWhenUrlIsFeedASubscribeJobIsEnqueued() {
        val feedInfo = FeedInformation("https://google.com/", "type")

        subject.subscribeToFeed(feedInfo)

        verify { workManager.enqueue(any<WorkRequest>()) }
    }


}

private inline fun <reified T : Observer<K>, reified K : Any> mockObserver(): T {
    val observer: T = mockk()
    every { observer.onChanged(any()) } just Runs
    return observer
}
