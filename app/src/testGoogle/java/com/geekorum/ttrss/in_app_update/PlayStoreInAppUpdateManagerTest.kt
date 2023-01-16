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
package com.geekorum.ttrss.in_app_update

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geekorum.ttrss.in_app_update.UpdateState.Status.DOWNLOADED
import com.geekorum.ttrss.in_app_update.UpdateState.Status.DOWNLOADING
import com.geekorum.ttrss.in_app_update.UpdateState.Status.FAILED
import com.geekorum.ttrss.in_app_update.UpdateState.Status.PENDING
import com.geekorum.ttrss.in_app_update.UpdateState.Status.UNKNOWN
import com.geekorum.ttrss.logging.CrashlyticsLoggingModule
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.InstallErrorCode
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import io.mockk.mockk
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.util.concurrent.Executors
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
@UninstallModules(CrashlyticsLoggingModule::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PlayStoreInAppUpdateManagerTest {

    private val mainThreadSurrogate = Executors.newSingleThreadExecutor {
        Thread(it, "UI Thread")
    }.asCoroutineDispatcher()

    lateinit var subject: PlayStoreInAppUpdateManager
    lateinit var fakeAppUpdateManager: FakeAppUpdateManager

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        fakeAppUpdateManager = FakeAppUpdateManager(ApplicationProvider.getApplicationContext())
        subject = PlayStoreInAppUpdateManager(fakeAppUpdateManager)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun testUpdateAvailability() = runBlockingTest {
        fakeAppUpdateManager.setUpdateNotAvailable()
        var updateAvailability = subject.getUpdateAvailability()
        assertThat(updateAvailability).isEqualTo(UpdateAvailability.NO_UPDATE)

        fakeAppUpdateManager.setUpdateAvailable(1234)
        updateAvailability = subject.getUpdateAvailability()
        assertThat(updateAvailability).isEqualTo(UpdateAvailability.UPDATE_AVAILABLE)
    }

    @Test
    fun testASuccessfulInstallFlow() = runBlockingTest {
        fakeAppUpdateManager.setUpdateAvailable(1234)

        val drivingFlow = subject.startUpdate(mockk(), 1234).onEach {
            when (it.status) { // triggers next step
                UNKNOWN -> fakeAppUpdateManager.userAcceptsUpdate()
                PENDING -> fakeAppUpdateManager.downloadStarts()
                DOWNLOADING -> fakeAppUpdateManager.downloadCompletes()
                // DOWNLOADED is now a final state. No other states after it
                else -> fakeAppUpdateManager.installFails()
            }
        }

        val statesFinal = async {
            drivingFlow.distinctUntilChanged().toList()
        }.awaitRunningMainLooper()
        assertThat(statesFinal).containsExactly(
            UpdateState(UNKNOWN),
            UpdateState(PENDING, InstallErrorCode.NO_ERROR),
            UpdateState(DOWNLOADING, InstallErrorCode.NO_ERROR),
            UpdateState(DOWNLOADED, InstallErrorCode.NO_ERROR)
        )
    }

    @Test
    fun testAFailureInstallFlow() = runBlockingTest {
        fakeAppUpdateManager.setUpdateAvailable(1234)

        val drivingFlow = subject.startUpdate(mockk(), 1234).onEach {
            when (it.status) { // triggers next step
                UNKNOWN -> fakeAppUpdateManager.userAcceptsUpdate()
                PENDING -> fakeAppUpdateManager.downloadStarts()
                DOWNLOADING -> {
                    fakeAppUpdateManager.setInstallErrorCode(InstallErrorCode.ERROR_INSTALL_NOT_ALLOWED)
                    fakeAppUpdateManager.downloadFails()
                }
                // DOWNLOADED is now a final state. No other states after it
                else -> fakeAppUpdateManager.installFails()
            }
        }

        val statesFinal = async {
            drivingFlow.distinctUntilChanged().toList()
        }.awaitRunningMainLooper()
        assertThat(statesFinal).containsExactly(
            UpdateState(UNKNOWN),
            UpdateState(PENDING, InstallErrorCode.NO_ERROR),
            UpdateState(DOWNLOADING, InstallErrorCode.NO_ERROR),
            UpdateState(FAILED, InstallErrorCode.ERROR_INSTALL_NOT_ALLOWED)
        )
    }

    private suspend fun <T> Deferred<T>.awaitRunningMainLooper(): T = coroutineScope {
        val runMainLooperIdle = launch {
            while(true) {
                yield()
                Shadows.shadowOf(Looper.getMainLooper()).idle()
            }
        }
        invokeOnCompletion { runMainLooperIdle.cancel() }
        await()
    }

}
