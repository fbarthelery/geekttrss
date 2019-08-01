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
package com.geekorum.ttrss.in_app_update

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.android.play.core.install.model.InstallErrorCode
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import java.util.concurrent.Executors
import kotlin.test.BeforeTest
import kotlin.test.Test

@UseExperimental(ExperimentalCoroutinesApi::class)
class InAppUpdateViewModelTest {

    @get:Rule
    val archRule = InstantTaskExecutorRule()

    private val mainThreadSurrogate =  Executors.newSingleThreadExecutor {
        Thread(it, "UI Thread")
    }.asCoroutineDispatcher()

    lateinit var subject: InAppUpdateViewModel
    lateinit var updateManager: InAppUpdateManager

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        updateManager = mockk()
        subject = InAppUpdateViewModel(updateManager)
    }

    @Test
    fun testUpdateAvailable() {
        val observer: Observer<Boolean> = mockObserver()
        coEvery { updateManager.getUpdateAvailability() } returns UpdateAvailability.UPDATE_AVAILABLE
        subject.isUpdateAvailable.observeForever(observer)

        verify { observer.onChanged(true) }
    }

    @Test
    fun testNoUpdateAvailable() {
        val observer: Observer<Boolean> = mockObserver()
        coEvery { updateManager.getUpdateAvailability() } returns UpdateAvailability.NO_UPDATE
        subject.isUpdateAvailable.observeForever(observer)

        verify { observer.onChanged(false) }
    }

    @Test
    fun testAnUpdateFlowGoingToReadyToInstall() {
        val observer: Observer<Boolean> = mockObserver()
        coEvery { updateManager.startUpdate(any(), any()) } returns flowOf(
            UpdateState(UpdateState.Status.UNKNOWN),
            UpdateState(UpdateState.Status.PENDING, InstallErrorCode.NO_ERROR),
            UpdateState(UpdateState.Status.DOWNLOADING, InstallErrorCode.NO_ERROR),
            UpdateState(UpdateState.Status.DOWNLOADED, InstallErrorCode.NO_ERROR)
        )
        subject.isUpdateReadyToInstall.observeForever(observer)

        subject.startUpdateFlow(mockk(), 42)

        verifyOrder {
            observer.onChanged(false)
            observer.onChanged(true)
        }
    }

    private inline fun <reified T : Observer<K>, reified K : Any> mockObserver(): T {
        val observer: T = mockk()
        every { observer.onChanged(any()) } just Runs
        return observer
    }

}
