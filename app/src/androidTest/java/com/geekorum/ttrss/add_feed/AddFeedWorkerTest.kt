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
package com.geekorum.ttrss.add_feed

import android.accounts.Account
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.BeforeTest

@RunWith(AndroidJUnit4::class)
class AddFeedWorkerTest {
    lateinit var workerBuilder: TestListenableWorkerBuilder<AddFeedWorker>
    private lateinit var apiService: MockApiService

    @BeforeTest
    fun setUp() {
        val applicationContext: Context = ApplicationProvider.getApplicationContext()
        apiService = MockApiService()
        workerBuilder = TestListenableWorkerBuilder(applicationContext)
        workerBuilder.setWorkerFactory(object : WorkerFactory() {
            override fun createWorker(
                appContext: Context, workerClassName: String, workerParameters: WorkerParameters
            ): ListenableWorker? {
                return AddFeedWorker(appContext, workerParameters, apiService)
            }
        })
    }


    @Test
    fun testSuccessfulWorker() = runBlocking {
        val inputData = AddFeedWorker.getInputData(Account("account", "type"),
        "https://my.example.feed")

        val worker = workerBuilder.setInputData(inputData).build()
        val result = worker.startWork().get()
        assertThat(result).isEqualTo(Result.success())
    }

    @Test
    fun testFailingWorker() = runBlocking {
        val inputData = AddFeedWorker.getInputData(Account("account", "type"),
            "https://my.example.feed")

        val worker = workerBuilder.setInputData(inputData).build()
        apiService.subscribeToFeedResult = ResultCode.INVALID_URL
        val result = worker.startWork().get()
        assertThat(result).isEqualTo(Result.failure())
    }
}


private class MockApiService : SubscribeToFeedService {
    var subscribeToFeedResult = ResultCode.SUCCESS

    override suspend fun subscribeToFeed(
        feedUrl: String, categoryId: Long, feedLogin: String, feedPassword: String
    ): ResultCode {
        return subscribeToFeedResult
    }
}
