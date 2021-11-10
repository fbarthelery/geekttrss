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
package com.geekorum.ttrss

import android.content.Context
import android.os.StrictMode.allowThreadDiskReads
import com.geekorum.geekdroid.dagger.AppInitializer
import com.geekorum.ttrss.debugtools.StrictModeInitializer
import com.geekorum.ttrss.debugtools.withStrictMode
import com.google.android.play.core.splitcompat.SplitCompat

class GoogleFlavorApplication : Application() {

    /**
     * Allow to load code/resources from on demand modules
     */
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        withStrictMode(allowThreadDiskReads()) {
            SplitCompat.install(this)
        }
    }

    override fun sortAppInitializers(initializers: Set<AppInitializer>): List<AppInitializer> {
        val result = mutableListOf<AppInitializer>()
        val strictModeInitializer = initializers.find { it is StrictModeInitializer }
        strictModeInitializer?.let { result.add(it) }
        result.addAll(initializers)
        return result.distinct()
    }
}
