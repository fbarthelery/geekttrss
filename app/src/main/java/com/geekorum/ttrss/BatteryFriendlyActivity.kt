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
package com.geekorum.ttrss

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.geekorum.geekdroid.battery.isPowerSaveModeFlow
import com.geekorum.geekdroid.battery.lowBatteryFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * An [android.app.Activity] who switch to Night mode when battery is low or in saving mode.
 */
@SuppressLint("Registered")
open class BatteryFriendlyActivity : AppCompatActivity() {

    private val nightViewModel: ForceNightModeViewModel by viewModels()

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                nightViewModel.forceNightMode.collect {
                    // we need to reset the local night mode to unspecified
                    // otherwise the default night mode is not picked
                    val mode =
                        if (it) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
                    delegate.localNightMode = mode
                }
            }
        }
    }
}

/**
 * Observe the system to know if we should force night mode on all activities
 */
@HiltViewModel
class ForceNightModeViewModel(
    private val batterySaverFlow: Flow<Boolean>,
    private val lowBatteryFlow: Flow<Boolean>
) : ViewModel() {

    @Inject
    constructor(application: Application, powerManager: PowerManager) : this(
        isPowerSaveModeFlow(application, powerManager), application.lowBatteryFlow()
    )

    val forceNightMode = batterySaverFlow.flatMapLatest { saving ->
        if (saving) flowOf(true) else lowBatteryFlow
    }

}

