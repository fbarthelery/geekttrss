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
package com.geekorum.ttrss.network

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.geekorum.geekdroid.network.BrowserLauncher
import com.geekorum.ttrss.R
import com.geekorum.ttrss.settings.SettingsActivity
import javax.inject.Inject

/**
 * A wrapper of [BrowserLauncher] with customization for the ttrss app
 */
class TtRssBrowserLauncher @Inject constructor(
    private val delegate: BrowserLauncher,
    private val appPreferences: SharedPreferences
) {
    private val PREFFERED_PACKAGES_LIST = arrayOf("org.mozilla.focus")

    fun warmUp() {
        delegate.warmUp(this::orderPreferredPackages)
    }

    fun mayLaunchUrl(vararg uris: Uri) = delegate.mayLaunchUrl(*uris)

    fun launchUrl(context: Context, uri: Uri) {
        delegate.launchUrl(context, uri) {
            val lightColorScheme = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(context.getColor(R.color.material_blue_grey_500))
                .build()
            val darkColorScheme = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(context.getColor(R.color.material_blue_grey_300))
                .build()

            setShareState(CustomTabsIntent.SHARE_STATE_ON)
                .setDefaultColorSchemeParams(lightColorScheme)
                .setShowTitle(true)
                .setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
                .setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_LIGHT, lightColorScheme)
                .setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_DARK, darkColorScheme)
                .setUrlBarHidingEnabled(true)
        }
    }

    fun shutdown() = delegate.shutdown()

    private fun orderPreferredPackages(availablePackages: List<String>): List<String> {
        val preferredPackages = (arrayOf(
            appPreferences.getString(SettingsActivity.KEY_IN_APP_BROWSER_ENGINE, null)) + PREFFERED_PACKAGES_LIST
            ).filterNotNull()

        return (preferredPackages.filter { availablePackages.contains(it) }
            + availablePackages)
            .distinct()
    }

}
