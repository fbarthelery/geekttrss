/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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

import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
private class NoInAppUpdateManager: InAppUpdateManager {
    override suspend fun getUpdateState(): UpdateState {
        return UpdateState(UpdateState.Status.UNKNOWN)
    }

    override suspend fun getUpdateAvailability(): UpdateAvailability {
        return UpdateAvailability.NO_UPDATE
    }

    override suspend fun startUpdate(intentSenderForResultStarter: IntentSenderForResultStarter, requestCode: Int): Flow<UpdateState> {
        return flowOf(UpdateState(UpdateState.Status.FAILED))
    }

    override suspend fun startImmediateUpdate(intentSenderForResultStarter: IntentSenderForResultStarter, requestCode: Int) {
        // do nothing
    }

    override fun completeUpdate() {
        // do nothing
    }
}

@Module
@DisableInstallInCheck
object NoInAppUpdateModule {

    @Provides
    fun providesInAppUpdateManager(): InAppUpdateManager = NoInAppUpdateManager()

}
