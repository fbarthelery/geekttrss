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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

/**
 * An Immutable module installations.
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
 * An InstallSession for an pre installed module
 */
private class CompleteSession(id: Int) : InstallSession(id) {
    private val state = State(State.Status.INSTALLED)

    override suspend fun getSessionState(): State = state

    override fun CoroutineScope.getSessionStates(): ReceiveChannel<State> = produce {
        send(state)
        close()
    }

    override fun cancel() {
        // no op
    }

    override fun registerListener(listener: Listener) {
        // send complete
        listener.onStateUpdate(this, state)
    }

    override fun unregisterListener(listener: Listener) {
        // no op
    }
}

/**
 * An InstallSession for an pre installed module
 */

private class FailedSession(id: Int) : InstallSession(id) {
    private val state = State(State.Status.FAILED)

    override suspend fun getSessionState(): State = state

    override fun CoroutineScope.getSessionStates(): ReceiveChannel<State> = produce {
        send(state)
        close()
    }

    override fun cancel() {
        // no op
    }

    override fun registerListener(listener: Listener) {
        // send fail
        listener.onStateUpdate(this, state)
    }

    override fun unregisterListener(listener: Listener) {
        // no op
    }

}
