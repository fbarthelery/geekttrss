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

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@UseExperimental(ExperimentalCoroutinesApi::class)
class InAppUpdateViewModel @Inject constructor(
    private val updateManager: InAppUpdateManager
) : ViewModel() {

    val isUpdateAvailable: LiveData<Boolean> = liveData {
        val result = updateManager.getUpdateAvailability()
        emit(result == UpdateAvailability.UPDATE_AVAILABLE)
    }

    private val updateStateChannel = Channel<UpdateState>()

    private val updateState: LiveData<UpdateState> = liveData {
        emit(updateManager.getUpdateState())
        for (state in updateStateChannel) {
            emit(state)
        }
    }

    val isUpdateReadyToInstall = updateState.map {
        it.status == UpdateState.Status.DOWNLOADED
    }

    private var updateJob: Job? = null

    fun startUpdateFlow(activity: Activity, requestCode: Int) {
        if (updateJob?.isActive == true) {
            return
        }

        updateJob = viewModelScope.launch {
            updateManager.startUpdate(activity, requestCode).collect {
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

    override fun onCleared() {
        updateStateChannel.close()
    }
}


