/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.on_demand_modules

import androidx.navigation.fragment.NavHostFragment
import timber.log.Timber
import javax.inject.Inject

/**
 * A standard [NavHostFragment] that acts as a DynamicNavHostFragment that can't install modules
 */
class OnDemandModuleNavHostFragment @Inject constructor(
    onDemandModuleManager: OnDemandModuleManager
): NavHostFragment(), OnDemandModuleNavHostProgressDestinationProvider {
    init {
        if (onDemandModuleManager.canInstallModule) {
            Timber.w("Use of a static OnDemandModuleNavHostFragment with OnDemandModuleManager that can install dynamic modules")
        }
    }
}