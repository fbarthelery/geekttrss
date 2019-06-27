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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestWorkerBuilder
import com.google.common.truth.Truth.assertThat
import org.junit.runner.RunWith
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.test.BeforeTest
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


    @Test
    fun testSuccessfulWorker() {
        val worker = workerBuilder.build()
        val result = worker.doWork()
        assertThat(result).isEqualTo(Result.success())
    }
}
