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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

/**
 * Allows to query some service to know if an Application update is available,
 * and if so, to download and install it.
 */
@UseExperimental(ExperimentalCoroutinesApi::class)
interface InAppUpdateManager {

    suspend fun getUpdateAvailability(): UpdateAvailability

    suspend fun getUpdateState(): UpdateState

    /**
     * Start the update process.
     */
    suspend fun startUpdate(activity: Activity, requestCode: Int): Flow<UpdateState>

    /**
     * If the [UpdateState.status] is [UpdateState.Status.DOWNLOADED], this method need to be called
     * complete the install and restart the application
     */
    fun completeUpdate()

}

enum class UpdateAvailability {
    UPDATE_AVAILABLE, NO_UPDATE
}

data class UpdateState(
    val status: Status,
    val errorCode: Any? = null
) {
    enum class Status {
        UNKNOWN, PENDING, DOWNLOADING,
        DOWNLOADED, INSTALLING, INSTALLED,
        FAILED, CANCELED
    }
}
