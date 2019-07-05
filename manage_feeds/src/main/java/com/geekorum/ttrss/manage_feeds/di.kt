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
package com.geekorum.ttrss.manage_feeds

import android.accounts.Account
import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import com.geekorum.geekdroid.dagger.FragmentFactoriesModule
import com.geekorum.geekdroid.dagger.FragmentKey
import com.geekorum.geekdroid.dagger.ViewModelKey
import com.geekorum.ttrss.CoreFactoriesModule
import com.geekorum.ttrss.accounts.PerAccount
import com.geekorum.ttrss.data.ArticlesDatabase
import com.geekorum.ttrss.di.FeatureScope
import com.geekorum.ttrss.di.ViewModelsModule
import com.geekorum.ttrss.features_api.ManageFeedsDependencies
import com.geekorum.ttrss.manage_feeds.add_feed.AddFeedModule
import com.geekorum.ttrss.manage_feeds.workers.WorkerComponent
import com.geekorum.ttrss.manage_feeds.workers.WorkersModule
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.multibindings.IntoMap

@Component(dependencies = [ManageFeedsDependencies::class],
    modules = [AndroidInjectorsModule::class,
        CoreFactoriesModule::class,
        WorkManagerModule::class,
        AddFeedModule::class,
        WorkersModule::class])
@FeatureScope
interface ManageFeedComponent {

    fun getWorkerFactory(): WorkerFactory

    fun createWorkerComponent() : WorkerComponent.Builder

    val activityInjector: DispatchingAndroidInjector<Activity>
}

@Module(includes = [AndroidSupportInjectionModule::class])
abstract class AndroidInjectorsModule {

    @ContributesAndroidInjector(modules = [FragmentFactoriesModule::class,
        ViewModelsModule::class,
        ManageFeedModule::class])
    @PerAccount
    internal abstract fun contributesManageFeedActivityInjector(): ManageFeedsActivity

}

@Module
private abstract class ManageFeedModule {
    @Binds
    @IntoMap
    @ViewModelKey(ManageFeedViewModel::class)
    abstract fun bindManageFeedViewModel(vm: ManageFeedViewModel): ViewModel

    @Binds
    @IntoMap
    @FragmentKey(ConfirmUnsubscribeFragment::class)
    abstract fun bindConfirmUnsubscribeFragment(fragment: ConfirmUnsubscribeFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(ManageFeedsFragment::class)
    abstract fun bindManageFeedsFragment(fragment: ManageFeedsFragment): Fragment

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun providesFeedsDao(articlesDatabase: ArticlesDatabase) = articlesDatabase.manageFeedsDao()

        @JvmStatic
        @Provides
        fun providesAccount(manageFeedsActivity: ManageFeedsActivity): Account = manageFeedsActivity.account!!

    }
}

@Module
class WorkManagerModule {
    @Provides
    fun provideWorkManager(application: Application): WorkManager = WorkManager.getInstance(application)
}

