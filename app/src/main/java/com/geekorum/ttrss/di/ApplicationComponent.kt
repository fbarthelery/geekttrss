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

import androidx.hilt.work.WorkerFactoryModule
import com.geekorum.geekdroid.dagger.AppInitializersModule
import com.geekorum.ttrss.AllFeaturesInstalledModule
import com.geekorum.ttrss.Application
import com.geekorum.ttrss.article_details.ResourcesWebFontProviderModule
import com.geekorum.ttrss.core.ActualCoroutineDispatchersModule
import com.geekorum.ttrss.data.ArticlesDatabaseModule
import com.geekorum.ttrss.features_api.ManageFeedsDependencies
import com.geekorum.ttrss.in_app_update.NoInAppUpdateModule
import com.geekorum.ttrss.sync.workers.FaviKonModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import javax.inject.Singleton

/**
 * Main component for the application
 */
//TODO: remove once tests are migrated to hilt
@Component(modules = [FlavorLessModule::class, ActualCoroutineDispatchersModule::class, ArticlesDatabaseModule::class, AssistedFactoriesModule::class, WorkerFactoryModule::class, FaviKonModule::class, ResourcesWebFontProviderModule::class, AllFeaturesInstalledModule::class, NoInAppUpdateModule::class])
@Singleton
interface ApplicationComponent : AndroidInjector<Application?>, ManageFeedsDependencies {
    @Component.Builder
    interface Builder {
        fun build(): ApplicationComponent

        @BindsInstance
        fun bindApplication(application: android.app.Application): Builder
    }

    override fun inject(application: Application?)
}

@EntryPoint
@InstallIn(dagger.hilt.android.components.ApplicationComponent::class)
interface ApplicationComponentEntryPoint : AndroidInjector<Application>, ManageFeedsDependencies
