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
