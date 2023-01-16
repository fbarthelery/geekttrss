/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2023 by Frederic-Charles Barthelery.
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

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.geekorum.ttrss.accounts.AndroidTinyrssAccountManager
import com.geekorum.ttrss.core.ActualCoroutineDispatchersModule
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticlesDatabase
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.DiskDatabaseModule
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.Transaction
import com.geekorum.ttrss.data.TransactionsDao
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.network.TinyrssApiModule
import com.geekorum.ttrss.providers.ArticlesContract.Transaction.Field
import com.geekorum.ttrss.providers.ArticlesContract.Transaction.Field.STARRED
import com.geekorum.ttrss.providers.ArticlesContract.Transaction.Field.UNREAD
import com.geekorum.ttrss.sync.DatabaseService
import com.google.common.truth.Truth.assertThat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@UninstallModules(ActualCoroutineDispatchersModule::class,
    WorkersModule::class,
    TinyrssApiModule::class,
    DiskDatabaseModule::class)
class SendTransactionsWorkerTest {
    private lateinit var workerBuilder: TestListenableWorkerBuilder<SendTransactionsWorker>
    private lateinit var apiService: MyMockApiService
    private val testCoroutineDispatcher = StandardTestDispatcher()

    @Inject lateinit var databaseService: DatabaseService
    @Inject lateinit var transactionsDao: TransactionsDao

    @JvmField
    @BindValue
    val dispatchers = CoroutineDispatchersProvider(main = testCoroutineDispatcher,
        io = testCoroutineDispatcher,
        computation = testCoroutineDispatcher)

    @Module(includes = [FakeSyncWorkersModule::class])
    @InstallIn(SingletonComponent::class)
    inner class MockModule {
        @Provides
        fun providesApiService(): ApiService = apiService

        @Provides
        @Singleton
        internal fun providesAppDatabase(application: Application): ArticlesDatabase {
            return buildInMemoryDatabase(application, dispatchers.io.asExecutor())
        }
    }

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BeforeTest
    fun setUp() {
        hiltRule.inject()
        Dispatchers.setMain(testCoroutineDispatcher)

        apiService = MyMockApiService()
        val applicationContext: Context = ApplicationProvider.getApplicationContext()
        workerBuilder = TestListenableWorkerBuilder(applicationContext)
        workerBuilder.setWorkerFactory(hiltWorkerFactory)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun testTransactionAreSendAndRemovedWhenRunningWorker() = runTest {
        // prepare database
        databaseService.insertCategories(listOf(Category(id = 1, title = "Dummy category")))
        databaseService.insertFeeds(listOf(Feed(id =1 , title= "Dummy feed", catId = 1)))
        // insert some transactions On Article 1 and 2
        databaseService.insertArticles(listOf(
                Article(id = 1, isUnread = false, feedId = 1),
                Article(id = 2, isUnread = true, isStarred = true, feedId = 1)))
        transactionsDao.insertTransaction(
                Transaction(id = 1, articleId = 1, field = UNREAD.toString(), value = true))
        transactionsDao.insertTransaction(
                Transaction(id = 2, articleId = 2, field = STARRED.toString(), value = false))
        assertThat(databaseService.getTransactions()).hasSize(2)

        val inputData = workDataOf(
            BaseSyncWorker.PARAM_ACCOUNT_NAME to "account.name",
            BaseSyncWorker.PARAM_ACCOUNT_TYPE to AndroidTinyrssAccountManager.ACCOUNT_TYPE
        )
        workerBuilder.setInputData(inputData)

        val worker = workerBuilder.build()
        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())

        assertThat(databaseService.getTransactions()).isEmpty()
        val article1 = databaseService.getArticle(1)
        assertThat(article1).isEqualTo(Article(id = 1, isUnread = true, isTransientUnread = true, feedId = 1))
        val article2 = databaseService.getArticle(2)
        assertThat(article2).isEqualTo(Article(id = 2, isUnread = true, isStarred = false, feedId = 1))
        assertThat(apiService.called).isEqualTo(2)
    }

    private class MyMockApiService : MockApiService() {
        internal var called = 0
        override suspend fun updateArticleField(id: Long, field: Field, value: Boolean) {
            called++
        }
    }
}
