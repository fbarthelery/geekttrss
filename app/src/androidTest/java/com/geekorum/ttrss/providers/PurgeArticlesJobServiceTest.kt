package com.geekorum.ttrss.providers

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import com.geekorum.ttrss.Application
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PurgeArticlesJobServiceTest {
    @get:Rule
    val serviceRule = ServiceTestRule()

    @Test
    fun testThatServiceStartsCorrectly() {
        serviceRule.startService(
            Intent(ApplicationProvider.getApplicationContext<Application>(), PurgeArticlesJobService::class.java))

    }
}
