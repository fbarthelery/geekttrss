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

import com.genymotion.GenymotionGradlePlugin
import com.genymotion.GenymotionPluginExtension
import com.genymotion.model.CloudVDLaunchDsl
import com.genymotion.model.GenymotionConfig
import com.genymotion.model.VDLaunchDsl
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.configure

internal fun Project.configureGenymotionDevices(useLocalDevices: Boolean = false, useCloudDevices: Boolean = false): Unit {
    if (!(useLocalDevices || useCloudDevices)) return

    apply<GenymotionGradlePlugin>()

    val genymotionLogcatDir = "${buildDir.absolutePath}/reports/genymotion/"
    configure<GenymotionPluginExtension> {
        if (useLocalDevices) {
            devices(closureOf<NamedDomainObjectContainer<VDLaunchDsl>> {
                register("$name-pixelc-v24") {
                    template = "Google Pixel C - 7.0.0 - API 24 - 2560x1800"
                    logcat = "$genymotionLogcatDir/$name.logcat"
                }
            })
        }

        if (useCloudDevices) {
            cloudDevices(closureOf<NamedDomainObjectContainer<CloudVDLaunchDsl>> {
                register("$name-pixelc-v24") {
                    template = "Google Pixel C - 7.0.0 - API 24 - 2560x1800"
                    logcat = "$genymotionLogcatDir/$name.logcat"
                    isClearLogAfterBoot = false
                }
                register("$name-pixel3-v28") {
                    template = "Google Pixel 3 - 9.0 - API 28 - 1080x2160"
                    logcat = "$genymotionLogcatDir/$name.logcat"
                    isClearLogAfterBoot = false
                }
            })
        }
    }
}

private fun GenymotionPluginExtension.config(action: GenymotionConfig.() -> Unit) {
    val extensionAware = this as ExtensionAware
    extensionAware.configure<GenymotionConfig>(action)
}
