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
package com.geekorum.ttrss.sync.workers

import android.accounts.Account
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleWithAttachments
import com.geekorum.ttrss.htmlparsers.ImageUrlExtractor
import com.geekorum.ttrss.sync.BackgroundDataUsageManager
import com.geekorum.ttrss.sync.HttpCacher
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest


@OptIn(ExperimentalCoroutinesApi::class)
class CollectNewArticlesWorkerTest {
    private lateinit var workerBuilder: TestListenableWorkerBuilder<CollectNewArticlesWorker>
    private lateinit var apiService: MockApiService
    private lateinit var databaseService: MockDatabaseService
    private lateinit var backgroundDataUsageManager: BackgroundDataUsageManager
    private lateinit var imageUrlExtractor: ImageUrlExtractor
    private lateinit var httpCacher: HttpCacher

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testCoroutineDispatcher)

        apiService = MyMockApiService()
        databaseService = MockDatabaseService()
        backgroundDataUsageManager = MockBackgroundDataUsageManager()
        imageUrlExtractor = ImageUrlExtractor()
        httpCacher = HttpCacher(OkHttpClient())

        val applicationContext: Context = ApplicationProvider.getApplicationContext()
        workerBuilder = TestListenableWorkerBuilder(applicationContext)
        workerBuilder.setWorkerFactory(object : WorkerFactory() {
            override fun createWorker(
                    appContext: Context, workerClassName: String, workerParameters: WorkerParameters
            ): ListenableWorker? {
                val dispatchers = CoroutineDispatchersProvider(main = testCoroutineDispatcher,
                        io = testCoroutineDispatcher,
                        computation = testCoroutineDispatcher)

                return CollectNewArticlesWorker(appContext, workerParameters, dispatchers,
                        apiService, databaseService,
                        backgroundDataUsageManager, imageUrlExtractor, httpCacher)
            }
        })
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        testCoroutineDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun testThatNewArticlesArePresentAfterRunningWorker() = testCoroutineDispatcher.runBlockingTest {
        val inputData = CollectNewArticlesWorker.getInputData(
                Account("account", "type"), 1)

        // no articles at the beginning
        assertThat(databaseService.getArticle(1)).isNull()

        workerBuilder.setInputData(inputData)
        val worker = workerBuilder.build()
        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(databaseService.getArticle(1)).isNotNull()
    }

    private class MyMockApiService : MockApiService() {

        override suspend fun getArticles(feedId: Long, sinceId: Long, offset: Int, showExcerpt: Boolean, showContent: Boolean, includeAttachments: Boolean): List<ArticleWithAttachments> {
            return if (offset == 0) {
                val article = Article(id = 1, isUnread = true)
                listOf(ArticleWithAttachments(article, emptyList()))
            } else {
                emptyList()
            }
        }
    }

}
