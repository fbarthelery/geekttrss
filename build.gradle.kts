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
import com.android.build.gradle.BaseExtension
import com.geekorum.build.SourceLicenseCheckerPlugin
import com.geekorum.build.configureAnnotationProcessorDeps
import com.geekorum.build.configureJavaVersion
import com.geekorum.build.createComponentsPlatforms
import com.geekorum.build.setupGoogleContent
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // these should not be needed but for an unknown reason they get applied
    // with bad ordering if not there. or they can't be applied dynamically
    // version used is in gradle.properties
    kotlin("jvm") apply false
    id("androidx.navigation.safeargs.kotlin")  apply false
    id("com.google.firebase.crashlytics") apply false
    id("com.google.gms.google-services") apply false
    id("com.google.android.gms.oss-licenses-plugin") apply false
    id("dagger.hilt.android.plugin") apply false
}

// Need to be there because if not, the various plugins downgrade the AGP version used from buildSrc
// during the compilation of the *.gradle.kts script
buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.2")
    }
}

// some extra properties
extra["compileSdkInt"] = 33

allprojects {
    repositories {
        google().setupGoogleContent()
        mavenCentral()
        // for geekdroid
        flatDir {
            dirs(rootProject.files("libs"))
        }
        maven {
            url = uri("https://jitpack.io")
        }
    }
    dependencies {
        createComponentsPlatforms()
    }
    configureAnnotationProcessorDeps()

    apply<SourceLicenseCheckerPlugin>()

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf(
                "-Xjvm-default=all", "-opt-in=kotlin.RequiresOptIn")
        }
    }

    afterEvaluate {
        extensions.findByType<KaptExtension>()?.arguments {
            arg("dagger.formatGeneratedSource", "enabled")
        }
        extensions.findByType<BaseExtension>()?.apply {
            configureJavaVersion()
        }
    }
}

tasks.register("clean", Delete::class.java) {
    delete(buildDir)
}
