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

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.manage_feeds.add_feed.FeedsFinder.FeedResult
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class SelectFeedScreenTest {
    lateinit var subscribeToFeedViewModel: SubscribeToFeedViewModel
    lateinit var workManager: WorkManager

    @get:Rule
    val composeTestRule = createComposeRule()

    @BeforeTest
    fun setUp() {
        workManager = mockk(relaxed = true)
        val dispatchers = CoroutineDispatchersProvider(Dispatchers.Main, Dispatchers.IO, Dispatchers.Default)
        subscribeToFeedViewModel = SubscribeToFeedViewModel(dispatchers, mockk(), workManager, mockk())
    }

    @Test
    fun testThatWeCanSelectAFeedWhenThereAreMany() {
        val feeds = listOf(
            FeedResult(FeedsFinder.Source.HTML, "http://my.feed", title = "First"),
            FeedResult(FeedsFinder.Source.HTML, "http://my.second.feed", title = "Second")
        )

        composeTestRule.runOnUiThread {
            subscribeToFeedViewModel._feedsFound.value = feeds
        }
        composeTestRule.setContent {
            SelectFeedScreen(subscribeToFeedViewModel)
        }

        composeTestRule.onNodeWithText("We found 2 feeds", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("First")
            .assertIsDisplayed()

        // open drop down and select second item
        composeTestRule.onNodeWithText("First")
            .performClick()
        composeTestRule.onNodeWithText("Second")
            .performClick()

        composeTestRule.onNodeWithText("Second")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("First")
            .assertDoesNotExist()
    }

    @Test
    fun testThatFeedIsVisibleIfOnlyOne() {
        val feeds = listOf(
            FeedResult(FeedsFinder.Source.HTML, "http://my.feed", title = "First")
        )

        composeTestRule.runOnUiThread {
            subscribeToFeedViewModel._feedsFound.value = feeds
        }
        composeTestRule.setContent {
            SelectFeedScreen(subscribeToFeedViewModel)
        }

        composeTestRule.onNodeWithText("We found this feed", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("First")
            .assertIsDisplayed()
    }

    @Test
    fun testThatCorrectMessageIsDiplayedWhenThereIsOnlyOneFeed() {
        val feeds = listOf(
            FeedResult(FeedsFinder.Source.HTML, "http://my.feed", title = "First")
        )

        composeTestRule.runOnUiThread {
            subscribeToFeedViewModel._feedsFound.value = feeds
        }
        composeTestRule.setContent {
            SelectFeedScreen(subscribeToFeedViewModel)
        }

        composeTestRule.onNodeWithText("We found this feed on this website. Do you want to subscribe ?")
            .assertIsDisplayed()
    }

    @Test
    fun testThatCorrectMessageIsDiplayedWhenThereIsManyFeed() {
        val feeds = listOf(
            FeedResult(FeedsFinder.Source.HTML, "http://my.feed", title = "First"),
            FeedResult(FeedsFinder.Source.HTML, "http://my.second.feed", title = "Second")
        )

        composeTestRule.runOnUiThread {
            subscribeToFeedViewModel._feedsFound.value = feeds
        }
        composeTestRule.setContent {
            SelectFeedScreen(subscribeToFeedViewModel)
        }

        composeTestRule.onNodeWithText("We found 2 feeds on this website. Select the one you want to subscribe to.")
            .assertIsDisplayed()
    }


}
