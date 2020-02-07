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
import com.geekorum.ttrss.accounts.ServerInformation
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.AccountInfo
import com.geekorum.ttrss.network.ServerInfo
import com.geekorum.ttrss.data.Account as DataAccount
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

@UseExperimental(ExperimentalCoroutinesApi::class)
class UpdateAccountInfoWorkerTest {
    private lateinit var workerBuilder: TestListenableWorkerBuilder<UpdateAccountInfoWorker>
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
                val account = Account("account", "type")
                val serverInformation = object: ServerInformation() {
                    override val apiUrl: String = "https://test.exemple.com/"
                    override val basicHttpAuthUsername: String? = null
                    override val basicHttpAuthPassword: String? = null
                }

                return UpdateAccountInfoWorker(appContext, workerParameters, dispatchers,
                        account, serverInformation, apiService, databaseService)
            }
        })
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        testCoroutineDispatcher.cleanupTestCoroutines()
    }


    @Test
    fun testThatAccountInfoAreUpdatedAfterRunningWorker() = testCoroutineDispatcher.runBlockingTest {
        // previous accountInfo
        assertThat(databaseService.getAccountInfo("account", "https://test.exemple.com"))
                .isNull()

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
