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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Transaction
import com.geekorum.ttrss.providers.ArticlesContract.Transaction.Field
import com.geekorum.ttrss.providers.ArticlesContract.Transaction.Field.STARRED
import com.geekorum.ttrss.providers.ArticlesContract.Transaction.Field.UNREAD
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@OptIn(ExperimentalCoroutinesApi::class)
class SendTransactionsWorkerTest {
    private lateinit var workerBuilder: TestListenableWorkerBuilder<SendTransactionsWorker>
    private lateinit var apiService: MyMockApiService
    private lateinit var databaseService: MockDatabaseService
    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testCoroutineDispatcher)

        val applicationContext: Context = ApplicationProvider.getApplicationContext()
        apiService = MyMockApiService()
        databaseService = MockDatabaseService()
        workerBuilder = TestListenableWorkerBuilder(applicationContext)
        workerBuilder.setWorkerFactory(object : WorkerFactory() {
            override fun createWorker(
                    appContext: Context, workerClassName: String, workerParameters: WorkerParameters
            ): ListenableWorker? {
                val dispatchers = CoroutineDispatchersProvider(main = testCoroutineDispatcher,
                        io = testCoroutineDispatcher,
                        computation = testCoroutineDispatcher)

                return SendTransactionsWorker(appContext, workerParameters, dispatchers,
                        apiService, databaseService)
            }
        })
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        testCoroutineDispatcher.cleanupTestCoroutines()
    }


    @Test
    fun testTransactionAreSendAndRemovedWhenRunningWorker() = testCoroutineDispatcher.runBlockingTest {
        // insert some transactions On Article 1 and 2
        databaseService.insertArticles(listOf(
                Article(id = 1, isUnread = false),
                Article(id = 2, isUnread = true, isStarred = true)))
        databaseService.insertTransaction(
                Transaction(id = 1, articleId = 1, field = UNREAD.toString(), value = true))
        databaseService.insertTransaction(
                Transaction(id = 2, articleId = 2, field = STARRED.toString(), value = false))
        assertThat(databaseService.getTransactions()).hasSize(2)

        val worker = workerBuilder.build()
        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())

        assertThat(databaseService.getTransactions()).isEmpty()
        val article1 = databaseService.getArticle(1)
        assertThat(article1).isEqualTo(Article(id = 1, isUnread = true, isTransientUnread = true))
        val article2 = databaseService.getArticle(2)
        assertThat(article2).isEqualTo(Article(id = 2, isUnread = true, isStarred = false))
        assertThat(apiService.called).isEqualTo(2)
    }

    private class MyMockApiService : MockApiService() {
        internal var called = 0
        override suspend fun updateArticleField(id: Long, field: Field, value: Boolean) {
            called++
        }
    }
}
