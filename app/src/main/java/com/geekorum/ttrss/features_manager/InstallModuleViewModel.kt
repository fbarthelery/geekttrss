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
package com.geekorum.ttrss.features_manager

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.geekorum.ttrss.R
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class InstallModuleViewModel @Inject constructor(
    private val moduleManager: OnDemandModuleManager
) : ViewModel() {

    data class InstallProgression(
        @StringRes
        val message: Int,
        val progress: Int,
        val max: Int
    )

    private val _sessionState = MutableLiveData<InstallSession.State>().apply {
        value = InstallSession.State(InstallSession.State.Status.PENDING, 0, 0)
    }

    val progress = _sessionState.map {
        Timber.d("install session state is ${it}")
        val message = when (it.status) {
            InstallSession.State.Status.DOWNLOADING -> R.string.lbl_download_in_progress
            InstallSession.State.Status.INSTALLING -> R.string.lbl_install_in_progress
            InstallSession.State.Status.INSTALLED -> R.string.lbl_install_complete
            InstallSession.State.Status.FAILED -> R.string.lbl_failed_to_install
            else -> R.string.lbl_other
        }
        val max = 100
        val percent = when (it.totalBytesDownloaded) {
            0L -> 0
            else -> Math.round((it.bytesDownloaded.toFloat() / it.totalBytesDownloaded) * max)
        }
        InstallProgression(message, percent, max)
    }

    private var session: InstallSession? = null

    fun isModuleInstalled(module: String): Boolean {
        return module in moduleManager.installedModules
    }

    fun deferedInstallModule(vararg modules: String) {
        moduleManager.deferredInstall(*modules)
    }

    fun startInstallModules(vararg modules: String) = viewModelScope.launch {
        if (session != null) {
            return@launch
        }

        val session = moduleManager.startInstallModule(*modules)

        val sessionStates = produceInstallSessionStates(session)

        this@InstallModuleViewModel.session = session

        sessionStates.consumeEach {
            _sessionState.value = it
        }

    }

    fun installModule(vararg modules: String): LiveData<InstallSession.State> {
        val sessionDeferred = viewModelScope.async { moduleManager.startInstallModule(*modules) }
        return liveData {
            val session = try {
                sessionDeferred.await()
            } catch (e: OnDemandModuleException) {
                Timber.w(e, "Unable to install modules ${modules.joinToString()}")
                return@liveData
            }
            val stateLiveData = MutableLiveData<InstallSession.State>()
            emitSource(stateLiveData)
            val listener = object : InstallSession.Listener {
                override fun onStateUpdate(session: InstallSession, state: InstallSession.State) {
                    stateLiveData.value = state
                }
            }
            try {
                Timber.d("RegisterListener for livedata")
                session.registerListener(listener)

                // wait for completion
                session.awaitCompletion()
            } finally {
                Timber.d("UnregisterListener for livedata")
                session.unregisterListener(listener)
            }
        }
    }

}
