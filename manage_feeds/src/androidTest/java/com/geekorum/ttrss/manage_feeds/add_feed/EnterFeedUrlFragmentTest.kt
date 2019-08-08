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

import android.os.Bundle
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.savedstate.SavedStateRegistryOwner
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import com.geekorum.geekdroid.app.lifecycle.Event
import com.geekorum.geekdroid.dagger.DaggerDelegateSavedStateVMFactory
import com.geekorum.ttrss.manage_feeds.R
import com.google.android.material.textfield.TextInputLayout
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import junit.framework.AssertionFailedError
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.BeforeTest
import kotlin.test.Test
import com.google.android.material.R as matR


@RunWith(AndroidJUnit4::class)
class EnterFeedUrlFragmentTest {
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
        subscribeToFeedViewModel = spyk(SubscribeToFeedViewModel(mockk(), workManager, mockk()))
        navController = mockk(relaxed = true)
        viewModelFactory = mockk()
        every { viewModelFactory.create(any(), any<Class<out ViewModel>>())} returns subscribeToFeedViewModel
        viewModelFactoryCreator = mockk()
        every { viewModelFactoryCreator.create(any(), any()) } returns  viewModelFactory
    }

    @Test
    fun testThatErrorEventSetErrorOnInputField() {
        launchFragmentInContainer(themeResId = matR.style.Theme_MaterialComponents_Light) {
            EnterFeedUrlFragment(viewModelFactoryCreator, framentFactory)
        }

        subscribeToFeedViewModel.submitUrl("invalid url")

        onView(withId(R.id.feed_url))
            .check { view, _ ->
                assert(view is TextInputLayout) { "view $view is not a TextInputLayout" }
                val v = view as TextInputLayout
                assert(v.error.toString() == "Invalid url") { "Error value is not correct" }
            }
    }


    @Test
    fun testThatWhenIOErrorNavigateToDisplayError() {
        val scenario = launchFragmentInContainer(themeResId = matR.style.Theme_MaterialComponents_Light) {
            EnterFeedUrlFragment(viewModelFactoryCreator, framentFactory)
        }

        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            subscribeToFeedViewModel._ioError.value = Event(IOException("error"))
        }

        val expectedNavigation = EnterFeedUrlFragmentDirections.actionDisplayError(
            R.string.fragment_display_error_io_error)
        verify { navController.navigate(eq(expectedNavigation)) }
    }

    @Test
    fun testThatWhenNoFeedsAreFoundNavigateToDisplayError() {
        val scenario = launchFragmentInContainer(themeResId = matR.style.Theme_MaterialComponents_Light) {
            EnterFeedUrlFragment(viewModelFactoryCreator, framentFactory)
        }

        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            subscribeToFeedViewModel._feedsFound.value = emptyList()
        }

        val expectedNavigation = EnterFeedUrlFragmentDirections.actionDisplayError(
            R.string.fragment_display_error_no_feeds_found)
        verify { navController.navigate(eq(expectedNavigation)) }
    }


    @Test
    fun testThatWhenManyFeedsAreFoundNavigateToSelectFeed() {
        val scenario = launchFragmentInContainer(themeResId = matR.style.Theme_MaterialComponents_Light) {
            EnterFeedUrlFragment(viewModelFactoryCreator, framentFactory)
        }

        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            subscribeToFeedViewModel._feedsFound.value = listOf(mockk(), mockk())
        }

        val expectedNavigation = EnterFeedUrlFragmentDirections.actionShowAvailableFeeds()
        verify { navController.navigate(eq(expectedNavigation)) }
    }


    @Test
    fun testThatWhenOnlyOneFeedsFromUrlSubscribe() {
        val scenario = launchFragmentInContainer(themeResId = matR.style.Theme_MaterialComponents_Light) {
            EnterFeedUrlFragment(viewModelFactoryCreator, framentFactory)
        }

        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            subscribeToFeedViewModel._feedsFound.value = listOf(FeedsFinder.FeedResult(FeedsFinder.Source.URL, "url"))
        }

        verify {
            // enqueue() is final and cannot be mocked on api < 28, so we spy the call to subscribeToFeed instead
            // workManager.enqueue(any<WorkRequest>())
            subscribeToFeedViewModel.subscribeToFeed(any())
        }
    }

    @Test
    fun testThatWhenOnlyOneFeedsFromHtmlSubscribe() {
        val scenario = launchFragmentInContainer(themeResId = matR.style.Theme_MaterialComponents_Light) {
            EnterFeedUrlFragment(viewModelFactoryCreator, framentFactory)
        }

        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            subscribeToFeedViewModel._feedsFound.value = listOf(FeedsFinder.FeedResult(FeedsFinder.Source.HTML, "url"))
        }

        val expectedNavigation = EnterFeedUrlFragmentDirections.actionShowAvailableFeeds()
        verify { navController.navigate(eq(expectedNavigation)) }
    }

}


private inline fun assert(value: Boolean, lazyMsg: () -> String = { "Assertion failed" }) {
    if (!value)
        throw AssertionFailedError(lazyMsg())
}
