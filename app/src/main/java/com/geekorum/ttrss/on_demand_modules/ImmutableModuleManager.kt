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
package com.geekorum.ttrss.on_demand_modules

import android.app.Activity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * An Immutable modules installation
 */
class ImmutableModuleManager(
    override val installedModules: Set<String>
) : OnDemandModuleManager {

    private var nextSessionId = 1
    override suspend fun startInstallModule(vararg modules: String): InstallSession {
        val toInstall = modules.toSet() - installedModules
        return if (toInstall.isEmpty())
            CompleteSession(nextSessionId++)
        else
            FailedSession(nextSessionId++)
    }

    override fun deferredInstall(vararg modules: String) {
        val toInstall = modules.toSet() - installedModules
        if (toInstall.isNotEmpty()) {
            throw UnsupportedOperationException("This module manager can't install new modules")
        }
    }

    override fun uninstall(vararg modules: String) {
        val toUninstall = installedModules.intersect(modules.toSet())
        if (toUninstall.isNotEmpty()) {
            throw UnsupportedOperationException("This module manager can't uninstall module")
        }
    }

}

/**
 * An InstallSession for a pre installed module
 */
internal class CompleteSession(id: Int) : InstallSession(id) {
    private val state = State(State.Status.INSTALLED, 0, 0)

    override suspend fun getSessionState(): State = state

    @ExperimentalCoroutinesApi
    override fun getSessionStates(): Flow<State> = flowOf(state)

    override fun cancel() {
        // no op
    }

    override suspend fun startUserConfirmationDialog(activity: Activity, code: Int) {
        // no op
    }

}

/**
 * An InstallSession for an pre installed module
 */

private class FailedSession(id: Int) : InstallSession(id) {
    private val state = State(State.Status.FAILED, 0, 0)

    override suspend fun getSessionState(): State = state

    @ExperimentalCoroutinesApi
    override fun getSessionStates(): Flow<State> = flowOf(state)

    override fun cancel() {
        // no op
    }

    override suspend fun startUserConfirmationDialog(activity: Activity, code: Int) {
        // no op
    }

}

class MockedSession(id: Int) : InstallSession(id) {

    @ExperimentalCoroutinesApi
    override fun getSessionStates(): Flow<State> = flow {
        var state = State(State.Status.PENDING, 0, 0)
        emit(state)
        delay(500)

        state = State(State.Status.DOWNLOADING, 0, 1000)
        emit(state)
        delay(500)
        state = State(State.Status.DOWNLOADING, 200, 1000)
        emit(state)
        delay(500)
        state = State(State.Status.DOWNLOADING, 700, 1000)
        emit(state)
        delay(500)
        state = State(State.Status.DOWNLOADING, 1000, 1000)
        emit(state)
        delay(500)

        state = State(State.Status.INSTALLING, 1000, 1000)
        emit(state)
        delay(500)

        state = State(State.Status.INSTALLED, 1000, 1000)
        emit(state)
    }

    override fun cancel() {
        TODO("not implemented")
    }

    override suspend fun getSessionState(): State {
        TODO("not implemented")
    }

    override suspend fun startUserConfirmationDialog(activity: Activity, code: Int) {
        TODO("not implemented")
    }

}
