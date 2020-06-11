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
package com.geekorum.ttrss.di

import com.geekorum.ttrss.GmsSecurityProviderModule
import com.geekorum.ttrss.article_details.GoogleFontsWebFontProviderModule
import com.geekorum.ttrss.in_app_update.PlayStoreInAppUpdateModule
import com.geekorum.ttrss.logging.CrashlyticsLoggingModule
import com.geekorum.ttrss.on_demand_modules.PlayStoreInstallModule
import dagger.Component
import dagger.Module
import dagger.hilt.InstallIn
import javax.inject.Singleton

/**
 * [ApplicationComponent] for the Google flavor.
 * It has additional modules beside the [FlavorLessModule]
 */
@Component(modules = [
    FlavorLessModule::class,
    GoogleFontsWebFontProviderModule::class,
    CrashlyticsLoggingModule::class,
    PlayStoreInstallModule::class,
    PlayStoreInAppUpdateModule::class,
    GmsSecurityProviderModule::class
])
@Singleton
interface GoogleFlavorApplicationComponent : ApplicationComponent {

    @Component.Builder
    interface Builder : ApplicationComponent.Builder
}

@Module(includes = [
    FlavorLessModule::class,
    GoogleFontsWebFontProviderModule::class,
    CrashlyticsLoggingModule::class,
    PlayStoreInstallModule::class,
    PlayStoreInAppUpdateModule::class,
    GmsSecurityProviderModule::class
])
@InstallIn(dagger.hilt.android.components.ApplicationComponent::class)
interface GoogleFlavorApplicationModule
