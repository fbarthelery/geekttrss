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

import com.hierynomus.gradle.license.LicenseBasePlugin
import com.hierynomus.gradle.license.tasks.LicenseCheck
import com.hierynomus.gradle.license.tasks.LicenseFormat
import nl.javadude.gradle.plugins.license.LicenseExtension
import nl.javadude.gradle.plugins.license.LicensePlugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named

internal fun Project.configureSourceLicenseChecker(): Unit {
    apply<LicensePlugin>()

    configure<LicenseExtension> {
        header = file("$rootDir/config/license/header.txt")
        mapping("java", "SLASHSTAR_STYLE")
        mapping("kt", "SLASHSTAR_STYLE")
        excludes(listOf("**/*.webp", "**/*.png"))
    }

    tasks {
        val checkKotlinFilesLicenseTask = register("checkKotlinFilesLicense", LicenseCheck::class.java) {
            source = fileTree("src").apply {
                include("**/*.kt")
            }
        }

        val formatKotlinFilesLicenseTask = register("formatKotlinFilesLicense", LicenseFormat::class.java) {
            source = fileTree("src").apply {
                include("**/*.kt")
            }
        }

        named<Task>(LicenseBasePlugin.getLICENSE_TASK_BASE_NAME()) {
            dependsOn(checkKotlinFilesLicenseTask)
        }

        named<Task>(LicenseBasePlugin.getFORMAT_TASK_BASE_NAME()) {
            dependsOn(formatKotlinFilesLicenseTask)
        }

    }
}
