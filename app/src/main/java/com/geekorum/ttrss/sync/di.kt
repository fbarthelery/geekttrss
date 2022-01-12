/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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
import com.geekorum.ttrss.accounts.NetworkLoginModule
import com.geekorum.ttrss.accounts.PerAccount
import com.geekorum.ttrss.data.plugins.SynchronizationFacade
import com.geekorum.ttrss.network.TinyrssApiModule
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.components.SingletonComponent

/**
 * Dependency injection pieces for the Sync functionality.
 *
 * The SyncComponent is a SubComponent of the ServiceComponent. It provides an ArticleSynchronizerFactory
 */


@Module(subcomponents = [SyncComponent::class])
@InstallIn(ServiceComponent::class)
private object SyncComponentModule


@Subcomponent(modules = [
    NetworkLoginModule::class, TinyrssApiModule::class
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
@InstallIn(SingletonComponent::class)
internal abstract class DatabaseAccessModule {
    @Binds
    abstract fun providesDatabaseService(synchronizationFacade: SynchronizationFacade): DatabaseService
}
