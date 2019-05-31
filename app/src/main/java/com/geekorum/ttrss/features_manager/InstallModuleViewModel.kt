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
import kotlinx.coroutines.async
import timber.log.Timber
import javax.inject.Inject

class InstallModuleViewModel @Inject constructor(
    private val moduleManager: OnDemandModuleManager
) : ViewModel() {

    fun isModuleInstalled(module: String): Boolean {
        return module in moduleManager.installedModules
    }

    fun deferedInstallModule(vararg modules: String) {
        moduleManager.deferredInstall(*modules)
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
