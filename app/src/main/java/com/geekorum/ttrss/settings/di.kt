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
package com.geekorum.ttrss.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.geekorum.geekdroid.dagger.FragmentKey
import com.geekorum.ttrss.settings.manage_features.InstallFeatureActivity
import com.geekorum.ttrss.settings.manage_features.ManageFeaturesModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(includes = [SettingsInitializerModule::class, ManageFeaturesModule::class])
abstract class SettingsModule {

    @ContributesAndroidInjector
    abstract fun contributesSettingsActivityInjector(): SettingsActivity

    @ContributesAndroidInjector
    abstract fun contributesInstallFeatureActivityInjector(): InstallFeatureActivity

    @Binds
    @IntoMap
    @FragmentKey(SettingsActivity.SettingsFragment::class)
    abstract fun bindSettingsFragment(settingsFragment: SettingsActivity.SettingsFragment): Fragment

    @Module
    companion object {

        @Provides
        fun providesApplicationPreferences(application: Application): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(application)
    }

}
