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

import com.geekorum.geekdroid.dagger.AppInitializer
import com.geekorum.geekdroid.dagger.initialize
import com.geekorum.ttrss.debugtools.StrictModeInitializer
import com.geekorum.ttrss.di.ApplicationComponent
import com.geekorum.ttrss.di.DaggerApplicationComponent
import dagger.android.support.DaggerApplication

import javax.inject.Inject

/**
 * Initialize global component for the TTRSS application.
 */
open class Application : DaggerApplication() {

    @Inject
    lateinit var appInitializers: MutableSet<AppInitializer>

    override fun onCreate() {
        super.onCreate()
        // initializer StrictMode first
        val strictModeFirst = appInitializers.partition { it is StrictModeInitializer }
        strictModeFirst.first.forEach { it.initialize(this) }
        strictModeFirst.second.forEach { it.initialize(this) }
    }

    override fun applicationInjector(): ApplicationComponent {
        return DaggerApplicationComponent.builder().bindApplication(this).build()
    }
}
