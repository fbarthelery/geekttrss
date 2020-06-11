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
package com.geekorum.ttrss.sync

import android.accounts.Account
import android.content.Context
import android.content.SharedPreferences
import com.geekorum.favikonsnoop.FaviKonSnoop
import com.geekorum.favikonsnoop.snoopers.AppManifestSnooper
import com.geekorum.favikonsnoop.snoopers.AppleTouchIconSnooper
import com.geekorum.favikonsnoop.snoopers.WhatWgSnooper
import com.geekorum.ttrss.accounts.NetworkLoginModule
import com.geekorum.ttrss.accounts.PerAccount
import com.geekorum.ttrss.data.plugins.SynchronizationFacade
import com.geekorum.ttrss.di.AssistedFactoriesModule
import com.geekorum.ttrss.network.TinyrssApiModule
import com.geekorum.ttrss.sync.workers.WorkersModule
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.android.ContributesAndroidInjector
import okhttp3.OkHttpClient

/**
 * Dependency injection pieces for the Sync functionality.
 *
 * ArticleSyncService has a SubComponent of the ApplicationComponent, that allows it to inject to ArticleSyncService.
 * ArticleSyncAdapter has a SubSubComponent which provides the actual SyncComponent
 *
 */

@Module
abstract class ServiceInjectorModule {

    @ContributesAndroidInjector(modules = [SyncComponentModule::class, SyncServiceModule::class])
    abstract fun contributeArticleSyncServiceInjector(): ArticleSyncService

}

@Module(includes = [WorkersModule::class])
object SyncWorkersModule

@Module(subcomponents = [SyncComponent::class])
private object SyncComponentModule


@Subcomponent(modules = [
    AssistedFactoriesModule::class,
    NetworkLoginModule::class, TinyrssApiModule::class,
    AccountPreferenceModule::class, DatabaseAccessModule::class
])
@PerAccount
internal interface SyncComponent {

    val articleSynchronizerFactory: ArticleSynchronizer.Factory

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun seedAccount(account: Account): Builder

        fun build(): SyncComponent
    }
}

@Module
internal object SyncServiceModule {

    @Provides
    fun providesContext(service: ArticleSyncService): Context {
        return service
    }

    @Provides
    fun providesFaviKonSnoop(okHttpClient: OkHttpClient): FaviKonSnoop {
        val snoopers = listOf(
            AppManifestSnooper(),
            WhatWgSnooper(),
            AppleTouchIconSnooper())
        return FaviKonSnoop(snoopers, okHttpClient)
    }
}

@Module
internal object AccountPreferenceModule {

    @Provides
    fun providesAccountPreferences(context: Context, account: Account): SharedPreferences {
        return context.getSharedPreferences(account.name, Context.MODE_PRIVATE)
    }
}

@Module
internal abstract class DatabaseAccessModule {
    @Binds
    abstract fun providesDatabaseService(synchronizationFacade: SynchronizationFacade): DatabaseService
}
