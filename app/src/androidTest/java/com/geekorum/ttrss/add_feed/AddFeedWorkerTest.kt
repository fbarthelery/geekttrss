package com.geekorum.ttrss.add_feed

import android.accounts.Account
import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ListenableWorker
import androidx.work.ListenableWorker.Result
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestDriver
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CancellationException
import kotlin.test.BeforeTest
import kotlin.test.fail

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


    @Ignore("Fails because of https://issuetracker.google.com/issues/135275844")
    @Test
    // TODO once issue is fixed use these tests and probably suppress AddFeedWorkerWithConstraintsTest
    fun testSuccessfulWorker() = runBlocking {
        val worker = workerBuilder.build()
        val result = worker.doWork()
        assertThat(result).isEqualTo(Result.success())
    }

    @Ignore("Fails because of https://issuetracker.google.com/issues/135275844")
    @Test
    fun testTailingWorker() = runBlocking {
        val worker = workerBuilder.build()
        apiService.subscribeToFeedResult = false
        val result = worker.doWork()
        assertThat(result).isEqualTo(Result.success())
    }
}


@RunWith(AndroidJUnit4::class)
class AddFeedWorkerWithConstraintsTest {
    private lateinit var testDriver: TestDriver
    private lateinit var workManager: WorkManager
    private lateinit var apiService: MockApiService

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()


    @BeforeTest
    fun setUp() {
        val applicationContext: Context = ApplicationProvider.getApplicationContext()
        apiService = MockApiService()

        val configuration = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory((object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context, workerClassName: String, workerParameters: WorkerParameters
                ): ListenableWorker? {
                    return AddFeedWorker(appContext, workerParameters, apiService)
                }
            }))
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(applicationContext, configuration)
        testDriver = WorkManagerTestInitHelper.getTestDriver(applicationContext)!!
        workManager = WorkManager.getInstance(applicationContext)
    }

    @Test
    fun testSuccessfulWorkerWithConstraints() {
        val request = createOneTimeWorkRequest()
        workManager.enqueue(request).result.get()

        testDriver.setAllConstraintsMet(request.id)

        assertWorkResult(request, WorkInfo.State.SUCCEEDED)
    }

    @Test
    fun testFailingWorkerWithConstraints() {
        val request = createOneTimeWorkRequest()

        apiService.subscribeToFeedResult = false
        workManager.enqueue(request).result.get()

        testDriver.setAllConstraintsMet(request.id)

        assertWorkResult(request, WorkInfo.State.FAILED)
    }

    private fun createOneTimeWorkRequest(): OneTimeWorkRequest {
        val inputData = AddFeedWorker.getInputData(Account("account", "type"),
            "https://my.example.feed")
        val request = OneTimeWorkRequestBuilder<AddFeedWorker>()
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())
            .setInputData(inputData)
            .build()
        return request
    }

    private fun assertWorkResult(request: OneTimeWorkRequest, state: WorkInfo.State) {
        val stateResult = CompletableDeferred<WorkInfo.State>()
        val livedata = workManager.getWorkInfoByIdLiveData(request.id)
        val observer = Observer<WorkInfo> {
            if (it.state == state) {
                stateResult.complete(it.state)
            }
        }
        livedata.observeForever(observer)
        runBlocking {
            try {
                withTimeout(5000) {
                    assertThat(stateResult.await()).isEqualTo(state)
                }
            } catch (e: CancellationException) {
                fail("Worker state result did not pass to $state")
            } finally {
                livedata.removeObserver(observer)
            }
        }
    }
}

private class MockApiService : SubscribeToFeedService {
    var subscribeToFeedResult = true

    override suspend fun subscribeToFeed(
        feedUrl: String, categoryId: Long, feedLogin: String, feedPassword: String
    ): Boolean {
        return subscribeToFeedResult
    }
}
