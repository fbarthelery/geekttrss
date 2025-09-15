/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InAppUpdateViewModelTest {

    @get:Rule
    val archRule = InstantTaskExecutorRule()

    lateinit var subject: InAppUpdateViewModel
    lateinit var updateManager: InAppUpdateManager

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        updateManager = mockk()
        subject = InAppUpdateViewModel(updateManager)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testUpdateAvailable()= runTest {
        coEvery { updateManager.getUpdateAvailability() } returns UpdateAvailability.UPDATE_AVAILABLE
        subject.isUpdateAvailable
                .take(1)
                .collect {
                    assertThat(it).isTrue()
                }
    }

    @Test
    fun testNoUpdateAvailable() = runTest {
        coEvery { updateManager.getUpdateAvailability() } returns UpdateAvailability.NO_UPDATE
        subject.isUpdateAvailable
                .take(1)
                .collect {
                    assertThat(it).isFalse()
                }
    }

    @Test
    fun testAnUpdateIsAlreadyReadyToInstall() = runTest {
        coEvery { updateManager.getUpdateState() } returns UpdateState(UpdateState.Status.DOWNLOADED)
        val updates = subject.isUpdateReadyToInstall
                .take(1)
                .toList()
        assertThat(updates).containsExactly(true)
    }

    @Test
    fun testAnUpdateFlowGoingToReadyToInstall() = runTest {
        coEvery { updateManager.getUpdateState() } returns UpdateState(UpdateState.Status.UNKNOWN)
        coEvery { updateManager.startUpdate(any(), any()) } returns flowOf(
            UpdateState(UpdateState.Status.UNKNOWN),
            UpdateState(UpdateState.Status.PENDING),
            UpdateState(UpdateState.Status.DOWNLOADING),
            UpdateState(UpdateState.Status.DOWNLOADED)
        )
        val results = async {
            subject.isUpdateReadyToInstall
                    .take(2)
                    .toList()
        }

        subject.startUpdateFlow(mockk(), 42)
        assertThat(results.await()).containsExactly(false, true)
    }

}
