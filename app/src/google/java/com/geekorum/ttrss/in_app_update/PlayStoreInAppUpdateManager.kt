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
package com.geekorum.ttrss.in_app_update

import android.app.Application
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallErrorCode
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.ktx.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import com.google.android.play.core.common.IntentSenderForResultStarter as PlayIntentSenderForResultStarter
import com.google.android.play.core.install.model.UpdateAvailability as PlayUpdateAvailability

private val TAG = PlayStoreInAppUpdateManager::class.java.simpleName

class PlayStoreInAppUpdateManager(
    private val appUpdateManager: AppUpdateManager
) : InAppUpdateManager {
    override suspend fun getUpdateAvailability(): UpdateAvailability {
        val updateInfo = appUpdateManager.requestAppUpdateInfo()
        return when (updateInfo.updateAvailability()) {
            PlayUpdateAvailability.UPDATE_AVAILABLE,
            PlayUpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            -> UpdateAvailability.UPDATE_AVAILABLE

            else -> UpdateAvailability.NO_UPDATE
        }
    }

    override suspend fun getUpdateState(): UpdateState {
        val updateInfo = appUpdateManager.requestAppUpdateInfo()
        val status = PlayInstallStatus(updateInfo.installStatus).toUpdateStateStatus()
        return UpdateState(status)
    }

    override suspend fun startUpdate(intentSenderForResultStarter: IntentSenderForResultStarter, requestCode: Int): Flow<UpdateState> {
        val updateInfo = appUpdateManager.requestAppUpdateInfo()
        val playIntentSenderForResultStarter = PlayIntentSenderForResultStarter { intent, finalRequestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options ->
            intentSenderForResultStarter.startIntentSenderForResult(intent, finalRequestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options)
        }
        appUpdateManager.startUpdateFlowForResult(updateInfo, AppUpdateType.FLEXIBLE,
            playIntentSenderForResultStarter, requestCode)
        return appUpdateManager.requestUpdateFlow()
            .map {
                it.toUpdateState().also {state ->
                    Timber.tag(TAG).d("In app update state $state")
                }
            }
    }

    override suspend fun startImmediateUpdate(intentSenderForResultStarter: IntentSenderForResultStarter, requestCode: Int) {
        val updateInfo = appUpdateManager.requestAppUpdateInfo()
        val playIntentSenderForResultStarter = PlayIntentSenderForResultStarter { intent, finalRequestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options ->
            intentSenderForResultStarter.startIntentSenderForResult(intent, finalRequestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options)
        }
        appUpdateManager.startUpdateFlowForResult(updateInfo, AppUpdateType.IMMEDIATE,
            playIntentSenderForResultStarter, requestCode)
    }

    override fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }
}

private fun AppUpdateResult.toUpdateState(): UpdateState {
    return when(this) {
        is AppUpdateResult.Available -> UpdateState(UpdateState.Status.UNKNOWN)
        is AppUpdateResult.NotAvailable -> UpdateState(UpdateState.Status.UNKNOWN)
        is AppUpdateResult.InProgress -> installState.toUpdateState()
        is AppUpdateResult.Downloaded -> UpdateState(UpdateState.Status.DOWNLOADED, InstallErrorCode.NO_ERROR)
    }
}

private fun InstallState.toUpdateState(): UpdateState {
    val status = PlayInstallStatus(installStatus).toUpdateStateStatus()
    return UpdateState(status, installErrorCode)
}

@JvmInline
value class PlayInstallStatus(private val value: Int) {

    fun toUpdateStateStatus(): UpdateState.Status = when (value) {
        InstallStatus.DOWNLOADING -> UpdateState.Status.DOWNLOADING
        InstallStatus.DOWNLOADED -> UpdateState.Status.DOWNLOADED
        InstallStatus.INSTALLING -> UpdateState.Status.INSTALLING
        InstallStatus.INSTALLED -> UpdateState.Status.INSTALLED
        InstallStatus.FAILED -> UpdateState.Status.FAILED
        InstallStatus.CANCELED -> UpdateState.Status.CANCELED
        InstallStatus.PENDING -> UpdateState.Status.PENDING
        InstallStatus.UNKNOWN -> UpdateState.Status.UNKNOWN
        else -> UpdateState.Status.UNKNOWN
    }
}

@Module
@InstallIn(SingletonComponent::class)
object PlayStoreInAppUpdateModule {

    @Provides
    fun providesInAppUpdateManager(application: Application): InAppUpdateManager {
        val appUpdateManager = AppUpdateManagerFactory.create(application)
        return PlayStoreInAppUpdateManager(appUpdateManager)
    }
}
