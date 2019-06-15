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

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ListenableWorker
import androidx.work.ListenableWorker.Result
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestDriver
import androidx.work.testing.TestWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.geekorum.ttrss.BackgroundJobManager
import com.google.common.truth.Truth.assertThat
import org.junit.runner.RunWith
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test


@RunWith(AndroidJUnit4::class)
class PurgeArticlesWorkerTest {
    lateinit var applicationContext: Context
    lateinit var workerBuilder : TestWorkerBuilder<PurgeArticlesWorker>
    lateinit var executor: Executor

    @BeforeTest
    fun setUp() {
        applicationContext = ApplicationProvider.getApplicationContext()
        executor = Executors.newSingleThreadExecutor()
        val articlesProvidersDao = ArticlesProvidersDao { 0 }
        workerBuilder = TestWorkerBuilder(applicationContext, executor)
        workerBuilder.setWorkerFactory(object: WorkerFactory() {
            override fun createWorker(
                appContext: Context, workerClassName: String, workerParameters: WorkerParameters
            ): ListenableWorker? = PurgeArticlesWorker(appContext, workerParameters, articlesProvidersDao)
        })
    }


    @Ignore("Fails because of https://issuetracker.google.com/issues/135275844")
    @Test
    fun testSuccessfulWorker() {
        val worker = workerBuilder.build()
        val result = worker.doWork()
        assertThat(result).isEqualTo(Result.success())
    }
}


@RunWith(AndroidJUnit4::class)
class PurgeArticlesWorkerWithConstraintsTest {
    lateinit var applicationContext: Application
    lateinit var backgroundJobManager: BackgroundJobManager
    lateinit var testDriver: TestDriver
    lateinit var workManager: WorkManager

    @BeforeTest
    fun setUp() {
        applicationContext = ApplicationProvider.getApplicationContext()
        backgroundJobManager = BackgroundJobManager(applicationContext)
        val articlesProvidersDao = ArticlesProvidersDao { 0 }

        val configuration = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .setWorkerFactory((object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context, workerClassName: String, workerParameters: WorkerParameters
                ): ListenableWorker? = PurgeArticlesWorker(appContext, workerParameters, articlesProvidersDao)
            }))
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(applicationContext, configuration)
        testDriver = WorkManagerTestInitHelper.getTestDriver(applicationContext)!!
        workManager = WorkManager.getInstance(applicationContext)
    }

    @Test
    fun testPeriodicWorker() {
        val request = PeriodicWorkRequestBuilder<PurgeArticlesWorker>(
            BackgroundJobManager.PERIODIC_PURGE_JOB_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder()
                .setRequiresDeviceIdle(true)
                .setRequiresCharging(true)
                .build())
            .build()

        workManager.enqueue(request).result.get()

        testDriver.apply {
            setAllConstraintsMet(request.id)
            setPeriodDelayMet(request.id)
        }

        val workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo.state).isEqualTo(WorkInfo.State.ENQUEUED)
    }

    @Test
    fun testSuccessfulWorkerWithConstraints() {
        val request = OneTimeWorkRequestBuilder<PurgeArticlesWorker>()
            .setConstraints(Constraints.Builder()
                .setRequiresDeviceIdle(true)
                .setRequiresCharging(true)
                .build())
            .build()

        workManager.enqueue(request).result.get()

        testDriver.apply {
            setAllConstraintsMet(request.id)
        }

        val workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo.state).isEqualTo(WorkInfo.State.SUCCEEDED)
    }

}
