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
package com.geekorum.build

import com.android.build.api.variant.*
import com.android.build.gradle.AppPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType


internal fun Project.makeAssembleReleaseTaskGeneratesUniversalApk() {
    val variantsName = mutableListOf<String>()
    plugins.withType<AppPlugin> {
        val android = project.extensions.getByType(AndroidComponentsExtension::class.java)
        android.onVariants(android.selector().withBuildType("release")) {
            variantsName += it.name
        }
    }

    // this needs to run after evaluation so the different tasks have been created
    afterEvaluate {
        tasks.apply {
            named("assembleRelease") {
                for (variant in variantsName) {
                    val packageTask = named("package${variant.capitalize()}UniversalApk")
                    dependsOn(packageTask)
                }
            }
        }
    }
}
