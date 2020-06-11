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
import dagger.hilt.DefineComponent
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent

/*
 * Dependency injection pieces for the Sync functionality.
 *
 * AccountSyncServiceComponent is a SubComponent of the [ServiceComponent]:
 *   It has binding for a specific account in the SyncService
 *
 */

@Module(includes = [WorkersModule::class])
object SyncWorkersModule


@DefineComponent(parent = ServiceComponent::class)
@PerAccount
interface AccountSyncServiceComponent

@DefineComponent.Builder
interface AccountSyncServiceComponentBuilder {
    @BindsInstance
    fun seedAccount(account: Account): AccountSyncServiceComponentBuilder

    fun build(): AccountSyncServiceComponent
}

@Module(includes = [
    AssistedFactoriesModule::class,
    NetworkLoginModule::class, TinyrssApiModule::class
])
@InstallIn(AccountSyncServiceComponent::class)
internal interface AccountSyncServiceModule

@Module
@InstallIn(AccountSyncServiceComponent::class)
internal object AccountPreferenceModule {

    @Provides
    fun providesAccountPreferences(context: Context, account: Account): SharedPreferences {
        return context.getSharedPreferences(account.name, Context.MODE_PRIVATE)
    }
}

@Module
@InstallIn(AccountSyncServiceComponent::class)
internal abstract class DatabaseAccessModule {
    @Binds
    abstract fun providesDatabaseService(synchronizationFacade: SynchronizationFacade): DatabaseService
}
