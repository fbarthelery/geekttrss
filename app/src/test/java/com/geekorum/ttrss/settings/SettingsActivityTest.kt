/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2020 by Frederic-Charles Barthelery.
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

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.truth.content.IntentSubject
import com.geekorum.ttrss.R
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.reflect.jvm.jvmName
import kotlin.test.Test

/*
 * For some reasons, when it's a Robolectric test the activity doesn't launch (or synchronization is bad) so we only check
 * if the correct intent is fired. See androidTest/com.geekorum.test.SettingsActivityTest for a more complete test.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28]) // for now we don't do robolectric on sdk 29 (needs to run on java9)
class SettingsActivityTest {

    @get:Rule
    val activityRule = IntentsTestRule(SettingsActivity::class.java)


    @Test
    fun testThatWeCanShowDependenciesOpenSourcesLicenses() {
        // click on OSS licenses
        onView(withId(R.id.recycler_view))
            .perform(actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(R.string.oss_license_title)),
                click()))

        val intents = Intents.getIntents()
        assertThat(intents).hasSize(1)
        val intent = intents.first()

        val applicationContext = ApplicationProvider.getApplicationContext<Context>()
        IntentSubject.assertThat(intent).hasComponent(applicationContext.packageName, OssLicensesMenuActivity::class.jvmName)

    }
}
