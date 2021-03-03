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
package com.geekorum.ttrss.manage_feeds.workers

import android.accounts.Account
import androidx.hilt.work.HiltWorkerFactory
import androidx.hilt.work.HiltWrapper_WorkerFactoryModule
import androidx.work.WorkerFactory
import com.geekorum.ttrss.accounts.NetworkLoginModule
import com.geekorum.ttrss.accounts.PerAccount
import com.geekorum.ttrss.network.TinyrssApiModule
import com.geekorum.ttrss.webapi.TinyRssApi
import com.geekorum.ttrss.webapi.TokenRetriever
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.hilt.migration.DisableInstallInCheck


@Module(subcomponents = [WorkerComponent::class], includes = [
    HiltWrapper_WorkerFactoryModule::class,
    SubscribeWorker_HiltModule::class,
    UnsubscribeWorker_HiltModule::class
])
@DisableInstallInCheck
abstract class WorkersModule {

    @Binds
    abstract fun hiltWorkerFactory(hiltWorkerFactory: HiltWorkerFactory): WorkerFactory

}


@PerAccount
@Subcomponent(modules = [ApiServiceModule::class] )
interface WorkerComponent {

    fun getManageFeedService(): ManageFeedService

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun setAccount(account: Account): Builder

        fun build(): WorkerComponent
    }
}

@Module(includes = [
    TinyrssApiModule::class,
    NetworkLoginModule::class
])
@DisableInstallInCheck
internal class ApiServiceModule {

    @Provides
    @PerAccount
    fun providesManageFeedService(tokenRetriever: TokenRetriever, tinyrssApi: TinyRssApi) : ManageFeedService {
        return RetrofitManageFeedService(tokenRetriever, tinyrssApi)
    }
}


