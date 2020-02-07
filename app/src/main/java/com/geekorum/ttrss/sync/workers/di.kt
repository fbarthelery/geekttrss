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
package com.geekorum.ttrss.sync.workers

import android.accounts.Account
import androidx.work.WorkerFactory
import com.geekorum.favikonsnoop.FaviKonSnoop
import com.geekorum.favikonsnoop.snoopers.AppManifestSnooper
import com.geekorum.favikonsnoop.snoopers.AppleTouchIconSnooper
import com.geekorum.favikonsnoop.snoopers.WhatWgSnooper
import com.geekorum.geekdroid.dagger.WorkerInjectionModule
import com.geekorum.geekdroid.dagger.WorkerKey
import com.geekorum.ttrss.accounts.NetworkLoginModule
import com.geekorum.ttrss.accounts.PerAccount
import com.geekorum.ttrss.accounts.ServerInformation
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.htmlparsers.ImageUrlExtractor
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.network.TinyrssApiModule
import com.geekorum.ttrss.sync.BackgroundDataUsageManager
import com.geekorum.ttrss.sync.DatabaseAccessModule
import com.geekorum.ttrss.sync.DatabaseService
import com.geekorum.ttrss.sync.FeedIconSynchronizer
import com.geekorum.ttrss.sync.HttpCacher
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient


@Module(includes = [WorkerInjectionModule::class], subcomponents = [SyncWorkerComponent::class])
abstract class WorkersModule {

    @Binds
    @IntoMap
    @WorkerKey(UpdateAccountInfoWorker::class)
    abstract fun providesUpdateAccountInfoWorkerFactory(
            workerFactory: UpdateAccountInfoWorker.WorkerFactory): WorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SendTransactionsWorker::class)
    abstract fun providesSendTransactionsWorkerFactory(
            workerFactory: SendTransactionsWorker.WorkerFactory): WorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncFeedsWorker::class)
    abstract fun providesSyncFeedsWorkerFactory(
            workerFactory: SyncFeedsWorker.WorkerFactory): WorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncFeedsIconWorker::class)
    abstract fun providesSyncFeedsIconWorkerFactory(
            workerFactory: SyncFeedsIconWorker.WorkerFactory): WorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(CollectNewArticlesWorker::class)
    abstract fun providesCollectNewArticlesWorkerFactory(
            workerFactory: CollectNewArticlesWorker.WorkerFactory): WorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(UpdateArticleStatusWorker::class)
    abstract fun providesUpdateArticleStatusWorkerFactory(
            workerFactory: UpdateArticleStatusWorker.WorkerFactory): WorkerFactory

}

@Subcomponent(modules = [
    NetworkLoginModule::class,
    TinyrssApiModule::class,
    DatabaseAccessModule::class,
    FaviKonModule::class
])
@PerAccount
interface SyncWorkerComponent {

    val account: Account
    val apiService: ApiService
    val serverInformation: ServerInformation
    val databaseService: DatabaseService
    val dispatchers: CoroutineDispatchersProvider
    val feedIconSynchronizer: FeedIconSynchronizer
    val backgroundDataUsageManager: BackgroundDataUsageManager
    val imageUrlExtractor: ImageUrlExtractor
    val httpCacher: HttpCacher

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun seedAccount(account: Account): Builder

        fun build(): SyncWorkerComponent
    }
}

@Module
internal object FaviKonModule {

    @Provides
    fun providesFaviKonSnoop(okHttpClient: OkHttpClient): FaviKonSnoop {
        val snoopers = listOf(
                AppManifestSnooper(),
                WhatWgSnooper(),
                AppleTouchIconSnooper())
        return FaviKonSnoop(snoopers, okHttpClient)
    }
}
