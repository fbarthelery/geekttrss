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
package com.geekorum.ttrss.background_job

import android.content.Context
import androidx.annotation.Keep
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@Keep
class BackgroundJobManagerInitializer : Initializer<BackgroundJobManager> {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BackgroundJobManagerEntryPoint {
        val backgroundJobManager: BackgroundJobManager
    }

    override fun create(context: Context): BackgroundJobManager {
        val entryPoint = EntryPointAccessors.fromApplication(context, BackgroundJobManagerEntryPoint::class.java)
        val backgroundJobManager = entryPoint.backgroundJobManager
        backgroundJobManager.setupPeriodicJobs()
        return backgroundJobManager
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(WorkManagerInitializer::class.java)

}
