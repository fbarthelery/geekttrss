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

pluginManagement {
    val kotlinVersion: String by settings
    val androidxNavigationVersion: String by settings
    val crashlyticsVersion: String by settings
    val googleServicesVersion: String by settings
    val ossLicensesVersion: String by settings

    plugins {
        kotlin("android") version kotlinVersion
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("androidx.navigation.safeargs.kotlin") version androidxNavigationVersion
        id("com.google.firebase.crashlytics") version crashlyticsVersion
        id("com.google.gms.google-services") version googleServicesVersion
        id("com.google.android.gms.oss-licenses-plugin") version ossLicensesVersion
    }

    repositories {
        gradlePluginPortal()
        jcenter()
        maven {
            // Workaround for bug https://github.com/gradle/kotlin-dsl/issues/1186
            // we publish oss-licenses-plugin 0.9.5.1
            url = uri("https://raw.githubusercontent.com/fbarthelery/play-services-plugins/master/repo/")
        }
        google()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.google.android.gms.oss-licenses-plugin" -> useModule("com.google.android.gms:oss-licenses-plugin:${ossLicensesVersion}")
                "com.google.gms.google-services" -> useModule("com.google.gms:google-services:${googleServicesVersion}")
                "com.google.firebase.crashlytics" -> useModule("com.google.firebase:firebase-crashlytics-gradle:${crashlyticsVersion}")
                "androidx.navigation.safeargs.kotlin" -> useModule("androidx.navigation:navigation-safe-args-gradle-plugin:${androidxNavigationVersion}")
            }
        }
    }
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
            substitute(module("com.geekorum:geekdroid")).with(project(":geekdroid"))
            substitute(module("com.geekorum:geekdroid-firebase")).with(project(":geekdroid-firebase"))
            substitute(module("com.geekorum.geekdroid:geekdroid")).with(project(":geekdroid"))
            substitute(module("com.geekorum.geekdroid:geekdroid-firebase")).with(project(":geekdroid-firebase"))
        }
    }
}
