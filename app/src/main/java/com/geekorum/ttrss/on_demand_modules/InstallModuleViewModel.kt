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
package com.geekorum.ttrss.on_demand_modules

import android.app.Activity
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.geekorum.ttrss.R
import com.geekorum.ttrss.on_demand_modules.InstallSession.State.Status.CANCELED
import com.geekorum.ttrss.on_demand_modules.InstallSession.State.Status.CANCELING
import com.geekorum.ttrss.on_demand_modules.InstallSession.State.Status.DOWNLOADING
import com.geekorum.ttrss.on_demand_modules.InstallSession.State.Status.FAILED
import com.geekorum.ttrss.on_demand_modules.InstallSession.State.Status.INSTALLED
import com.geekorum.ttrss.on_demand_modules.InstallSession.State.Status.INSTALLING
import com.geekorum.ttrss.on_demand_modules.InstallSession.State.Status.PENDING
import com.geekorum.ttrss.on_demand_modules.InstallSession.State.Status.REQUIRES_USER_CONFIRMATION
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class InstallModuleViewModel @Inject constructor(
    private val moduleManager: OnDemandModuleManager
) : ViewModel() {

    data class InstallProgression(
        @StringRes
        val message: Int,
        val progress: Int,
        val max: Int,
        val progressIndeterminate: Boolean
    )

    private val _sessionState = MutableStateFlow(InstallSession.State(PENDING, 0, 0))
    val sessionState = _sessionState.asStateFlow()

    val progress = sessionState.map(this::mapStateToProgression)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), mapStateToProgression(sessionState.value))

    private var session: InstallSession? = null

    fun isModuleInstalled(module: String): Boolean {
        return module in moduleManager.installedModules
    }

    fun startInstallModules(vararg modules: String) = viewModelScope.launch {
        if (session != null) {
            return@launch
        }

        try {
            val session = moduleManager.startInstallModule(*modules)
            this@InstallModuleViewModel.session = session

            session.getSessionStates().collect {
                _sessionState.value = it
            }
        } catch (e: OnDemandModuleException) {
            Timber.w(e, "Unable to start installation of modules : $modules")
            _sessionState.value = InstallSession.State(FAILED, 0, 0)
        }
    }

    private fun mapStateToProgression(state: InstallSession.State): InstallProgression {
        Timber.d("install session state is $state")
        val message = when (state.status) {
            PENDING, DOWNLOADING -> R.string.lbl_download_in_progress
            INSTALLING -> R.string.lbl_install_in_progress
            INSTALLED -> R.string.lbl_install_complete
            FAILED -> R.string.lbl_failed_to_install
            else -> R.string.lbl_other
        }
        val max = 100
        val percent = when (state.status) {
            DOWNLOADING -> ((state.bytesDownloaded.toFloat() / state.totalBytesDownloaded) * max).roundToInt()
            INSTALLED -> 100
            else -> 0
        }
        val progressIndeterminate = when (state.status) {
            PENDING, INSTALLING, REQUIRES_USER_CONFIRMATION, CANCELING -> true
            DOWNLOADING, INSTALLED, FAILED, CANCELED -> false
        }
        return InstallProgression(message, percent, max, progressIndeterminate)
    }

    fun startUserConfirmationDialog(activity: Activity, code: Int)= viewModelScope.launch {
        session?.startUserConfirmationDialog(activity, code)
    }

}
