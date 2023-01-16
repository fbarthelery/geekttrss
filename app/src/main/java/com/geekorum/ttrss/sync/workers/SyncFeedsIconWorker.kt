/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2023 by Frederic-Charles Barthelery.
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
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.sync.FeedIconSynchronizer
import com.geekorum.ttrss.webapi.ApiCallException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


/**
 * Synchronize icon url info for every feeds.
 */
@HiltWorker
class SyncFeedsIconWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    syncWorkerComponentBuilder: SyncWorkerComponent.Builder,
    private val dispatchers: CoroutineDispatchersProvider
) : BaseSyncWorker(context, workerParams, syncWorkerComponentBuilder) {

    private val feedIconSynchronizer: FeedIconSynchronizer = syncWorkerComponent.feedIconSynchronizer

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

}
