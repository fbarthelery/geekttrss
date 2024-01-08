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

import com.geekorum.build.configureVersionChangeset
import com.geekorum.build.dualTestImplementation
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsPlugin

plugins {
    id("com.geekorum.build.conventions.android-application")
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.com.geekorum.gms.oss.license)
    id("com.geekorum.build.android-tests")
    id("com.geekorum.build.android-avdl")
    alias(libs.plugins.androidx.navigation.safeargs.kotlin)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.androidx.room)
}

// workaround bug https://issuetracker.google.com/issues/275534543
// try to remove it on next navigation-safe-args plugin release
buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0")
    }
}

androidComponents {
    val major = 1
    val minor = 6
    val patch = 6
    configureVersionChangeset(project, major, minor, patch)
}

android {
    namespace = "com.geekorum.ttrss"
    defaultConfig {
        applicationId = "com.geekorum.ttrss"
        targetSdk = 34

        sourceSets {
            named("androidTest") {
                assets.srcDir(files("$projectDir/schemas"))
            }
        }
    }


    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable += listOf("MissingTranslation")
    }

    buildTypes {
        named("release") {
            postprocessing {
                isRemoveUnusedCode = true
//                isRemoveUnusedResources = true
                isObfuscate = false
                isOptimizeCode = true
                proguardFile("proguard-rules.pro")
            }
        }
    }

    buildFeatures {
        dataBinding = true
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    flavorDimensions += "distribution"
    productFlavors {
        register("free") {
            dimension = "distribution"
            applicationIdSuffix = ".free"
            // disable mapping file upload for free flavor as it doesn't work and the free flavor
            // doesn't use crashlytics anyway
            plugins.withType<CrashlyticsPlugin>{
                (this@register as ExtensionAware).extensions.configure<CrashlyticsExtension> {
                    mappingFileUploadEnabled = false
                }
            }
        }

        register("google") {
            dimension = "distribution"
            versionNameSuffix = "-google"
        }
    }

    dynamicFeatures += setOf(":manage_feeds")

    packaging {
        // Fix: https://github.com/Kotlin/kotlinx.coroutines/issues/2023
        resources {
            excludes += listOf(
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "DebugProbesKt.bin"
            )
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas/")
}

ksp {
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}


dependencies {

    implementation(libs.androidx.core)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity)
    implementation(libs.kotlinx.datetime)

    // androidx ui
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.preferences)

    // compose
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.util)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.core)
    api(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.compose.material3.window.sizes)
    api(libs.androidx.compose.ui.viewbinding)
    api(libs.androidx.activity.compose)
    api(libs.androidx.compose.runtime.livedata)
    api(libs.androidx.compose.animation.graphics)
    api(libs.androidx.paging.compose)
    api(libs.accompanist.drawablepainter)
    api(libs.accompanist.webview)
    api(libs.androidx.compose.ui.tooling)
    api(libs.coil.compose)


    // for layout inspector
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // androidx others
    implementation(libs.androidx.browser)
    implementation(libs.androidx.startup)

    implementation(enforcedPlatform(libs.kotlinx.serialization.bom))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization.converter)

    //geekdroid
    implementation(libs.geekdroid)
    add("googleImplementation", libs.geekdroid.firebase)
    implementation(libs.aboutoss.core)

    implementation(project(":htmlparsers"))
    implementation(project(":webapi"))
    implementation(project(":faviKonSnoop"))

    implementation(libs.android.material)

    implementation(enforcedPlatform(libs.okhttp.bom))
    implementation(libs.okhttp.logging.interceptor)
    testImplementation(libs.okhttp.mockwebserver)
    implementation(libs.coil)

    implementation(libs.jsoup)

    implementation(libs.androidx.lifecycle.livedata.core)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    dualTestImplementation(libs.androidx.arch.core.testing)
    implementation(libs.androidx.lifecycle.viewmodel.compose)


    implementation(libs.dagger)
    ksp(libs.dagger.compiler)
    kspTest(libs.dagger.compiler)
    implementation(libs.dagger.hilt.android)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    ksp(libs.dagger.hilt.compiler)
    testImplementation(libs.dagger.hilt.android.testing)
    kspTest(libs.dagger.hilt.compiler)
    androidTestImplementation(libs.dagger.hilt.android.testing)
    kspAndroidTest(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)


    ksp(libs.androidx.room.compiler)
    androidTestImplementation(libs.androidx.room.testing)

    androidTestImplementation(libs.androidx.work.testing)

    implementation(enforcedPlatform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))

    implementation(enforcedPlatform(libs.kotlinx.coroutines.bom))
    testImplementation(enforcedPlatform(libs.kotlinx.coroutines.bom))
    androidTestImplementation(enforcedPlatform(libs.kotlinx.coroutines.bom))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.jdk8)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.kotlinx.coroutines.test)

    implementation(enforcedPlatform(libs.firebase.bom))
    add("googleImplementation", libs.firebase.crashlytics)
    // ensure that the free flavor don't get any firebase dependencies
    configurations["freeImplementation"].exclude(group = "com.google.firebase")
    configurations["freeImplementation"].exclude(group = "com.google.android.play")
    configurations["freeImplementation"].exclude(group = "com.google.android.gms")

    add("googleImplementation", libs.google.play.feature.delivery)
    add("googleImplementation", libs.google.play.app.update)
    add("googleImplementation", libs.google.play.review)
    add("googleImplementation", libs.gms.play.services.base)
    add("googleImplementation", libs.androidx.navigation.dynamic.features.fragment)

    // api dependencies for features modules
    api(libs.androidx.appcompat)
    api(libs.androidx.work.runtime)
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.paging)
    api(libs.androidx.room)
    api(libs.androidx.paging)
    api(libs.retrofit)
    api(enforcedPlatform(libs.okhttp.bom))
    api(libs.okhttp)
    api(libs.timber)

    api(libs.androidx.navigation.fragment)
    api(libs.androidx.navigation.ui)
    api(libs.androidx.navigation.compose)

    debugImplementation(libs.androidx.fragment.testing.manifest)

    debugImplementation(libs.leakcanary.android)

    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

apply {
    val playServicesActivated = file("google-services.json").exists()
    if (playServicesActivated) {
        // needs to be applied after configuration
        plugin("com.google.gms.google-services")
        plugin("com.google.firebase.crashlytics")
    }
}
