import com.geekorum.build.enforcedCoroutinesPlatform

/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2019 by Frederic-Charles Barthelery.
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
    kotlin("jvm")
    id("kotlinx-serialization")
    id("com.geekorum.build.source-license-checker")
}

dependencies {
    val kotlinVersion: String by rootProject.extra
    implementation(kotlin("stdlib-jdk8"))
    implementation(enforcedPlatform(kotlin("bom", kotlinVersion)))

    implementation("androidx.annotation:annotation:1.1.0")
    implementation("javax.inject:javax.inject:1")
    implementation("com.squareup.retrofit2:retrofit:2.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.11.0")

    val coroutinesVersion: String by rootProject.extra
    implementation(enforcedCoroutinesPlatform(coroutinesVersion))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")


    testImplementation("com.google.truth:truth:0.45")
    testImplementation(kotlin("test-junit", kotlinVersion))
}