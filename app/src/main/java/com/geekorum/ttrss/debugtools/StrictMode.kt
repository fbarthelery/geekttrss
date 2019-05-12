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
package com.geekorum.ttrss.debugtools

import android.app.Application
import android.content.ContentResolver
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.allowThreadDiskReads
import android.util.Log
import com.geekorum.geekdroid.dagger.AppInitializer
import com.geekorum.geekdroid.dagger.AppInitializersModule
import com.geekorum.ttrss.BuildConfig
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject

private const val TAG = "StrictMode"

/**
 * Configure StrictMode policies
 */
class StrictModeInitializer @Inject constructor(
    private val contentResolver: ContentResolver
) : AppInitializer {
    override fun initialize(app: Application) {
        val shouldBeFatal = shouldBeFatal()
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectCleartextNetwork()
            .detectFileUriExposure()
            .detectLeakedClosableObjects()
            .detectLeakedRegistrationObjects()
            .detectLeakedSqlLiteObjects()
            .penaltyLog()
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    detectContentUriWithoutPermission()
                    // because crashlytics don't tag its socket
                    if (BuildConfig.FLAVOR != "google" || !shouldBeFatal) {
                        detectUntaggedSockets()
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // appcompat use nonsdk
//                        detectNonSdkApiUsage()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    penaltyListener(Executors.newSingleThreadExecutor(), StrictMode.OnVmViolationListener {
                        val priority = if (shouldBeFatal) Log.ERROR else Log.WARN
                        Timber.tag(TAG).log(priority, it, "StrictMode violation")
                    })
                }
                if (shouldBeFatal) {
                    penaltyDeath()
                }
            }
            .build())

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    penaltyListener(Executors.newSingleThreadExecutor(), StrictMode.OnThreadViolationListener {
                        val priority = if (shouldBeFatal) Log.ERROR else Log.WARN
                        Timber.tag(TAG).log(priority, it, "StrictMode violation")
                    })
                }
                if (shouldBeFatal) {
                    penaltyDeath()
                }
            }
            .build())
    }

    private fun shouldBeFatal(): Boolean {
        return isFirebaseTestDevice(contentResolver) || BuildConfig.DEBUG
    }
}


@Module(includes = [AppInitializersModule::class, CoroutinesInitializerModule::class])
abstract class StrictModeModule {
    @Binds
    @IntoSet
    abstract fun bindStrictModeInitializer(strictModeInitializer: StrictModeInitializer): AppInitializer
}


class CoroutinesAsyncInitializer @Inject constructor() : AppInitializer {
    override fun initialize(app: Application) {
        // load Dispatchers.Main at application start
        withStrictMode(allowThreadDiskReads()) {
            Dispatchers.Main
        }
    }
}

@Module
abstract class CoroutinesInitializerModule {
    @Binds
    @IntoSet
    abstract fun bindCoroutinesAsyncInitializer(oroutinesAsyncInitializer: CoroutinesAsyncInitializer): AppInitializer

}

/**
 * Run [block] then restore [originalThreadPolicy]
 * Allows to write code like
 * ```
 *  withStrictMode(allowThreadDiskReads()) {
 *      //read disk data
 *  }
 *
 * ```
 */
inline fun withStrictMode(originalThreadPolicy: StrictMode.ThreadPolicy, block: () -> Unit) {
    block()
    StrictMode.setThreadPolicy(originalThreadPolicy)
}
