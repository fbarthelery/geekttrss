/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class InAppUpdateViewModel @Inject constructor(
    private val updateManager: InAppUpdateManager
) : ViewModel() {

    val isUpdateAvailable: Flow<Boolean> = flow {
        val result = updateManager.getUpdateAvailability()
        Timber.d("Update availability $result")
        emit(result == UpdateAvailability.UPDATE_AVAILABLE)
    }

    private val updateStateChannel = Channel<UpdateState>(Channel.CONFLATED)

    private val updateState: Flow<UpdateState> = flow {
        emit(updateManager.getUpdateState())
        for (state in updateStateChannel) {
            emit(state)
        }
    }

    val isUpdateReadyToInstall = updateState.map {
        Timber.d("Update status ${it.status}")
        it.status == UpdateState.Status.DOWNLOADED
    }.distinctUntilChanged()

    private var updateJob: Job? = null

    fun startUpdateFlow(intentSenderForResultStarter: IntentSenderForResultStarter, requestCode: Int) {
        if (updateJob?.isActive == true) {
            return
        }

        updateJob = viewModelScope.launch {
            updateManager.startUpdate(intentSenderForResultStarter, requestCode).collect {
                updateStateChannel.send(it)
            }
        }
    }

    fun cancelUpdateFlow() {
        updateJob?.cancel()
    }


    fun completeUpdate() {
        updateManager.completeUpdate()
    }
}


