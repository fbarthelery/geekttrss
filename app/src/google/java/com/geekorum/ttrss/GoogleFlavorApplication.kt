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
import com.geekorum.ttrss.debugtools.StrictModeInitializer
import com.geekorum.ttrss.di.ApplicationComponent
import com.geekorum.ttrss.di.DaggerGoogleFlavorApplicationComponent
import com.geekorum.ttrss.logging.CrashlyticsInitializer

class GoogleFlavorApplication : Application() {

    override fun applicationInjector(): ApplicationComponent {
        return DaggerGoogleFlavorApplicationComponent.builder().bindApplication(this).build()
    }

    override fun sortAppInitializers(initializers: Set<AppInitializer>): List<AppInitializer> {
        val result = mutableListOf<AppInitializer>()
        val strictModeInitializer = initializers.find { it is StrictModeInitializer }
        strictModeInitializer?.let { result.add(it) }
        val crashlyticsInitializer = initializers.find { it is CrashlyticsInitializer }
        crashlyticsInitializer?.let { result.add(it) }
        result.addAll(initializers)
        return result.distinct()
    }
}
