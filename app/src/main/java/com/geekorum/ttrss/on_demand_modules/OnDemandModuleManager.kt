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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

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

    /**
     * Wether this ModuleManager can install new modules or not
     */
    val canInstallModule: Boolean
}

class OnDemandModuleException
@JvmOverloads constructor(
    message: String? = null, cause: Throwable? = null
) : Exception(message, cause)


abstract class InstallSession(
    val id: Int
) {
    abstract fun cancel()

    abstract suspend fun getSessionState(): State

    abstract fun getSessionStates(): Flow<State>

    abstract suspend fun startUserConfirmationDialog(activity: Activity, code: Int)

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


