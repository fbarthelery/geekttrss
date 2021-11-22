/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2021 by Frederic-Charles Barthelery.
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
import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.geekorum.ttrss.core.ActualCoroutineDispatchersModule
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.*
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.network.TinyrssApiModule
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
class UpdateArticleStatusWorkerTest {
    private lateinit var workerBuilder: TestListenableWorkerBuilder<UpdateArticleStatusWorker>
    private lateinit var apiService: MyMockApiService
    private val testCoroutineDispatcher = StandardTestDispatcher()

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory
    @Inject
    lateinit var databaseService: DatabaseService

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

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
    fun testThatArticleStatusChangeAfterRunningWorker() = runTest {
        // prepare database
        databaseService.insertCategories(listOf(Category(id = 1, title = "Dummy category")))
        databaseService.insertFeeds(listOf(Feed(id =1 , title= "Dummy feed", catId = 1)))
        // insert a article with status unread to false
        databaseService.insertArticles(listOf(Article(id = 1, isUnread = false, feedId = 1)))
        val previous = databaseService.getArticle(1)!!
        assertThat(previous.isUnread).isEqualTo(false)

        val inputData = UpdateArticleStatusWorker.getInputData(
            Account("account", "type"), 1)
        workerBuilder.setInputData(inputData)
        val worker = workerBuilder.build()
        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        val article = databaseService.getArticle(1)!!
        assertThat(article.isUnread).isEqualTo(true)
    }

    private class MyMockApiService : MockApiService() {

        override suspend fun getArticles(feedId: Long, sinceId: Long, offset: Int, showExcerpt: Boolean, showContent: Boolean, includeAttachments: Boolean): List<ArticleWithAttachments> {
            return if (sinceId == 0L) {
                val article = Article(id = 1, isUnread = true)
                listOf(ArticleWithAttachments(article, emptyList()))
            } else {
                emptyList()
            }
        }

    }

}

