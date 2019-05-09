/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2018 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.settings

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geekorum.ttrss.R
import org.hamcrest.`object`.HasToString.hasToString
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {


    @Test
    fun testThatWeCanShowDependenciesOpenSourcesLicenses() {
        ActivityScenario.launch(SettingsActivity::class.java)

        // click on OSS licenses
        onView(withId(R.id.recycler_view))
            .perform(actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(R.string.oss_license_title)),
                click()))

        // click on androidx
        onData(
            hasToString("org.jetbrains.kotlin:kotlin-stdlib")
        ).perform(click())

        onView(withId(R.id.license_activity_textview))
            .check(matches(withText("http://www.apache.org/licenses/LICENSE-2.0.txt")))
    }
}
