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

import android.content.Context
import android.os.StrictMode
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
import androidx.preference.PreferenceManager
import androidx.startup.Initializer
import com.geekorum.ttrss.debugtools.StrictModeInitializer
import com.geekorum.ttrss.debugtools.withStrictMode
import com.geekorum.ttrss.settings.SettingsActivity
import com.geekorum.ttrss.settings.SettingsInitializer

@Keep
class DefaultNightModeInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        withStrictMode(StrictMode.allowThreadDiskWrites()) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val nighMode =
                sharedPreferences.getString(SettingsActivity.KEY_THEME, MODE_NIGHT_UNSPECIFIED.toString())!!.toInt()
            AppCompatDelegate.setDefaultNightMode(nighMode)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(StrictModeInitializer::class.java, SettingsInitializer::class.java)
}
