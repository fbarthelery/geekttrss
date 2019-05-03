package com.geekorum.ttrss

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geekorum.ttrss.add_feed.AddFeedActivity
import com.google.common.truth.Truth.assertWithMessage
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

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

            // Check that the title is there
            onView(withId(R.id.title))
                .check(matches(
                    allOf(withText(R.string.activity_add_feed_title),
                        isCompletelyDisplayed()
                    )))

            // click outside
            onView(withId(R.id.touch_outside))
                .perform(click())

            assertWithMessage("The activity should finish on outside touch")
                .that(currentActivity.isFinishing).isTrue()
        }
    }
}
