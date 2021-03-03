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
package com.geekorum.ttrss.manage_feeds

import android.app.Activity
import android.app.Application
import androidx.hilt.lifecycle.ViewModelFactoryModules
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import com.geekorum.ttrss.ForceNightModeViewModel_HiltModule
import com.geekorum.ttrss.articles_list.TtrssAccountViewModel_HiltModule
import com.geekorum.ttrss.core.CoreFactoriesModule
import com.geekorum.ttrss.di.FeatureScope
import com.geekorum.ttrss.features_api.ManageFeedsDependencies
import com.geekorum.ttrss.manage_feeds.add_feed.AddFeedActivity
import com.geekorum.ttrss.manage_feeds.add_feed.AddFeedModule
import com.geekorum.ttrss.manage_feeds.workers.WorkerComponent
import com.geekorum.ttrss.manage_feeds.workers.WorkersModule
import com.geekorum.ttrss.session.SessionAccountModule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.hilt.android.internal.lifecycle.DefaultActivityViewModelFactory
import dagger.hilt.migration.DisableInstallInCheck

@Component(dependencies = [ManageFeedsDependencies::class],
    modules = [CoreFactoriesModule::class,
        WorkManagerModule::class,
        ManageFeedActivityModule::class,
        AddFeedModule::class,
        WorkersModule::class])
@FeatureScope
interface ManageFeedComponent {

    fun createActivityComponent(): ActivityComponent.Factory

    fun getWorkerFactory(): WorkerFactory

    fun createWorkerComponent(): WorkerComponent.Builder

}

@Module(subcomponents = [ActivityComponent::class])
@DisableInstallInCheck
interface ManageFeedActivityModule

@Subcomponent(modules = [SessionAccountModule::class,
    ForceNightModeViewModel_HiltModule::class,
    TtrssAccountViewModel_HiltModule::class,
    ViewModelFactoryModules.ActivityModule::class,
    ManageFeedModule::class
])
interface ActivityComponent {

    @Subcomponent.Factory
    interface Factory {
        fun newComponent(@BindsInstance activity: Activity): ActivityComponent
    }

    // can't be injected because DefaultActivityViewModelFactory can't be applied on field
    @DefaultActivityViewModelFactory
    fun getActivityViewModelFactory(): Set<ViewModelProvider.Factory?>

    // inject required to provide daggerDelegateFragmentFactory for fragment constructor injection.
    fun inject(baseSessionActivity: BaseSessionActivity)
    fun inject(addFeedActivity: AddFeedActivity)
}

@Module(includes = [ManageFeedViewModel_HiltModule::class])
@DisableInstallInCheck
private class ManageFeedModule

@Module
@DisableInstallInCheck
object WorkManagerModule {
    @Provides
    fun provideWorkManager(application: Application): WorkManager = WorkManager.getInstance(application)
}

