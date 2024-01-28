/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.on_demand_modules

import app.cash.turbine.test
import com.geekorum.ttrss.R
import com.geekorum.ttrss.on_demand_modules.InstallSession.State.Status
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test
import kotlin.test.BeforeTest

class InstallModuleViewModelTest {
    lateinit var subject: InstallModuleViewModel

    private lateinit var mockModuleManager: SuccessfulMockModuleManager

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockModuleManager = SuccessfulMockModuleManager()
        subject = InstallModuleViewModel(mockModuleManager)
    }

    @Test
    fun testSuccessfulSessionState() = runTest {
        subject.sessionState.test {
            assertThat(awaitItem()).isEqualTo(InstallSession.State(Status.PENDING,  0 , 0))
            subject.startInstallModules("fakemodule")

            assertThat(awaitItem()).isEqualTo(InstallSession.State(Status.DOWNLOADING,  0 , 0))
            assertThat(awaitItem()).isEqualTo(InstallSession.State(Status.DOWNLOADING,  0 , 1000))
            assertThat(awaitItem()).isEqualTo(InstallSession.State(Status.DOWNLOADING,  200 , 1000))
            assertThat(awaitItem()).isEqualTo(InstallSession.State(Status.DOWNLOADING,  700 , 1000))
            assertThat(awaitItem()).isEqualTo(InstallSession.State(Status.DOWNLOADING,  1000 , 1000))
            assertThat(awaitItem()).isEqualTo(InstallSession.State(Status.INSTALLING,  1000 , 1000))
            assertThat(awaitItem()).isEqualTo(InstallSession.State(Status.INSTALLED,  1000 , 1000))
        }
    }

    @Test
    fun testSuccessfulSessionProgress() = runTest {
        subject.progress.test {
            assertThat(awaitItem()).isEqualTo(InstallModuleViewModel.InstallProgression(R.string.lbl_download_in_progress, 0, 100, true))
            subject.startInstallModules("fakemodule")

            assertThat(awaitItem()).isEqualTo(InstallModuleViewModel.InstallProgression(R.string.lbl_download_in_progress, 0, 100, false))
            assertThat(awaitItem()).isEqualTo(InstallModuleViewModel.InstallProgression(R.string.lbl_download_in_progress, 20, 100, false))
            assertThat(awaitItem()).isEqualTo(InstallModuleViewModel.InstallProgression(R.string.lbl_download_in_progress, 70, 100, false))
            assertThat(awaitItem()).isEqualTo(InstallModuleViewModel.InstallProgression(R.string.lbl_download_in_progress, 100, 100, false))
            assertThat(awaitItem()).isEqualTo(InstallModuleViewModel.InstallProgression(R.string.lbl_install_in_progress, 0, 100, true))
            assertThat(awaitItem()).isEqualTo(InstallModuleViewModel.InstallProgression(R.string.lbl_install_complete, 100, 100, false))
        }
    }
}

private class SuccessfulMockModuleManager(override val installedModules: Set<String> = emptySet()) : OnDemandModuleManager {

    override fun deferredInstall(vararg modules: String) {}

    override fun uninstall(vararg modules: String) {}

    override suspend fun startInstallModule(vararg modules: String): InstallSession {
        return MockedSession(1)
    }

    override val canInstallModule: Boolean = true
}