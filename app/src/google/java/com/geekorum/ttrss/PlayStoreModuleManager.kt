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
package com.geekorum.ttrss

import com.geekorum.geekdroid.gms.await
import com.geekorum.ttrss.features_manager.CompleteSession
import com.geekorum.ttrss.features_manager.InstallSession
import com.geekorum.ttrss.features_manager.OnDemandModuleException
import com.geekorum.ttrss.features_manager.OnDemandModuleManager
import com.google.android.play.core.splitinstall.SplitInstallException
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.google.android.play.core.tasks.Task
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import timber.log.Timber

class PlayStoreModuleManager constructor(
    private val splitInstallManager: SplitInstallManager
) : OnDemandModuleManager {
    override suspend fun startInstallModule(vararg modules: String): InstallSession {
        val installRequest = SplitInstallRequest.newBuilder().apply {
            modules.forEach {
                addModule(it)
            }
        }.build()
        val installTask: Task<Int> = splitInstallManager.startInstall(installRequest)
        try {
            val id = installTask.await()
            if (id == 0) {
                // already installed so not a real session
                return CompleteSession(id)
            }
            return SplitInstallSession(splitInstallManager, id)
        } catch (e: SplitInstallException) {
            throw OnDemandModuleException("unable to install modules", e)
        }
    }

    override fun deferredInstall(vararg modules: String) {
        splitInstallManager.deferredInstall(modules.toList())
    }

    override fun uninstall(vararg modules: String) {
        splitInstallManager.deferredUninstall(modules.toList())
    }

    override val installedModules: Set<String>
        get() = splitInstallManager.installedModules


}

private class SplitInstallSession(
    private val splitInstallManager: SplitInstallManager,
    id: Int
) : InstallSession(id) {
    override fun CoroutineScope.getSessionStates(): ReceiveChannel<State> = produce {
        val listener = SplitInstallStateUpdatedListener {
            val installState = it.toInstallSessionState()
            launch {
                //TODO close when state is a terminal state?
                send(installState)
            }
        }
        splitInstallManager.registerListener(listener)
        invokeOnClose {
            splitInstallManager.unregisterListener(listener)
        }
    }

    private val splitListener: SplitInstallStateUpdatedListener = SplitInstallStateUpdatedListener { state ->
        if (state.sessionId() != id) {
            return@SplitInstallStateUpdatedListener
        }
        val installState = state.toInstallSessionState()
        listeners.forEach {
            it.onStateUpdate(this, installState)
        }
    }

    private val listeners = mutableSetOf<Listener>()

    override suspend fun getSessionState(): State {
        val splitInstallSessionState = splitInstallManager.getSessionState(id).await()
        return splitInstallSessionState.toInstallSessionState()
    }

    override fun cancel() {
        splitInstallManager.cancelInstall(id)
    }

    override fun registerListener(listener: Listener) {
        if (listeners.isEmpty()) {
            splitInstallManager.registerListener(splitListener)
        }
        listeners.add(listener)
    }

    override fun unregisterListener(listener: Listener) {
        // remove
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            splitInstallManager.unregisterListener(splitListener)
        }
    }

}

private fun SplitInstallSessionState.toInstallSessionState(): InstallSession.State {
    Timber.d("convert split install state status ${status()}")
    val status = when (val status = status()) {
        SplitInstallSessionStatus.INSTALLED -> InstallSession.State.Status.INSTALLED
        SplitInstallSessionStatus.FAILED -> InstallSession.State.Status.FAILED
        SplitInstallSessionStatus.PENDING -> InstallSession.State.Status.PENDING
        SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> InstallSession.State.Status.REQUIRES_USER_CONFIRMATION
        SplitInstallSessionStatus.DOWNLOADING -> InstallSession.State.Status.DOWNLOADING
        SplitInstallSessionStatus.DOWNLOADED, SplitInstallSessionStatus.INSTALLING -> InstallSession.State.Status.INSTALLING
        SplitInstallSessionStatus.CANCELING -> InstallSession.State.Status.CANCELING
        SplitInstallSessionStatus.CANCELED -> InstallSession.State.Status.CANCELED
        else -> TODO("unhandled status $status")
    }
    return InstallSession.State(status)
}


@Module
class PlayStoreInstallModule {
    @Provides
    fun providesOnDemandModuleManager(application: android.app.Application): OnDemandModuleManager {
        return PlayStoreModuleManager(SplitInstallManagerFactory.create(application))
    }
}
