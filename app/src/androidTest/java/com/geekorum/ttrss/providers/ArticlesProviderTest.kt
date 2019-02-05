package com.geekorum.ttrss.providers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geekorum.ttrss.Application
import com.google.common.truth.Truth.assertThat
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class ArticlesProviderTest {

// We should use a provider test rule to have an isolated context and database but
// it can't be used because it replace the application context and dagger
// injection assume it's an instance of Application
/*
    @get:Rule
    val providerRule: ProviderTestRule =
        ProviderTestRule.Builder(ArticlesProvider::class.java, ArticlesContract.AUTHORITY)
            .build()
*/


    @Test
    fun testThatWeAreAbleToMakeAQuery() {
        val resolver = ApplicationProvider.getApplicationContext<Application>().contentResolver
        val cursor = resolver.query(ArticlesContract.Article.CONTENT_URI, null, null, null, null)
        assertThat(cursor?.count).isEqualTo(0)
    }

}
