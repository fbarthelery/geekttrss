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
package com.geekorum.ttrss.di;

import com.geekorum.ttrss.MainActivity;
import com.geekorum.ttrss.settings.manage_modules.InstallFeatureActivity;
import com.geekorum.ttrss.install_feature.InstallFeatureModule;
import com.geekorum.ttrss.settings.SettingsActivity;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * Provides some binding modules to inject Android components.
 */
@Module(includes = {AndroidSupportInjectionModule.class})
public abstract class AndroidBindingsModule {

    @ContributesAndroidInjector
    abstract MainActivity contributesMainActivityInjector();

    @ContributesAndroidInjector
    abstract SettingsActivity contributesSettingsActivityInjector();

    @ContributesAndroidInjector(modules = {InstallFeatureModule.class})
    abstract InstallFeatureActivity contributesInstallFeatureActivityInjector();

}
