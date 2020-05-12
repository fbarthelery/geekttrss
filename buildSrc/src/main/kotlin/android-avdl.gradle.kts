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
package com.geekorum.build

/**
 * This defines the configuration to run tests with gradle-avdl.
 * The CI will run the test on on the following devices.
 * To run this your project must have declared the following gradle properties
 * via command line or your gradle.properties
 * RUN_TESTS_ON_AVDL=true
 */

private val runTestOnAvdl = findProperty("RUN_TESTS_ON_AVDL") as String?
val runTests =  runTestOnAvdl?.toBoolean() ?: false

if (runTests) {
    val flydroidUrl = checkNotNull(findProperty("FLYDROID_URL") as String?) { "FLYDROID_URL property not specified" }
    val flydroidKey = checkNotNull(findProperty("FLYDROID_KEY") as String?) { "FLYDROID_KEY property not specified" }

    configureAvdlDevices(flydroidUrl, flydroidKey)
}
