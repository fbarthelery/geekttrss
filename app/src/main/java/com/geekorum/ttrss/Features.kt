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
package com.geekorum.ttrss

import com.geekorum.ttrss.on_demand_modules.ImmutableModuleManager
import com.geekorum.ttrss.on_demand_modules.OnDemandModuleManager
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck


object Features {
    const val MANAGE_FEEDS = "manage_feeds"

    val allFeatures = setOf(MANAGE_FEEDS)
}

@Module
@DisableInstallInCheck
object AllFeaturesInstalledModule {
    @Provides
    fun providesOnDemandModuleManager(): OnDemandModuleManager {
        return ImmutableModuleManager(Features.allFeatures)
    }
}
