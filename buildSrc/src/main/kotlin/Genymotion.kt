/**
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2018 by Frederic-Charles Barthelery.
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
import com.genymotion.tools.AndroidPluginTools
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.configure

internal fun Project.configureGenymotionDevices(useLocalDevices: Boolean = false, useCloudDevices: Boolean = false): Unit {
    if (!(useLocalDevices || useCloudDevices)) return

    apply<GenymotionGradlePlugin>()

    configure<GenymotionPluginExtension> {
        if (useLocalDevices) {
            devices(closureOf<NamedDomainObjectContainer<VDLaunchDsl>> {
                register("nexus9-v24") {
                    template = "Google Nexus 9 - 7.0.0 - API 24 - 1536x2048"
                }
            })
        }

        if (useCloudDevices) {
            cloudDevices(closureOf<NamedDomainObjectContainer<CloudVDLaunchDsl>> {
                register("nexus9-v24") {
                    template = "Google Nexus 9 - 7.0.0 - API 24 - 1536x2048"
                }
                register("pixel2-v26") {
                    template = "Google Pixel 2 - 8.0 - API 26 - 1080x1920"
                }
            })
        }

    }
}

private fun GenymotionPluginExtension.config(action: GenymotionConfig.() -> Unit) {
    val extensionAware = this as ExtensionAware
    extensionAware.configure<GenymotionConfig>(action)
}
