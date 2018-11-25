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
package com.geekorum.build

import com.android.build.gradle.AppExtension
import com.github.triplet.gradle.play.PlayPublisherExtension
import com.github.triplet.gradle.play.PlayPublisherPlugin
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the


// Configuration for "com.github.triplet.play" plugin
// This configuration expects the given properties
// PLAY_STORE_JSON_KEY_FILE: google play console service credentials json file to use
// PLAY_STORE_TRACK: track to publish the build, default to internal but can be set to alpha, beta or production
// PLAY_STORE_FROM_TRACK: track from which to promote a build, default to internal but can be set to alpha, beta or production

internal fun Project.configureAndroidPlayStorePublisher(): Unit {
    apply<PlayPublisherPlugin>()
    configure<PlayPublisherExtension> {
        defaultToAppBundles = false
        serviceAccountCredentials = file(properties["PLAY_STORE_JSON_KEY_FILE"]!!)
        track = properties.getOrDefault("PLAY_STORE_TRACK", "internal") as String
        fromTrack = properties.getOrDefault("PLAY_STORE_FROM_TRACK", "internal") as String
    }

    val android = the<AppExtension>()
    val androidExtensionsAware = android as ExtensionAware
    val playAccountConfigs =
        androidExtensionsAware.extensions["playConfigs"] as NamedDomainObjectContainer<PlayPublisherExtension>

    playAccountConfigs.register("google") {
        serviceAccountCredentials = file(properties["PLAY_STORE_JSON_KEY_FILE"]!!)
    }
}
