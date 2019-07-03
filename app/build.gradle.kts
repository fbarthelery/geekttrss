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

import com.geekorum.build.computeChangesetVersionCode
import com.geekorum.build.configureJavaVersion
import com.geekorum.build.dualTestImplementation
import com.geekorum.build.enforcedCoroutinesPlatform
import com.geekorum.build.enforcedDaggerPlatform
import com.geekorum.build.getChangeSet
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.kotlin

plugins {
    id("com.android.application")
    id("com.google.android.gms.oss-licenses-plugin")
    kotlin("android")
    kotlin("kapt")
    id("com.geekorum.build.android-tests")
    id("com.geekorum.build.android-signing")
    id("com.geekorum.build.android-genymotion")
    id("com.geekorum.build.android-release-universal-apk")
    id("com.geekorum.build.source-license-checker")
    id("com.geekorum.build.play-store-publish")
}

android {
    val compileSdkVersion: String by rootProject.extra
    setCompileSdkVersion(compileSdkVersion)
    defaultConfig {
        applicationId = "com.geekorum.ttrss"
        minSdkVersion(24)
        targetSdkVersion(28)
        val major = 1
        val minor = 3
        val patch = 1
        versionCode = computeChangesetVersionCode(major, minor, patch)
        versionName = "$major.$minor.$patch"
        buildConfigField("String", "REPOSITORY_CHANGESET", "\"${getChangeSet()}\"")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf(
                    "room.schemaLocation" to "$projectDir/schemas")
            }
        }
        sourceSets {
            named("androidTest") {
                assets.srcDir(files("$projectDir/schemas"))
            }
        }
    }


    lintOptions {
        isAbortOnError = false
        isCheckReleaseBuilds = false
        disable("MissingTranslation")
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
        named("debug") {
            // prevent fabric for generating build id which hurts gradle task caching
            // see https://docs.fabric.io/android/crashlytics/build-tools.html#optimize-builds-when-you-re-not-proguarding-or-using-beta-by-crashlytics
            (this as ExtensionAware).extra["alwaysUpdateBuildId"] = false
        }
    }

    dataBinding {
        isEnabled = true
    }

    configureJavaVersion()

    flavorDimensions("distribution")
    productFlavors {
        register("free") {
            dimension = "distribution"
            applicationIdSuffix = ".free"
        }

        register("google"){
            dimension = "distribution"
            versionNameSuffix = "-google"
        }
    }

    dynamicFeatures = mutableSetOf(":manage_feeds")
}


dependencies {

    implementation("androidx.core:core-ktx:1.0.2")
    implementation("androidx.fragment:fragment-ktx:1.1.0-beta01")
    implementation("androidx.activity:activity-ktx:1.0.0-beta01")

    // androidx ui
    implementation("androidx.drawerlayout:drawerlayout:1.1.0-alpha02")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.recyclerview:recyclerview:1.0.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.0.0")
    implementation("androidx.preference:preference-ktx:1.0.0")

    // androidx others
    implementation("androidx.browser:browser:1.0.0")

    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.4.0")

    // internal libraries
    val GEEKDROID_PROJECT_DIR: String? by project
    val geekdroidExt = GEEKDROID_PROJECT_DIR?.let { "" } ?: "aar"
    implementation(group = "com.geekorum", name = "geekdroid", version = "0.0.1", ext = geekdroidExt)
    create(group = "com.geekorum", name = "geekdroid-firebase", version = "0.0.1", ext = geekdroidExt).also {
        add("googleImplementation", it)
    }
    implementation(project(":htmlparsers"))
    implementation(project(":webapi"))

    implementation("com.google.android.material:material:1.1.0-alpha07")
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")
    implementation("com.squareup.okhttp3:logging-interceptor:3.12.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:3.12.0")
    implementation("com.squareup.picasso:picasso:2.5.2")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")

    implementation("org.jsoup:jsoup:1.10.2")

    val lifecycleVersion: String by rootProject.extra
    implementation("androidx.lifecycle:lifecycle-extensions:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-core-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    kapt("androidx.lifecycle:lifecycle-compiler:$lifecycleVersion")
    dualTestImplementation("androidx.arch.core:core-testing:2.0.1")

    // dagger
    val daggerVersion: String by rootProject.extra
    implementation(enforcedDaggerPlatform(daggerVersion))
    kapt(enforcedDaggerPlatform(daggerVersion))
    implementation("com.google.dagger:dagger:$daggerVersion")
    implementation("com.google.dagger:dagger-android:$daggerVersion")
    implementation("com.google.dagger:dagger-android-support:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    kapt("com.google.dagger:dagger-android-processor:$daggerVersion")
    kaptTest("com.google.dagger:dagger-compiler:$daggerVersion")
    kaptTest("com.google.dagger:dagger-android-processor:$daggerVersion")
    implementation("com.squareup.inject:assisted-inject-annotations-dagger2:0.4.0")
    kapt("com.squareup.inject:assisted-inject-processor-dagger2:0.4.0")

    val roomVersion = "2.1.0"
    kapt("androidx.room:room-compiler:$roomVersion")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")

    val workVersion = "2.1.0-rc01"
    androidTestImplementation("androidx.work:work-testing:$workVersion")

    val kotlinVersion: String by rootProject.extra
    implementation(enforcedPlatform(kotlin("bom", kotlinVersion)))
    implementation(kotlin("stdlib-jdk8"))

    val coroutinesVersion: String by rootProject.extra
    implementation(enforcedCoroutinesPlatform(coroutinesVersion))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

    implementation(enforcedPlatform("com.google.firebase:firebase-bom:20.1.0"))
    add("googleImplementation", "com.crashlytics.sdk.android:crashlytics")
    // ensure that the free flavor don't get any firebase dependencies
    configurations["freeImplementation"].exclude(group = "com.google.firebase")

    add("googleImplementation", "com.google.android.play:core:1.5.0")

    // api dependencies for features modules
    api("androidx.appcompat:appcompat:1.1.0-beta01")
    api("androidx.work:work-runtime-ktx:$workVersion")
    api("androidx.room:room-runtime:$roomVersion")
    api("androidx.room:room-ktx:$roomVersion")
    api("androidx.paging:paging-runtime:2.1.0")
    api("com.squareup.retrofit2:retrofit:2.5.0")
    api("com.squareup.okhttp3:okhttp:3.12.0")
    api("com.jakewharton.timber:timber:4.7.1")

    // fragment testing declare some activities and resources that needs to be in the apk
    // we don't use it. here but it is used in feature modules
    debugImplementation("androidx.fragment:fragment-testing:1.2.0-alpha01")
    // fragment testing doesn't request 1.2.0 yet, so force it
    debugImplementation("androidx.test:core-ktx:1.2.0")

}

apply {
    val playServicesActivated = file("google-services.json").exists()
    if (playServicesActivated) {
        // needs to be applied after configuration
        plugin("com.google.gms.google-services")
        plugin("io.fabric")
    }
}
