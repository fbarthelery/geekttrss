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
import com.geekorum.build.dualTestImplementation

plugins {
//    alias(libs.plugins.android.dynamic.feature)
    id("com.android.dynamic-feature")
//    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.kotlin.kapt)
    kotlin("android")
    kotlin("kapt")
    id("com.geekorum.build.android-tests")
    id("com.geekorum.build.android-avdl")
    alias(libs.plugins.androidx.navigation.safeargs.kotlin)
}

android {
    val compileSdkInt: Int by rootProject.extra
    compileSdk = compileSdkInt
    namespace = "com.geekorum.ttrss.manage_feeds"

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        dataBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
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

kotlin {
//        this seems to break hilt at the moment
//        jvmToolchain(11)
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

dependencies {
    // it seems that app is not added in classpath when running tests from gradle
    implementation(project(":app"))

    implementation(enforcedPlatform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))

    implementation(libs.dagger)
    kapt(libs.dagger.compiler)
    kaptTest(libs.dagger.compiler)
    implementation(libs.androidx.hilt.work)
    testImplementation(libs.dagger.hilt.android.testing)
    androidTestImplementation(libs.dagger.hilt.android.testing)
    kapt(libs.dagger.hilt.compiler)
    kapt(libs.androidx.hilt.compiler)
    kaptTest(libs.dagger.hilt.compiler)
    kaptAndroidTest(libs.dagger.hilt.compiler)

    //geekdroid
    implementation(libs.geekdroid)

    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)

    implementation(libs.androidx.activity)

    // androidx UI
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.android.material)

    // compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.tooling.preview)

    // necessary for compose previews
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)

    implementation(libs.androidx.core)

    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.livedata.core)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime)

    implementation(project(":htmlparsers"))
    implementation(project(":webapi"))

    implementation(libs.coil)

    androidTestImplementation(libs.androidx.work.testing)
    dualTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(platform(libs.okhttp.bom))
    androidTestImplementation(libs.okhttp.mockwebserver)

    debugImplementation(libs.androidx.fragment.testing.manifest)
    androidTestImplementation(libs.androidx.fragment.testing)

    testImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

}
