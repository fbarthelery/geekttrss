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
package com.geekorum.ttrss.sync.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.sync.FeedIconSynchronizer
import com.geekorum.ttrss.webapi.ApiCallException
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


/**
 * Synchronize icon url info for every feeds.
 */
class SyncFeedsIconWorker(
        context: Context,
        workerParams: WorkerParameters,
        private val dispatchers: CoroutineDispatchersProvider,
        private val feedIconSynchronizer: FeedIconSynchronizer
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(dispatchers.io) {
        try {
            Timber.i("Synchronizing feeds icons")
            feedIconSynchronizer.synchronizeFeedIcons()
            Result.success()
        } catch (e: ApiCallException) {
            Timber.w(e, "unable to update feeds icons")
            Result.failure()
        }
    }

    class WorkerFactory @Inject constructor(
            syncWorkerComponentBuilder: SyncWorkerComponent.Builder
    ) : SyncWorkerFactory(syncWorkerComponentBuilder) {

        override fun createWorker(
                appContext: Context, workerClassName: String, workerParameters: WorkerParameters
        ): ListenableWorker? {
            if (workerClassName != SyncFeedsIconWorker::class.java.name) {
                return null
            }

            val syncWorkerComponent = createSyncWorkerComponent(workerParameters)
            return with(syncWorkerComponent) {
                SyncFeedsIconWorker(appContext, workerParameters,
                        dispatchers,
                        feedIconSynchronizer)
            }
        }
    }


}
