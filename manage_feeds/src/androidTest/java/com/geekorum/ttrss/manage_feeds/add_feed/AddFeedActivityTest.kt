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

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geekorum.ttrss.manage_feeds.R
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import org.junit.runner.RunWith
import com.geekorum.geekdroid.R as geekdroidR

@RunWith(AndroidJUnit4::class)
class AddFeedActivityTest {

    @Test
    fun testThatWeCanLaunchTheActivity() {
        val scenario: ActivityScenario<AddFeedActivity> = launchActivity()
        scenario.use {
            // remember the activity. We should not but we are explicitely testing that it is finishing
            lateinit var currentActivity: Activity
            scenario.onActivity {
                currentActivity = it
            }

            // Check that the toolbar is there
            onView(withId(R.id.toolbar))
                .check(matches(isCompletelyDisplayed()))

            // click outside
            onView(withId(geekdroidR.id.touch_outside))
                .perform(click())

            assertWithMessage("The activity should finish on outside touch")
                .that(currentActivity.isFinishing).isTrue()
        }
    }
}
