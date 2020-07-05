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
import com.geekorum.ttrss.data.AccountInfo
import com.geekorum.ttrss.data.ArticlesDatabase
import com.geekorum.ttrss.data.DiskDatabaseModule
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.network.ServerInfo
import com.geekorum.ttrss.network.TinyrssApiModule
import com.geekorum.ttrss.sync.DatabaseService
import com.google.common.truth.Truth.assertThat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import com.geekorum.ttrss.data.Account as DataAccount

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@UninstallModules(ActualCoroutineDispatchersModule::class,
    WorkersModule::class,
    TinyrssApiModule::class,
    DiskDatabaseModule::class)
class UpdateAccountInfoWorkerTest {
    private lateinit var workerBuilder: TestListenableWorkerBuilder<UpdateAccountInfoWorker>
    private lateinit var apiService: MyMockApiService
    private val testCoroutineDispatcher = TestCoroutineDispatcher()

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
    @InstallIn(ApplicationComponent::class)
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
        testCoroutineDispatcher.cleanupTestCoroutines()
    }


    @Test
    fun testThatAccountInfoAreUpdatedAfterRunningWorker() = runBlocking {
        // previous accountInfo
        assertThat(databaseService.getAccountInfo("account", "https://test.exemple.com"))
                .isNull()

        val inputData = workDataOf(
            BaseSyncWorker.PARAM_ACCOUNT_NAME to "account",
            BaseSyncWorker.PARAM_ACCOUNT_TYPE to AndroidTinyrssAccountManager.ACCOUNT_TYPE
        )
        workerBuilder.setInputData(inputData)
        val worker = workerBuilder.build()
        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        val expected = AccountInfo(account = DataAccount("account", "https://test.exemple.com/"),
                serverVersion = "2.0",
                apiLevel = 42)
        val accountInfo = databaseService.getAccountInfo("account", "https://test.exemple.com/")!!
        assertThat(accountInfo).isEqualTo(expected)
    }

    private class MyMockApiService : MockApiService() {

        override suspend fun getServerInfo(): ServerInfo {
            return ServerInfo(42, "https://test.exemple.com/feeds-icons/", "2.0")
        }
    }
}
