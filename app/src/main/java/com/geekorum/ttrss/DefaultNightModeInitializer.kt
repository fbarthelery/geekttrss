/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2018 by Frederic-Charles Barthelery.
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
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO
import com.geekorum.geekdroid.dagger.AppInitializer
import com.geekorum.geekdroid.dagger.AppInitializersModule
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet


class DefaultNightModeInitializer : AppInitializer {
    override fun initialize(app: Application) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)
        val nighModeStr = sharedPreferences.getString(SettingsActivity.KEY_THEME, Integer.toString(MODE_NIGHT_AUTO))
        val nighMode = Integer.valueOf(nighModeStr!!)
        AppCompatDelegate.setDefaultNightMode(nighMode)
    }
}

@Module(includes = [AppInitializersModule::class])
class DefaultNightModeModule {

    @Provides
    @IntoSet
    fun providesDefaultNightModeInitializer(): AppInitializer = DefaultNightModeInitializer()
}
