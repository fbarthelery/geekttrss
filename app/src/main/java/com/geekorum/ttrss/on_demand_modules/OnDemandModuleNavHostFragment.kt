/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2021 by Frederic-Charles Barthelery.
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

import androidx.fragment.app.Fragment
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.NavGraphNavigator
import androidx.navigation.Navigator
import androidx.navigation.dynamicfeatures.fragment.DynamicNavHostFragment
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.plusAssign
import com.geekorum.geekdroid.dagger.FragmentFactoriesModule
import com.geekorum.geekdroid.dagger.FragmentKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import timber.log.Timber
import javax.inject.Inject

/**
 * A [DynamicNavHostFragment] that reverts to standard [Navigator] when [OnDemandModuleManager]
 * can't install modules
 */
class OnDemandModuleNavHostFragment @Inject constructor(
    private val onDemandModuleManager: OnDemandModuleManager
): DynamicNavHostFragment() {

    override fun onCreateNavController(navController: NavController) {
        if (!onDemandModuleManager.canInstallModule) {
            Timber.i("The application can't install dynamic feature modules. Fallback to standard navigators")
            // restore default navigator (undo DynamicNavHostFragment
            val navigatorProvider = navController.navigatorProvider
            navigatorProvider += ActivityNavigator(requireActivity())
            check(id > 0)
            navigatorProvider += FragmentNavigator(requireContext(), childFragmentManager, id)
            navigatorProvider += NavGraphNavigator(navigatorProvider)
            navigatorProvider += DialogFragmentNavigator(requireContext(), childFragmentManager)
        } else {
            super.onCreateNavController(navController)
        }
    }

}

@Module(includes = [FragmentFactoriesModule::class])
@InstallIn(SingletonComponent::class)
abstract class OnDemandModules {
    @Binds
    @IntoMap
    @FragmentKey(OnDemandModuleNavHostFragment::class)
    abstract fun bindOnDemandModuleNavHostFragment(fragment: OnDemandModuleNavHostFragment): Fragment
}
