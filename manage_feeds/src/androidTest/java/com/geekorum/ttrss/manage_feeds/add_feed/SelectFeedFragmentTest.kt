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

import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModel
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
import com.geekorum.geekdroid.dagger.DaggerDelegateSavedStateVMFactory
import com.geekorum.ttrss.manage_feeds.R
import com.geekorum.ttrss.manage_feeds.add_feed.FeedsFinder.FeedResult
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anything
import org.hamcrest.Matchers.not
import org.junit.runner.RunWith
import kotlin.test.BeforeTest
import kotlin.test.Test
import com.google.android.material.R as matR

@RunWith(AndroidJUnit4::class)
class SelectFeedFragmentTest {
    lateinit var framentFactory: FragmentFactory
    lateinit var viewModelFactoryCreator: DaggerDelegateSavedStateVMFactory.Creator
    lateinit var viewModelFactory: DaggerDelegateSavedStateVMFactory
    lateinit var subscribeToFeedViewModel: SubscribeToFeedViewModel
    lateinit var navController: NavController
    lateinit var workManager: WorkManager

    @BeforeTest
    fun setUp() {
        framentFactory = mockk()
        workManager = mockk(relaxed = true)
        subscribeToFeedViewModel = SubscribeToFeedViewModel(mockk(), workManager, mockk())
        navController = mockk(relaxed = true)
        viewModelFactory = mockk()
        every { viewModelFactory.create(any(), any<Class<out ViewModel>>())} returns subscribeToFeedViewModel
        viewModelFactoryCreator = mockk()
        every { viewModelFactoryCreator.create(any(), any()) } returns  viewModelFactory
    }

    @Test
    fun testThatWeCanSelectAFeedWhenThereAreMany() {
        val feeds = listOf(
            FeedResult(FeedsFinder.Source.HTML, "http://my.feed", title = "First"),
            FeedResult(FeedsFinder.Source.HTML, "http://my.second.feed", title = "Second")
        )

        val scenario = launchFragmentInContainer(themeResId = matR.style.Theme_MaterialComponents_Light) {
            SelectFeedFragment(viewModelFactoryCreator, framentFactory)
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

        val scenario = launchFragmentInContainer(themeResId = matR.style.Theme_MaterialComponents_Light) {
            SelectFeedFragment(viewModelFactoryCreator, framentFactory)
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

        val scenario = launchFragmentInContainer(themeResId = matR.style.Theme_MaterialComponents_Light) {
            SelectFeedFragment(viewModelFactoryCreator, framentFactory)
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

        val scenario = launchFragmentInContainer(themeResId = matR.style.Theme_MaterialComponents_Light) {
            SelectFeedFragment(viewModelFactoryCreator, framentFactory)
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
