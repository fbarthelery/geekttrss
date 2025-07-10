/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
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
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.StrictMode
import android.view.ContextThemeWrapper
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.preference.PreferenceManager
import androidx.startup.Initializer
import com.geekorum.ttrss.debugtools.StrictModeInitializer
import com.geekorum.ttrss.debugtools.withStrictMode
import com.geekorum.ttrss.settings.SettingsActivity
import com.geekorum.ttrss.settings.SettingsInitializer
import timber.log.Timber

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


/**
 * Run [content] by overriding [Configuration] based on [SettingsActivity.KEY_THEME] preferences.
 *
 * This is used to changes the configuration like [AppCompatDelegate.setDefaultNightMode]
 */
@OptIn(ExperimentalStdlibApi::class)
@Composable
fun WithNightModePreferencesTheme(
    content : @Composable () -> Unit
) {
    val uiNightModeFromPreferences by produceThemeConfigurationPreferences()
    Timber.i("night mode from preferences ${uiNightModeFromPreferences.toHexString()}")
    OverriddenConfiguration(
        configuration = Configuration().apply {
            // Initialize from the current configuration
            updateFrom(LocalConfiguration.current)

            // Override uiMode
            val newNightMode = uiMode and Configuration.UI_MODE_NIGHT_MASK.inv() or uiNightModeFromPreferences
            Timber.i("newNightMode = ${newNightMode.toHexString()}")
            this.uiMode = newNightMode
        },
        content = content
    )
}

@Composable
private fun produceThemeConfigurationPreferences(): State<Int> {
    val context = LocalContext.current
    // get default night mode config from application context
    val appConfig: Configuration =
        context.applicationContext.resources.configuration
    val defaultNighMode = appConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK

    fun convertNightModeSettingToConfiguration(value: String) = when (value.toIntOrNull()) {
        // -1 MODE_NIGHT_UNSPECIFIED
        -1 -> defaultNighMode
        // 1 MODE_NIGHT_NO
        1 -> Configuration.UI_MODE_NIGHT_NO
        // 2 MODE_NIGHT_UES
        2 -> Configuration.UI_MODE_NIGHT_YES
        else -> {
            Timber.w("Invalid nightmode settings $value. use unspecified")
            defaultNighMode
        }
    }

    return produceState(initialValue = defaultNighMode, key1 = context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        // -1 MODE_NIGHT_UNSPECIFIED
        val nightModeValue = sharedPreferences.getString(SettingsActivity.KEY_THEME, "-1")!!
        value = convertNightModeSettingToConfiguration(nightModeValue)
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == SettingsActivity.KEY_THEME) {
                    val updatedSettingsValue = sharedPreferences.getString(SettingsActivity.KEY_THEME, "-1")!!
                    value = convertNightModeSettingToConfiguration(updatedSettingsValue)
                }
            }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitDispose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}


/**
 * Overrides the compositions locals related to the given [configuration].
 */
@Composable
private fun OverriddenConfiguration(
    configuration: Configuration,
    content: @Composable () -> Unit
) {
    // We don't override the theme, but we do want to override the configuration and this seems
    // convenient to do so
    val newContext = ContextThemeWrapper(LocalContext.current, 0).apply {
        applyOverrideConfiguration(configuration)
    }

    CompositionLocalProvider(
        LocalContext provides newContext,
        LocalConfiguration provides configuration,
        content = content,
    )
}