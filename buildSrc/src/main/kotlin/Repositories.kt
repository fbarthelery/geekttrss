/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
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
package com.geekorum.build

import org.gradle.api.artifacts.repositories.MavenArtifactRepository

/**
 * Setup the content of google() repository
 */
fun MavenArtifactRepository.setupGoogleContent() = apply {
    require(name == "Google") { "Only apply to `google()` repository "}
    content {
        includeGroupByRegex("""android\.arch\..*""")
        includeGroupByRegex("""androidx\..*""")
        includeGroupByRegex("""com\.android\..*""")
        includeGroupByRegex("""com\.google\..*""")
        includeGroup("com.crashlytics.sdk.android")
        includeGroup("io.fabric.sdk.android")
        includeGroup("org.chromium.net")
        includeGroup("zipflinger")
        includeGroup("com.android")
    }
}
