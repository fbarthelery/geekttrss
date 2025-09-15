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

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.google.android.gms.oss-licenses-plugin" -> useModule("com.google.android.gms:oss-licenses-plugin:${requested.version}")
                "com.google.gms.google-services" -> useModule("com.google.gms:google-services:${requested.version}")
                "dagger.hilt.android.plugin" -> useModule("com.google.dagger:hilt-android-gradle-plugin:${requested.version}")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

include(":app")
include(":manage_feeds")
include(":htmlparsers")
include(":webapi")
include(":faviKonSnoop")

val GEEKDROID_PROJECT_DIR: String? by settings
GEEKDROID_PROJECT_DIR?.let {
    includeBuild(it) {
        dependencySubstitution {
            substitute(module("com.geekorum:geekdroid")).using(project(":geekdroid"))
            substitute(module("com.geekorum:geekdroid-firebase")).using(project(":geekdroid-firebase"))
            substitute(module("com.geekorum.geekdroid:geekdroid")).using(project(":geekdroid"))
            substitute(module("com.geekorum.geekdroid:geekdroid-firebase")).using(project(":geekdroid-firebase"))
        }
    }
}
