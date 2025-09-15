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
package com.geekorum.build

import com.android.build.api.dsl.ApplicationExtension
import com.github.triplet.gradle.play.PlayPublisherExtension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the


// Configuration for "com.github.triplet.play" plugin
// This configuration expects the given properties
// PLAY_STORE_JSON_KEY_FILE: google play console service credentials json file to use
// PLAY_STORE_TRACK: track to publish the build, default to internal but can be set to alpha, beta or production
// PLAY_STORE_FROM_TRACK: track from which to promote a build, default to internal but can be set to alpha, beta or production

internal fun Project.configureAndroidPlayStorePublisher(): Unit {
    apply(plugin = "com.github.triplet.play")
    configure<PlayPublisherExtension> {
        defaultToAppBundles.set(true)
        track.set(properties.getOrDefault("PLAY_STORE_TRACK", "internal") as String)
        fromTrack.set(properties.getOrDefault("PLAY_STORE_FROM_TRACK", "internal") as String)
        serviceAccountCredentials.set(file(properties["PLAY_STORE_JSON_KEY_FILE"]!!))
    }

    val android = the<ApplicationExtension>() as ExtensionAware

    android.extensions.configure<NamedDomainObjectContainer<PlayPublisherExtension>> {
        register("free") {
            enabled.set(false)
        }
        register("google") {
            // we always build the release as a separate step when publishing
            // use artifactDir to be sure that publish don't rebuild it
            artifactDir.set(layout.buildDirectory.dir("outputs/bundle/googleRelease"))
        }
    }

    tasks.apply {
        register("publishToGooglePlayStore") {
            group = "Continuous Delivery"
            description = "Publish project to Google play store"
            dependsOn(named("publishApps"))
        }

        // only there for consistent naming scheme
        register("promoteOnGooglePlayStore") {
            group = "Continuous Delivery"
            description = "Promote project Google play store"
            dependsOn(named("promoteArtifact"))
        }
    }

}
