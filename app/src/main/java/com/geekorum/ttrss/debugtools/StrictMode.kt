/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2021 by Frederic-Charles Barthelery.
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

import android.content.Context
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.allowThreadDiskReads
import android.os.strictmode.Violation
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.startup.Initializer
import com.geekorum.ttrss.BuildConfig
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import java.util.concurrent.Executors
import kotlin.jvm.internal.Reflection

private const val TAG = "StrictMode"

/**
 * Configure StrictMode policies
 */
@Keep
class StrictModeInitializer : Initializer<Unit> {
    private val listenerExecutor by lazy { Executors.newSingleThreadExecutor() }

    @delegate:RequiresApi(Build.VERSION_CODES.P)
    private val violationListener: ViolationListener by lazy { ViolationListener() }

    override fun create(context: Context) {
        initialize()
    }

    fun initialize() {
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectCleartextNetwork()
            .detectFileUriExposure()
            .detectLeakedClosableObjects()
            .detectLeakedRegistrationObjects()
            .detectLeakedSqlLiteObjects()
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    detectContentUriWithoutPermission()
                    // because crashlytics don't tag its socket
                    if (BuildConfig.FLAVOR != "google") {
                        detectUntaggedSockets()
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // appcompat use nonsdk
//                        detectNonSdkApiUsage()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    penaltyListener(listenerExecutor, violationListener)
                } else {
                    penaltyLog()
                }
            }
            .build())

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    penaltyListener(listenerExecutor, violationListener)
                } else {
                    penaltyLog()
                }
            }
            .build())
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private inner class ViolationListener : StrictMode.OnThreadViolationListener, StrictMode.OnVmViolationListener {
        override fun onThreadViolation(v: Violation) = onViolation(v)

        override fun onVmViolation(v: Violation) = onViolation(v)

        private fun onViolation(v: Violation) {
            Timber.tag(TAG).e(v, "StrictMode violation")
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

}


/**
 * Initialize some kotlin functionality eagerly to avoid a DiskReadViolation out of our control
 */
@Keep
class KotlinInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        // load Dispatchers.Main at application start
        withStrictMode(allowThreadDiskReads()) {
            Dispatchers.Main
            val k = Reflection.getOrCreateKotlinClass(Object::class.java)
            Timber.d("initialize kotlin Klass with class $k")
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(StrictModeInitializer::class.java)
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
inline fun <R> withStrictMode(originalThreadPolicy: StrictMode.ThreadPolicy, block: () -> R): R {
    try {
        return block()
    } finally {
        StrictMode.setThreadPolicy(originalThreadPolicy)
    }
}
