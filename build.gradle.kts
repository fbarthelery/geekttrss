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
import com.geekorum.build.*

plugins {
//    alias(libs.plugins.android.application) apply false
//    alias(libs.plugins.android.dynamic.feature) apply false
//    alias(libs.plugins.kotlin.android) apply false
//    alias(libs.plugins.kotlin.jvm) apply false
//    alias(libs.plugins.kotlin.kapt) apply false
    kotlin("plugin.serialization") version libs.versions.kotlin.asProvider().get() apply false
    alias(libs.plugins.kotlin.ksp) apply false
    // these should not be needed but for an unknown reason they get applied
    // with bad ordering if not there. or they can't be applied dynamically
    // version used is in gradle.properties
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

allprojects {
    repositories {
        google().setupGoogleContent()
        mavenCentral()
        // for geekdroid
        maven {
            url = uri("https://jitpack.io")
        }
    }
 }

tasks.register("clean", Delete::class.java) {
    delete(layout.buildDirectory)
}
