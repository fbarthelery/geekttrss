/**
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
import java.net.URI

plugins {
    `kotlin-dsl`
}

version = "1.0"

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}


repositories {
    gradlePluginPortal()
    jcenter()
    google()
    maven {
        // Workaround for genymotion plugin not working on gradle 5.0
        // we publish 1.4.1 version with fixes
        url = URI("https://raw.githubusercontent.com/fbarthelery/genymotion-gradle-plugin/master/repo/")
    }
}

dependencies {
    implementation("com.android.tools.build:gradle:3.2.1")
    implementation("com.genymotion:plugin:1.4.1")
    implementation("gradle.plugin.nl.javadude.gradle.plugins:license-gradle-plugin:0.14.0")
    implementation("com.github.triplet.gradle:play-publisher:2.0.0-rc2")
}
