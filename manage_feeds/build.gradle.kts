/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2020 by Frederic-Charles Barthelery.
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
import com.geekorum.build.dualTestImplementation
import com.geekorum.build.enforcedDaggerPlatform

plugins {
    id("com.android.dynamic-feature")
    kotlin("android")
    kotlin("kapt")
    id("com.geekorum.build.android-tests")
    id("com.geekorum.build.android-avdl")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    val compileSdkInt: Int by rootProject.extra
    compileSdk = compileSdkInt

    defaultConfig {
        minSdk = 24
    }

    dataBinding {
        isEnabled = true
    }

    flavorDimensions += "distribution"
    productFlavors {
        register("free") {
            dimension = "distribution"
        }

        register("google") {
            dimension = "distribution"
        }
    }

    buildTypes {
        named("release") {
            proguardFile("proguard-rules.pro")
        }
    }
}


dependencies {
    // it seems that app is not added in classpath when running tests from gradle
    implementation(project(":app"))

    implementation(enforcedPlatform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))

    val daggerVersion = "2.36"
    implementation(enforcedDaggerPlatform(daggerVersion))
    kapt(enforcedDaggerPlatform(daggerVersion))
    implementation("com.google.dagger:dagger:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    kaptTest("com.google.dagger:dagger-compiler:$daggerVersion")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    implementation("androidx.hilt:hilt-work:1.0.0")
    testImplementation("com.google.dagger:hilt-android-testing:$daggerVersion")
    androidTestImplementation("com.google.dagger:hilt-android-testing:$daggerVersion")
    kapt("com.google.dagger:hilt-compiler:$daggerVersion")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    kaptTest("com.google.dagger:hilt-compiler:$daggerVersion")
    kaptAndroidTest("com.google.dagger:hilt-compiler:$daggerVersion")

    //geekdroid
    implementation("com.geekorum.geekdroid:geekdroid:geekttrss-1.5.2")

    implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.5.0"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

    implementation("androidx.activity:activity-ktx:1.2.3")

    // androidx UI
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.recyclerview:recyclerview:1.2.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.1.0")
    implementation("com.google.android.material:material:1.3.0")

    implementation("androidx.core:core-ktx:1.5.0")

    val lifecycleVersion = "2.3.1"
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-core-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    implementation(project(":htmlparsers"))
    implementation(project(":webapi"))

    implementation("io.coil-kt:coil:1.2.0")

    androidTestImplementation("androidx.work:work-testing:2.6.0-alpha02")
    dualTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.6.0")

    debugImplementation("androidx.fragment:fragment-testing:1.3.4")

    // the test apk need these dependencies because they provide some of its resources
    // likely due to manifest merging with :app manifest
    androidTestImplementation("androidx.fragment:fragment-testing:1.3.0")
    androidTestImplementation("com.squareup.leakcanary:leakcanary-android:2.7")
    androidTestImplementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
    androidTestImplementation("com.google.android.play:core-ktx:1.8.1")

    // used in test through geekdroid
    androidTestImplementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.3.1")

}
