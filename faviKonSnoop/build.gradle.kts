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
//    alias(libs.plugins.kotlin.jvm)
//    alias(libs.plugins.kotlinx.serialization)
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(enforcedPlatform(kotlin("bom")))

    api(platform(libs.okhttp.bom))
    api(libs.okhttp)
    api(libs.okio)
    implementation(platform(libs.kotlinx.coroutines.bom))
    api(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
    implementation(libs.jsoup)
    implementation(libs.kotlinx.serialization.json.okio)

    testImplementation(libs.truth)
    testImplementation(kotlin("test-junit"))
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
