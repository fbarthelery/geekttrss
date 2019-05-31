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

import androidx.annotation.MainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

/**
 * Allows to manage On Demand modules
 */
interface OnDemandModuleManager {
    /**
     * Currently installed modules
     */
    val installedModules: Set<String>

    /**
     * Install these modules asynchronously
     */
    fun deferredInstall(vararg modules: String)

    /**
     * Uninstall these modules asynchronously
     */
    fun uninstall(vararg modules: String)

    /**
     * Start an Install Session that you can monitor
     */
    @Throws(OnDemandModuleException::class)
    suspend fun startInstallModule(vararg modules: String): InstallSession
}

class OnDemandModuleException
@JvmOverloads constructor(
    message: String? = null, cause: Throwable? = null
) : Exception(message, cause)


abstract class InstallSession(
    val id: Int
) {
    abstract fun cancel()
    abstract fun registerListener(listener: Listener)
    abstract fun unregisterListener(listener: Listener)

    abstract suspend fun sendStatesTo(channel: SendChannel<State>)

    abstract suspend fun getSessionState(): State

    interface Listener {
        @MainThread
        fun onStateUpdate(session: InstallSession, state: State)
    }

    data class State(
        val status: Status,
        val bytesDownloaded: Long,
        val totalBytesDownloaded: Long
    ) {
        enum class Status {
            PENDING,
            REQUIRES_USER_CONFIRMATION,
            DOWNLOADING,
            INSTALLING,
            INSTALLED,
            FAILED,
            CANCELING,
            CANCELED
        }
    }
}


/* Extensions functions for easy usage */

fun CoroutineScope.produceInstallSessionStates(session: InstallSession) = produce <InstallSession.State> {
    session.sendStatesTo(channel)
}

suspend fun InstallSession.awaitCompletion(): InstallSession.State {
    fun isComplete(state: InstallSession.State) = when (state.status) {
        InstallSession.State.Status.INSTALLED,
        InstallSession.State.Status.FAILED,
        InstallSession.State.Status.CANCELED -> true

        InstallSession.State.Status.PENDING,
        InstallSession.State.Status.REQUIRES_USER_CONFIRMATION,
        InstallSession.State.Status.DOWNLOADING,
        InstallSession.State.Status.INSTALLING,
        InstallSession.State.Status.CANCELING -> false
    }

    val state = getSessionState()
    if (isComplete(state)) {
        return state
    }

    return suspendCancellableCoroutine {
        val listener = object : InstallSession.Listener {
            override fun onStateUpdate(session: InstallSession, state: InstallSession.State) {
                Timber.d("Unregister listener for awaitCompletion")
                unregisterListener(this)
                it.resume(state)
            }
        }
        it.invokeOnCancellation {
            Timber.d("Unregister listener for awaitCompletion")
            unregisterListener(listener)
        }
        Timber.d("Register listener for awaitCompletion")
        registerListener(listener)
    }
}



