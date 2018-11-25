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

plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
    `java-gradle-plugin`
}


version = "1.0"

repositories {
    gradlePluginPortal()
    jcenter()
    google()
}

dependencies {
    implementation("com.android.tools.build:gradle:3.2.1")
    implementation("com.genymotion:plugin:1.4")
    implementation("gradle.plugin.nl.javadude.gradle.plugins:license-gradle-plugin:0.14.0")
    implementation("com.github.triplet.gradle:play-publisher:2.0.0-rc2")
}
