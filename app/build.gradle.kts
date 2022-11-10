/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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
import com.geekorum.build.enforcedDaggerPlatform
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsPlugin

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.android.gms.oss-licenses-plugin")
    id("com.geekorum.build.android-tests")
    id("com.geekorum.build.android-signing")
    id("com.geekorum.build.android-avdl")
    id("com.geekorum.build.android-release-universal-apk")
    id("com.geekorum.build.play-store-publish")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
}

val composeVersion = "2022.11.00"

androidComponents {
    val major = 1
    val minor = 5
    val patch = 2
    configureVersionChangeset(project, major, minor, patch)
}

android {
    val compileSdkInt: Int by rootProject.extra
    compileSdk = compileSdkInt
    namespace = "com.geekorum.ttrss"
    defaultConfig {
        applicationId = "com.geekorum.ttrss"
        minSdk = 24
        targetSdk = 31

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
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
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

    packagingOptions {
        // Fix: https://github.com/Kotlin/kotlinx.coroutines/issues/2023
        resources {
            excludes += listOf("META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "DebugProbesKt.bin"
            )
        }
    }
}

kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
        // assisted inject create a module without hilt annotation. will be fixed in 0.5.3
        arg("dagger.hilt.disableModulesHaveInstallInCheck", true)
    }
}


dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.5.4")
    implementation("androidx.activity:activity-ktx:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    // androidx ui
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.preference:preference-ktx:1.2.0")

    // compose
    api(platform("androidx.compose:compose-bom:$composeVersion"))
    api("androidx.compose.ui:ui")
    api("androidx.compose.ui:ui-util")
    api("androidx.compose.foundation:foundation")
    api("androidx.compose.material:material")
    api("androidx.compose.material:material-icons-core")
    api("androidx.compose.material:material-icons-extended")
    api("androidx.compose.material3:material3-window-size-class")
    api("androidx.compose.ui:ui-viewbinding")
    api("androidx.activity:activity-compose:1.6.1")
    api("androidx.compose.runtime:runtime-livedata")
    api("androidx.compose.animation:animation-graphics")
    api("androidx.paging:paging-compose:1.0.0-alpha17")
    val accompanistVersion = "0.27.0"
    api("com.google.accompanist:accompanist-insets-ui:$accompanistVersion")
    api("com.google.accompanist:accompanist-swiperefresh:$accompanistVersion")
    api("com.google.accompanist:accompanist-drawablepainter:$accompanistVersion")
    api("com.google.accompanist:accompanist-webview:$accompanistVersion")
    api("androidx.compose.ui:ui-tooling")


    // for layout inspector
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // androidx others
    implementation("androidx.browser:browser:1.4.0")
    implementation("androidx.window:window:1.0.0")
    implementation("androidx.startup:startup-runtime:1.1.1")
    // needed by robolectric
    implementation("androidx.loader:loader:1.1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")

    //geekdroid
    implementation("com.geekorum.geekdroid:geekdroid:master-SNAPSHOT")
    add("googleImplementation", "com.geekorum.geekdroid:geekdroid-firebase:master-SNAPSHOT")

    implementation(project(":htmlparsers"))
    implementation(project(":webapi"))
    implementation(project(":faviKonSnoop"))

    implementation("com.google.android.material:material:1.7.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
    val coilVersion = "2.2.2"
    implementation("io.coil-kt:coil:$coilVersion")
    implementation("io.coil-kt:coil-compose:$coilVersion")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")

    implementation("org.jsoup:jsoup:1.13.1")

    val lifecycleVersion = "2.5.1"
    implementation("androidx.lifecycle:lifecycle-livedata-core-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")
    dualTestImplementation("androidx.arch.core:core-testing:2.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    // dagger
    val daggerVersion = "2.44"
    implementation(enforcedDaggerPlatform(daggerVersion))
    kapt(enforcedDaggerPlatform(daggerVersion))
    implementation("com.google.dagger:dagger:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    kaptTest("com.google.dagger:dagger-compiler:$daggerVersion")
    implementation("com.google.dagger:hilt-android:$daggerVersion")
    implementation("androidx.hilt:hilt-work:1.0.0")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    kapt("com.google.dagger:hilt-compiler:$daggerVersion")
    testImplementation("com.google.dagger:hilt-android-testing:$daggerVersion")
    kaptTest("com.google.dagger:hilt-compiler:$daggerVersion")
    androidTestImplementation("com.google.dagger:hilt-android-testing:$daggerVersion")
    kaptAndroidTest("com.google.dagger:hilt-compiler:$daggerVersion")


    val roomVersion = "2.4.3"
    kapt("androidx.room:room-compiler:$roomVersion")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")

    val workVersion = "2.7.1"
    androidTestImplementation("androidx.work:work-testing:$workVersion")

    implementation(enforcedPlatform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))

    val coroutinesVersion = "1.6.4"
    implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:$coroutinesVersion"))
    testImplementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:$coroutinesVersion"))
    androidTestImplementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:$coroutinesVersion"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

    implementation(enforcedPlatform("com.google.firebase:firebase-bom:28.0.1"))
    add("googleImplementation", "com.google.firebase:firebase-crashlytics")
    // ensure that the free flavor don't get any firebase dependencies
    configurations["freeImplementation"].exclude(group = "com.google.firebase")
    configurations["freeImplementation"].exclude(group = "com.google.android.play")

    add("googleImplementation", "com.google.android.play:core:1.10.0")
    add("googleImplementation", "com.google.android.play:core-ktx:1.8.1")

    // api dependencies for features modules
    api("androidx.appcompat:appcompat:1.5.1")
    api("androidx.work:work-runtime-ktx:$workVersion")
    api("androidx.room:room-runtime:$roomVersion")
    api("androidx.room:room-paging:$roomVersion")
    api("androidx.room:room-ktx:$roomVersion")
    api("androidx.paging:paging-runtime-ktx:3.1.1")
    api("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.squareup.okhttp3:okhttp:4.10.0")
    api("com.jakewharton.timber:timber:5.0.1")

    val navigationVersion = "2.5.3"
    api("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    api("androidx.navigation:navigation-ui-ktx:$navigationVersion")
    api("androidx.navigation:navigation-dynamic-features-fragment:$navigationVersion")

    // fragment testing declare some activities and resources that needs to be in the apk
    // we don't use it. here but it is used in feature modules
    debugImplementation("androidx.fragment:fragment-testing:1.5.4")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.9.1")
}

apply {
    val playServicesActivated = file("google-services.json").exists()
    if (playServicesActivated) {
        // needs to be applied after configuration
        plugin("com.google.gms.google-services")
        plugin("com.google.firebase.crashlytics")
    }
}
