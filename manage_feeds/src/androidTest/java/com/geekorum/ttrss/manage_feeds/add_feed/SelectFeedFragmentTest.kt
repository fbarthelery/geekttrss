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

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSpinnerText
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.manage_feeds.R
import com.geekorum.ttrss.manage_feeds.add_feed.FeedsFinder.FeedResult
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anything
import org.hamcrest.Matchers.not
import org.junit.runner.RunWith
import kotlin.test.BeforeTest
import kotlin.test.Test
import com.google.android.material.R as matR

@RunWith(AndroidJUnit4::class)
class SelectFeedFragmentTest {
    lateinit var viewModelProvider: ViewModelProvider.Factory
    lateinit var subscribeToFeedViewModel: SubscribeToFeedViewModel
    lateinit var navController: NavController
    lateinit var workManager: WorkManager

    @BeforeTest
    fun setUp() {
        workManager = mockk(relaxed = true)
        val dispatchers = CoroutineDispatchersProvider(Dispatchers.Main, Dispatchers.IO, Dispatchers.Default)
        subscribeToFeedViewModel = SubscribeToFeedViewModel(dispatchers, mockk(), workManager, mockk())
        navController = mockk(relaxed = true)
        viewModelProvider = createViewModelFactoryFor(subscribeToFeedViewModel)
    }

    @Test
    fun testThatWeCanSelectAFeedWhenThereAreMany() {
        val feeds = listOf(
            FeedResult(FeedsFinder.Source.HTML, "http://my.feed", title = "First"),
            FeedResult(FeedsFinder.Source.HTML, "http://my.second.feed", title = "Second")
        )

        val scenario = launchFragmentInViewModelProvidedActivity(
            viewModelProviderFactory = viewModelProvider,
            themeResId = matR.style.Theme_MaterialComponents_Light) {
            SelectFeedFragment()
        }

        scenario.onFragment {
            subscribeToFeedViewModel._feedsFound.value = feeds
        }

        onView(withId(R.id.available_feeds_single))
            .check(matches(not(isDisplayed())))

        onView(withId(R.id.available_feeds))
            .check(matches(isDisplayed()))
            .perform(click())

        Espresso.onData(anything())
            .atPosition(1)
            .perform(click())

        onView(withId(R.id.available_feeds))
            .check(matches(withSpinnerText(feeds[1].toString())))
    }

    @Test
    fun testThatFeedIsVisibleIfOnlyOne() {
        val feeds = listOf(
            FeedResult(FeedsFinder.Source.HTML, "http://my.feed", title = "First")
        )

        val scenario = launchFragmentInViewModelProvidedActivity(
            viewModelProviderFactory = viewModelProvider,
            themeResId = matR.style.Theme_MaterialComponents_Light) {
            SelectFeedFragment()
        }

        scenario.onFragment {
            subscribeToFeedViewModel._feedsFound.value = feeds
        }

        onView(withId(R.id.available_feeds_single))
            .check(matches(allOf(
                isDisplayed(),
                withText(feeds[0].title)
            )))

        onView(withId(R.id.available_feeds))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun testThatCorrectMessageIsDiplayedWhenThereIsOnlyOneFeed() {
        val feeds = listOf(
            FeedResult(FeedsFinder.Source.HTML, "http://my.feed", title = "First")
        )

        val scenario = launchFragmentInViewModelProvidedActivity(
            viewModelProviderFactory = viewModelProvider,
            themeResId = matR.style.Theme_MaterialComponents_Light) {
            SelectFeedFragment()
        }

        var expectedMessage = ""
        scenario.onFragment {
            subscribeToFeedViewModel._feedsFound.value = feeds
            expectedMessage = it.resources.getQuantityString(R.plurals.fragment_select_feed_label, feeds.size, feeds.size)
        }


        onView(withId(R.id.lbl_select))
            .check(matches(allOf(
                isDisplayed(),
                withText(expectedMessage)
            )))

    }

    @Test
    fun testThatCorrectMessageIsDiplayedWhenThereIsManyFeed() {
        val feeds = listOf(
            FeedResult(FeedsFinder.Source.HTML, "http://my.feed", title = "First"),
            FeedResult(FeedsFinder.Source.HTML, "http://my.second.feed", title = "Second")
        )

        val scenario = launchFragmentInViewModelProvidedActivity(
            viewModelProviderFactory = viewModelProvider,
            themeResId = matR.style.Theme_MaterialComponents_Light) {
            SelectFeedFragment()
        }

        var expectedMessage = ""
        scenario.onFragment {
            subscribeToFeedViewModel._feedsFound.value = feeds
            expectedMessage = it.resources.getQuantityString(R.plurals.fragment_select_feed_label, feeds.size, feeds.size)
        }


        onView(withId(R.id.lbl_select))
            .check(matches(allOf(
                isDisplayed(),
                withText(expectedMessage)
            )))

    }


}
