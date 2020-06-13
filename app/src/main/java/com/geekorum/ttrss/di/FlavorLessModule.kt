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

import com.geekorum.geekdroid.dagger.AndroidFrameworkModule
import com.geekorum.ttrss.DefaultNightModeModule
import com.geekorum.ttrss.accounts.AndroidTinyrssAccountManagerModule
import com.geekorum.ttrss.background_job.BackgroundJobsModule
import com.geekorum.ttrss.core.CoreFactoriesModule
import com.geekorum.ttrss.data.ArticlesDatabaseModule
import com.geekorum.ttrss.debugtools.StrictModeModule
import com.geekorum.ttrss.logging.LogcatLoggingModule
import com.geekorum.ttrss.on_demand_modules.OnDemandModules
import com.geekorum.ttrss.settings.SettingsModule
import dagger.Module

/**
 * Base module who includes all the modules common to all flavor of the applicatio
 */
@Module(includes = [
    CoreFactoriesModule::class,
    AndroidFrameworkModule::class,
    AndroidBindingsModule::class,
    ViewModelsModule::class,
    DefaultNightModeModule::class,
    BackgroundJobsModule::class,
    NetworkModule::class,
    ArticlesDatabaseModule::class,
    SettingsModule::class,
    LogcatLoggingModule::class,
    AndroidTinyrssAccountManagerModule::class,
    StrictModeModule::class,
    OnDemandModules::class,
    com.geekorum.ttrss.article_details.ActivitiesInjectorModule::class,
    com.geekorum.ttrss.articles_list.ActivitiesInjectorModule::class,
    com.geekorum.ttrss.accounts.ServicesInjectorModule::class,
    com.geekorum.ttrss.add_feed.AndroidInjectorsModule::class,
    com.geekorum.ttrss.providers.AndroidInjectorsModule::class
])
object FlavorLessModule
