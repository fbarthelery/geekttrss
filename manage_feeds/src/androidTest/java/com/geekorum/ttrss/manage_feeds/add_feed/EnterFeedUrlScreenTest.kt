/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
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

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import com.geekorum.geekdroid.app.lifecycle.Event
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.BeforeTest
import kotlin.test.Test


@RunWith(AndroidJUnit4::class)
class EnterFeedUrlScreenTest {
    lateinit var subscribeToFeedViewModel: SubscribeToFeedViewModel
    lateinit var workManager: WorkManager

    @get:Rule
    val composeRule = createComposeRule()

    @BeforeTest
    fun setUp() {
        workManager = mockk(relaxed = true)
        val dispatchers = CoroutineDispatchersProvider(Dispatchers.Main, Dispatchers.IO, Dispatchers.Default)
        subscribeToFeedViewModel = spyk(SubscribeToFeedViewModel(dispatchers, mockk(), workManager, mockk()))
    }

    @Test
    fun testThatErrorEventSetErrorOnInputField() {
        composeRule.setContent {
            EnterFeedUrlScreen(viewModel = subscribeToFeedViewModel,
                navigateToShowAvailableFeeds = {},
                navigateToDisplayError = {},
                finishActivity = {} )
        }

        subscribeToFeedViewModel.submitUrl("invalid url")

        composeRule.onNodeWithText("Invalid http(s) url")
            .assertIsDisplayed()
    }


    @Test
    fun testThatWhenIOErrorNavigateToDisplayError() {
        var navigateToError = false
        composeRule.setContent {
            EnterFeedUrlScreen(viewModel = subscribeToFeedViewModel,
                navigateToShowAvailableFeeds = {},
                navigateToDisplayError = { navigateToError = true },
                finishActivity = {}
            )
        }

        composeRule.runOnUiThread {
            subscribeToFeedViewModel._ioError.value = Event(IOException("error"))
        }

        assertThat(navigateToError).isTrue()
    }

    @Test
    fun testThatWhenNoFeedsAreFoundNavigateToDisplayError() {
        var navigateToError = false
        composeRule.setContent {
            EnterFeedUrlScreen(viewModel = subscribeToFeedViewModel,
                navigateToShowAvailableFeeds = {},
                navigateToDisplayError = { navigateToError = true },
                finishActivity = {}
            )
        }
        composeRule.runOnUiThread {
            subscribeToFeedViewModel._feedsFound.value = emptyList()
        }

        composeRule.waitForIdle()
        assertThat(navigateToError).isTrue()
    }


    @Test
    fun testThatWhenManyFeedsAreFoundNavigateToSelectFeeds() {
        var navigateToFeeds = false
        composeRule.setContent {
            EnterFeedUrlScreen(viewModel = subscribeToFeedViewModel,
                navigateToShowAvailableFeeds = {
                    navigateToFeeds = true
                },
                navigateToDisplayError = { },
                finishActivity = {}
            )
        }
        composeRule.runOnUiThread {
            subscribeToFeedViewModel._feedsFound.value = listOf(mockk(), mockk())
        }

        composeRule.waitForIdle()
        assertThat(navigateToFeeds).isTrue()
    }


    @Test
    fun testThatWhenOnlyOneFeedsFromUrlSubscribe() {
        composeRule.setContent {
            EnterFeedUrlScreen(viewModel = subscribeToFeedViewModel,
                navigateToShowAvailableFeeds = {},
                navigateToDisplayError = { },
                finishActivity = {}
            )
        }
        composeRule.runOnUiThread {
            subscribeToFeedViewModel._feedsFound.value = listOf(FeedsFinder.FeedResult(FeedsFinder.Source.URL, "url"))
        }
        composeRule.waitForIdle()

        verify {
            // enqueue() is final and cannot be mocked on api < 28, so we spy the call to subscribeToFeed instead
            // workManager.enqueue(any<WorkRequest>())
            subscribeToFeedViewModel.subscribeToFeed(any())
        }
    }

    @Test
    fun testThatWhenOnlyOneFeedsFromHtmlNavigateToSelectFeeds() {
        var navigateToFeeds = false
        composeRule.setContent {
            EnterFeedUrlScreen(viewModel = subscribeToFeedViewModel,
                navigateToShowAvailableFeeds = { navigateToFeeds = true},
                navigateToDisplayError = { },
                finishActivity = {}
            )
        }
        composeRule.runOnUiThread {
            subscribeToFeedViewModel._feedsFound.value = listOf(FeedsFinder.FeedResult(FeedsFinder.Source.HTML, "url"))
        }

        composeRule.waitForIdle()
        assertThat(navigateToFeeds).isTrue()
    }
}
