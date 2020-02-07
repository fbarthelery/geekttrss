/**
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
package com.geekorum.ttrss.settings.manage_features

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.geekorum.geekdroid.dagger.FragmentKey
import com.geekorum.geekdroid.dagger.ViewModelKey
import com.geekorum.ttrss.on_demand_modules.InstallModuleViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ManageFeaturesModule {

    @Binds
    @IntoMap
    @FragmentKey(ManageFeaturesFragment::class)
    abstract fun getManageFeaturesFragment(manageFeaturesFragment: ManageFeaturesFragment): Fragment

    @Binds
    @IntoMap
    @ViewModelKey(ManageFeaturesViewModel::class)
    abstract fun getManageFeaturesViewModel(manageFeaturesViewModel: ManageFeaturesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InstallModuleViewModel::class)
    abstract fun getInstallModuleViewModel(vm: InstallModuleViewModel): ViewModel
}
