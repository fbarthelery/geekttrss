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
plugins {
    `kotlin-dsl`
}

version = "1.0"

repositories {
    gradlePluginPortal()
    google()
}

dependencies {
    // play-publisher depends on AGP original not api. So we need to include it here
    // in order to be in the correct classpath
    implementation("com.android.tools.build:gradle:8.0.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")

    implementation("com.android.tools.build:gradle-api:8.0.2")
    implementation("gradle.plugin.com.hierynomus.gradle.plugins:license-gradle-plugin:0.16.1")
    implementation("com.github.triplet.gradle:play-publisher:3.8.4")
    implementation("com.geekorum.gradle.avdl:flydroid:0.0.4")

    // fix https://github.com/google/dagger/issues/3068
    implementation("com.squareup:javapoet:1.13.0")
}
