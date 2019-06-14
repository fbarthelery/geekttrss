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
package com.geekorum.ttrss

import android.app.Activity
import androidx.work.Configuration
import androidx.work.WorkManager
import com.geekorum.geekdroid.dagger.AppInitializer
import com.geekorum.geekdroid.dagger.DaggerDelegateWorkersFactory
import com.geekorum.geekdroid.dagger.initialize
import com.geekorum.ttrss.debugtools.StrictModeInitializer
import com.geekorum.ttrss.di.ApplicationComponent
import com.geekorum.ttrss.di.DaggerApplicationComponent
import dagger.android.support.DaggerApplication
import javax.inject.Inject

/**
 * Initialize global component for the TTRSS application.
 */
open class Application : DaggerApplication(), Configuration.Provider {

    @Inject
    lateinit var appInitializers: MutableSet<AppInitializer>

    @Inject
    lateinit var workerFactory: DaggerDelegateWorkersFactory

    open val applicationComponent by lazy {
        DaggerApplicationComponent.builder().bindApplication(this).build()
    }

    override fun onCreate() {
        super.onCreate()
        // a few initializers need to be set up before others
        sortAppInitializers(appInitializers).initialize(this)
    }

    protected open fun sortAppInitializers(initializers: Set<AppInitializer>): List<AppInitializer> {
        val result = mutableListOf<AppInitializer>()
        val strictModeInitializer = initializers.find { it is StrictModeInitializer }
        strictModeInitializer?.let { result.add(it) }
        result.addAll(initializers)
        return result.distinct()
    }

    override fun applicationInjector(): ApplicationComponent {
        return applicationComponent
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

}

val Activity.applicationComponent: ApplicationComponent
        get() = (applicationContext as Application).applicationComponent
