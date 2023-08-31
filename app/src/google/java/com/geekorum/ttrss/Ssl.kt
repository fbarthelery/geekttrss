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
package com.geekorum.ttrss

import android.app.Application
import android.content.Intent
import com.geekorum.geekdroid.dagger.AppInitializer
import com.geekorum.geekdroid.dagger.AppInitializersModule
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import timber.log.Timber


private class GmsSecurityProviderInitializer : AppInitializer {
    override fun initialize(app: Application) {
        ProviderInstaller.installIfNeededAsync(app, object : ProviderInstaller.ProviderInstallListener {
            override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent?) {
                Timber.e("Unable to install Google play services security provider. error : $errorCode")
                GoogleApiAvailability.getInstance().apply {
                    if (isUserResolvableError(errorCode)) {
                        showErrorNotification(app, errorCode)
                    }
                }
            }

            override fun onProviderInstalled() {
                Timber.i("Google play services security provider successfully installed")
            }

        })
    }
}

@Module(includes = [AppInitializersModule::class])
@InstallIn(SingletonComponent::class)
class GmsSecurityProviderModule {

    @Provides
    @IntoSet
    fun providesGmsSecurityProviderInitializer(): AppInitializer = GmsSecurityProviderInitializer()
}
