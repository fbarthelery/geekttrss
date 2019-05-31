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
package com.geekorum.build

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.TestOptions
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.kotlin

const val espressoVersion = "3.2.0"
const val androidxTestRunnerVersion = "1.1.1"
const val androidxTestCoreVersion = "1.1.0"
const val androidxTestExtVersion = "1.1.0"
const val robolectricVersion = "4.1"


/*
 * Configuration for espresso and robolectric usage in an Android project
 */
internal fun Project.configureTests() {
    val kotlinVersion: String by rootProject.extra

    extensions.configure<BaseExtension>("android") {
        defaultConfig {
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            testInstrumentationRunnerArgument("clearPackageData", "true")
        }

        testOptions {
            execution = "ANDROIDX_TEST_ORCHESTRATOR"
            animationsDisabled = true

            unitTests(closureOf<TestOptions.UnitTestOptions> {
                isIncludeAndroidResources = true
            })
        }
    }


    dependencies {
        dualTestImplementation(kotlin("test-junit", kotlinVersion))

        androidTestUtil("androidx.test:orchestrator:$androidxTestRunnerVersion")
        androidTestImplementation("androidx.test:runner:$androidxTestRunnerVersion")
        dualTestImplementation("androidx.test.ext:junit-ktx:$androidxTestExtVersion")

        dualTestImplementation("androidx.test:core-ktx:$androidxTestCoreVersion")
        dualTestImplementation("androidx.test:rules:$androidxTestRunnerVersion")

        dualTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
        dualTestImplementation("androidx.test.espresso:espresso-contrib:$espressoVersion")
        dualTestImplementation("androidx.test.espresso:espresso-intents:$espressoVersion")

        // assertions
        dualTestImplementation("com.google.truth:truth:0.44")
        dualTestImplementation("androidx.test.ext:truth:$androidxTestExtVersion")

        // fix for guava listenablefuture conflict with application runtime version
        dualTestImplementation("com.google.guava:listenablefuture:1.0") {
            isForce = true
        }

        // mock
        testImplementation("io.mockk:mockk:1.9")
        testImplementation("org.robolectric:robolectric:$robolectricVersion")

    }
}

fun DependencyHandler.dualTestImplementation(dependencyNotation: Any) {
    add("androidTestImplementation", dependencyNotation)
    add("testImplementation", dependencyNotation)
}

fun DependencyHandler.dualTestImplementation(dependencyNotation: Any, action: ExternalModuleDependency.() -> Unit) {
    val closure = closureOf(action)
    add("androidTestImplementation", dependencyNotation, closure)
    add("testImplementation", dependencyNotation, closure)
}

internal fun DependencyHandler.`androidTestImplementation`(dependencyNotation: Any): Dependency? =
    add("androidTestImplementation", dependencyNotation)


internal fun DependencyHandler.`androidTestUtil`(dependencyNotation: Any): Dependency? =
    add("androidTestUtil", dependencyNotation)

internal fun DependencyHandler.`testImplementation`(dependencyNotation: Any): Dependency? =
    add("testImplementation", dependencyNotation)

