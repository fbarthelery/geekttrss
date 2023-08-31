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
import com.google.android.play.core.ktx.bytesDownloaded
import com.google.android.play.core.ktx.requestCancelInstall
import com.google.android.play.core.ktx.requestDeferredInstall
import com.google.android.play.core.ktx.requestDeferredUninstall
import com.google.android.play.core.ktx.requestInstall
import com.google.android.play.core.ktx.requestProgressFlow
import com.google.android.play.core.ktx.requestSessionState
import com.google.android.play.core.ktx.status
import com.google.android.play.core.ktx.totalBytesToDownload
import com.google.android.play.core.splitinstall.SplitInstallException
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class PlayStoreModuleManager constructor(
    private val splitInstallManager: SplitInstallManager
) : OnDemandModuleManager {
    override val canInstallModule = true

    override suspend fun startInstallModule(vararg modules: String): InstallSession {
        try {
            val id = splitInstallManager.requestInstall(modules = modules.toList())
            if (id == 0) {
                // already installed so not a real session
                return CompleteSession(id)
            }
            return SplitInstallSession(splitInstallManager, id)
        } catch (e: SplitInstallException) {
            throw OnDemandModuleException("unable to install modules", e)
        }
    }

    override fun deferredInstall(vararg modules: String) = runBlocking {
        splitInstallManager.requestDeferredInstall(modules.toList())
    }

    override fun uninstall(vararg modules: String) = runBlocking {
        splitInstallManager.requestDeferredUninstall(modules.toList())
    }

    override val installedModules: Set<String>
        get() = splitInstallManager.installedModules

}

private class SplitInstallSession(
    private val splitInstallManager: SplitInstallManager,
    id: Int
) : InstallSession(id) {

    override fun getSessionStates(): Flow<State> {
        return splitInstallManager.requestProgressFlow()
                .map { it.toInstallSessionState() }
    }

    override suspend fun getSessionState(): State {
        val splitInstallSessionState = splitInstallManager.requestSessionState(id)
        return splitInstallSessionState.toInstallSessionState()
    }

    override fun cancel() = runBlocking {
        splitInstallManager.requestCancelInstall(id)
    }

    override suspend fun startUserConfirmationDialog(activity: Activity, code: Int) {
        val state = splitInstallManager.requestSessionState(id)
        splitInstallManager.startConfirmationDialogForResult(state, activity, code)
    }

}

private fun SplitInstallSessionState.toInstallSessionState(): InstallSession.State {
    val status = when (status) {
        SplitInstallSessionStatus.PENDING -> InstallSession.State.Status.PENDING
        SplitInstallSessionStatus.DOWNLOADING -> InstallSession.State.Status.DOWNLOADING
        SplitInstallSessionStatus.DOWNLOADED, SplitInstallSessionStatus.INSTALLING -> InstallSession.State.Status.INSTALLING
        SplitInstallSessionStatus.INSTALLED -> InstallSession.State.Status.INSTALLED
        SplitInstallSessionStatus.FAILED, SplitInstallSessionStatus.UNKNOWN -> InstallSession.State.Status.FAILED
        SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> InstallSession.State.Status.REQUIRES_USER_CONFIRMATION
        SplitInstallSessionStatus.CANCELING -> InstallSession.State.Status.CANCELING
        SplitInstallSessionStatus.CANCELED -> InstallSession.State.Status.CANCELED
        else -> throw IllegalArgumentException("unhandled status $status")
    }
    return InstallSession.State(status, bytesDownloaded, totalBytesToDownload)
}


@Module
@InstallIn(SingletonComponent::class)
class PlayStoreInstallModule {
    @Provides
    fun providesOnDemandModuleManager(application: android.app.Application): OnDemandModuleManager {
        return PlayStoreModuleManager(SplitInstallManagerFactory.create(application))
    }
}
