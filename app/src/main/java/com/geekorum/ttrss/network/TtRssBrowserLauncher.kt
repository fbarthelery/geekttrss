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
package com.geekorum.ttrss.network

import android.content.Context
import android.net.Uri
import com.geekorum.geekdroid.network.BrowserLauncher
import com.geekorum.ttrss.R
import javax.inject.Inject

/**
 * A wrapper of [BrowserLauncher] with customization for the ttrss app
 */
class TtRssBrowserLauncher @Inject constructor(
    private val delegate: BrowserLauncher
) {
    private val PREFFERED_PACKAGES_LIST = arrayOf("org.mozilla.focus")

    fun warmUp() {
        delegate.warmUp(this::orderPreferredPackages)
    }

    fun mayLaunchUrl(vararg uris: Uri) = delegate.mayLaunchUrl(*uris)

    fun launchUrl(context: Context, uri: Uri) {
        delegate.launchUrl(context, uri) {
            val tabColor = context.getColor(R.color.primary)
            addDefaultShareMenuItem()
                .setToolbarColor(tabColor)
                .setShowTitle(true)
                .enableUrlBarHiding()
        }
    }

    fun shutdown() = delegate.shutdown()

    private fun orderPreferredPackages(availablePackages: List<String>): List<String> {
        return (PREFFERED_PACKAGES_LIST.filter { availablePackages.contains(it) }
                + availablePackages)
            .distinct()
    }

}
