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
import org.gradle.api.JavaVersion
import org.gradle.api.Project

/**
 * Configure java version compile options based on minSdkVersion value
 */
fun BaseExtension.configureJavaVersion() {
    val api = defaultConfig.minSdkVersion.apiLevel
    val version = when {
        api >= 24 -> JavaVersion.VERSION_1_8
        api >= 19 -> JavaVersion.VERSION_1_7
        else -> JavaVersion.VERSION_1_6
    }
    compileOptions {
        setSourceCompatibility(version)
        setTargetCompatibility(version)
    }
}
