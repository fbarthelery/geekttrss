/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2020 by Frederic-Charles Barthelery.
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
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
import androidx.preference.PreferenceManager
import com.geekorum.geekdroid.dagger.AppInitializer
import com.geekorum.geekdroid.dagger.AppInitializersModule
import com.geekorum.ttrss.settings.SettingsActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.multibindings.IntoSet


class DefaultNightModeInitializer : AppInitializer {
    override fun initialize(app: Application) {
        val threadPolicy = StrictMode.allowThreadDiskWrites()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)
        val nighMode =
            sharedPreferences.getString(SettingsActivity.KEY_THEME, MODE_NIGHT_UNSPECIFIED.toString())!!.toInt()
        AppCompatDelegate.setDefaultNightMode(nighMode)
        StrictMode.setThreadPolicy(threadPolicy)
    }
}

@Module(includes = [AppInitializersModule::class])
@InstallIn(ApplicationComponent::class)
object DefaultNightModeModule {

    @Provides
    @IntoSet
    fun providesDefaultNightModeInitializer(): AppInitializer = DefaultNightModeInitializer()
}
