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

import org.gradle.api.internal.DynamicObjectAware
import kotlin.reflect.KProperty
import java.net.URI

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        maven {
            // Workaround for bug https://github.com/gradle/kotlin-dsl/issues/1186
            // we publish oss-licenses-plugin 0.9.3.2
            url = URI("https://raw.githubusercontent.com/fbarthelery/play-services-plugins/master/repo/")
        }
        google()
        maven {
            url = URI("https://maven.fabric.io/public")
        }
    }
    resolutionStrategy {
        eachPlugin {
            val id = requested.id.id
            when {
                id == "com.google.android.gms.oss-licenses-plugin" -> useModule("com.google.android.gms:oss-licenses-plugin:${requested.version}")
                id == "com.google.gms.google-services" -> useModule("com.google.gms:google-services:${requested.version}")
                id.startsWith("com.android.") -> useModule("com.android.tools.build:gradle:${requested.version}")
                id == "com.github.triplet.play" -> useModule("com.github.triplet.gradle:play-publisher:${requested.version}")
                id == "io.fabric" -> useModule("io.fabric.tools:gradle:${requested.version}")
                id == "kotlinx-serialization" -> useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}

include(":app")
include(":manage_feeds")

val GEEKDROID_PROJECT_DIR: String? by settings
GEEKDROID_PROJECT_DIR?.let {
    includeBuild(it) {
        dependencySubstitution {
            substitute(module("com.geekorum:geekdroid")).with(project(":library"))
            substitute(module("com.geekorum:geekdroid-firebase")).with(project(":geekdroid-firebase"))
        }
    }
}
