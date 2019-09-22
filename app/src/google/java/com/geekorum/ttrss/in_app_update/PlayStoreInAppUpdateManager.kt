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
import android.app.Application
import com.geekorum.geekdroid.gms.await
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import com.google.android.play.core.install.model.UpdateAvailability as PlayUpdateAvailability

private val TAG = PlayStoreInAppUpdateManager::class.java.simpleName

class PlayStoreInAppUpdateManager(
    private val appUpdateManager: AppUpdateManager
) : InAppUpdateManager {
    override suspend fun getUpdateAvailability(): UpdateAvailability {
        val updateInfo = appUpdateManager.appUpdateInfo.await()
        return when (updateInfo.updateAvailability()) {
            PlayUpdateAvailability.UPDATE_AVAILABLE,
            PlayUpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            -> UpdateAvailability.UPDATE_AVAILABLE

            else -> UpdateAvailability.NO_UPDATE
        }
    }

    override suspend fun getUpdateState(): UpdateState {
        val updateInfo = appUpdateManager.appUpdateInfo.await()
        val status = PlayInstallStatus(updateInfo.installStatus()).toUpdateStateStatus()
        return UpdateState(status)
    }

    @UseExperimental(ExperimentalCoroutinesApi::class)
    override suspend fun startUpdate(activity: Activity, requestCode: Int): Flow<UpdateState> {
        val updateInfo = appUpdateManager.appUpdateInfo.await()

        return callbackFlow {
            val listener = InstallStateUpdatedListener {
                Timber.tag(TAG).d("In app update state $it")
                channel.offer(it.toUpdateState())
                if (it.isTerminal()) {
                    channel.close()
                }
            }
            appUpdateManager.registerListener(listener)
            appUpdateManager.startUpdateFlowForResult(updateInfo, AppUpdateType.FLEXIBLE,
                activity, requestCode)
            // set state to UNKNOWN, because user has not accepted the update yet
            channel.offer(UpdateState(UpdateState.Status.UNKNOWN))
            awaitClose { appUpdateManager.unregisterListener(listener) }
        }
    }

    override fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }
}

private fun InstallState.toUpdateState(): UpdateState {
    val status = PlayInstallStatus(installStatus()).toUpdateStateStatus()
    return UpdateState(status, installErrorCode())
}

inline class PlayInstallStatus(private val value: Int) {

    fun toUpdateStateStatus(): UpdateState.Status {
        return when (value) {
            InstallStatus.DOWNLOADING -> UpdateState.Status.DOWNLOADING
            InstallStatus.DOWNLOADED -> UpdateState.Status.DOWNLOADED
            InstallStatus.INSTALLING -> UpdateState.Status.INSTALLING
            InstallStatus.INSTALLED -> UpdateState.Status.INSTALLED
            InstallStatus.FAILED -> UpdateState.Status.FAILED
            InstallStatus.CANCELED -> UpdateState.Status.CANCELED
            InstallStatus.REQUIRES_UI_INTENT,
            InstallStatus.PENDING -> UpdateState.Status.PENDING
            InstallStatus.UNKNOWN -> UpdateState.Status.UNKNOWN
            else -> UpdateState.Status.UNKNOWN
        }
    }
}

private fun InstallState.isTerminal(): Boolean = when (installStatus()) {
    InstallStatus.CANCELED,
    InstallStatus.INSTALLED,
    InstallStatus.FAILED -> true

    else -> false
}


@Module
object PlayStoreInAppUpdateModule {

    @JvmStatic
    @Provides
    fun providesInAppUpdateManager(application: Application): InAppUpdateManager {
        val appUpdateManager = AppUpdateManagerFactory.create(application)
        return PlayStoreInAppUpdateManager(appUpdateManager)
    }
}
