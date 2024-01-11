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
plugins {
    `kotlin-dsl`
}

version = "1.0"

repositories {
    gradlePluginPortal()
    google()
}

// see https://github.com/gradle/gradle/issues/17963
fun Provider<PluginDependency>.gav(): String {
    val t = get()
    val id = t.pluginId
    val version = t.version
    return "$id:$id.gradle.plugin:$version"
}

dependencies {
    // play-publisher depends on AGP original not api. So we need to include it here
    // in order to be in the correct classpath
    implementation(libs.android.gradle.plugin)
    implementation(libs.plugins.kotlin.android.gav())

    implementation(libs.android.gradle.plugin.api)
    implementation(libs.plugins.license.gradle.plugin.gav())
    implementation(libs.plugins.gradle.play.publisher.gav())
    implementation(libs.plugins.com.geekorum.avdl.flydroid.gav())

    // fix https://github.com/google/dagger/issues/3068
    implementation("com.squareup:javapoet:1.13.0")
}
