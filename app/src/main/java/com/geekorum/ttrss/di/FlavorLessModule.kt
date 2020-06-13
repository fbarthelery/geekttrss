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
import com.geekorum.ttrss.core.CoreFactoriesModule
import dagger.Module
import dagger.android.support.AndroidSupportInjectionModule

/**
 * Base module who includes all the modules common to all flavor of the applicatio
 */
@Module(includes = [
    CoreFactoriesModule::class,
    AndroidFrameworkModule::class,
    AndroidSupportInjectionModule::class,
//    AndroidBindingsModule::class,
    com.geekorum.geekdroid.dagger.ViewModelsModule::class,
//    ViewModelsModule::class,
    com.geekorum.ttrss.article_details.ActivitiesInjectorModule::class,
//    com.geekorum.ttrss.articles_list.ActivitiesInjectorModule::class,
    com.geekorum.ttrss.accounts.ServicesInjectorModule::class,
    com.geekorum.ttrss.add_feed.AndroidInjectorsModule::class,
    com.geekorum.ttrss.providers.AndroidInjectorsModule::class
])
object FlavorLessModule
