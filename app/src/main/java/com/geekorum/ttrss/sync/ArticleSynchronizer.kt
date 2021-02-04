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
import android.app.Application
import android.content.OperationApplicationException
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import androidx.lifecycle.asFlow
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.workDataOf
import com.geekorum.geekdroid.accounts.CancellableSyncAdapter
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.sync.SyncContract.EXTRA_FEED_ID
import com.geekorum.ttrss.sync.SyncContract.EXTRA_NUMBER_OF_LATEST_ARTICLES_TO_REFRESH
import com.geekorum.ttrss.sync.SyncContract.EXTRA_UPDATE_FEED_ICONS
import com.geekorum.ttrss.sync.workers.CollectNewArticlesWorker
import com.geekorum.ttrss.sync.workers.SendTransactionsWorker
import com.geekorum.ttrss.sync.workers.SyncFeedsIconWorker
import com.geekorum.ttrss.sync.workers.SyncFeedsWorker
import com.geekorum.ttrss.sync.workers.SyncWorkerFactory
import com.geekorum.ttrss.sync.workers.UpdateAccountInfoWorker
import com.geekorum.ttrss.sync.workers.UpdateArticleStatusWorker
import com.geekorum.ttrss.webapi.ApiCallException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import timber.log.Timber
import java.util.UUID

/**
 * Synchronize Articles from the network.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ArticleSynchronizer @AssistedInject constructor(
    application: Application,
    @Assisted params: Bundle,
    private val account: Account,
    private val databaseService: DatabaseService
) : CancellableSyncAdapter.CancellableSync() {

    @AssistedFactory
    interface Factory {
        fun create(params: Bundle): ArticleSynchronizer
    }

    private val workManager = WorkManager.getInstance(application)

    private val numberOfLatestArticlesToRefresh = params.getInt(EXTRA_NUMBER_OF_LATEST_ARTICLES_TO_REFRESH, 500)
    private val updateFeedIcons = params.getBoolean(EXTRA_UPDATE_FEED_ICONS, false)
    private val feedId = params.getLong(EXTRA_FEED_ID, ApiService.ALL_ARTICLES_FEED_ID)

    private var syncInfoAndFeedWorkId: UUID? = null
    private var collectNewArticlesJobsTag: String? = null
    private var updateStatusJobsTag: String? = null

    override suspend fun sync() {
        try {
            syncInfoAndFeeds()
            collectNewArticles()
            updateArticlesStatus()
        } catch (e: ApiCallException) {
            Timber.w(e, "unable to synchronize articles")
        } catch (e: RemoteException) {
            Timber.e(e, "unable to synchronize articles")
        } catch (e: OperationApplicationException) {
            Timber.e(e, "unable to synchronize articles")
        } catch (e: RuntimeException) {
            Timber.log(if (e is CancellationException) Log.WARN else Log.ERROR,
                e,"unable to synchronize articles")
        }
    }

    private suspend fun syncInfoAndFeeds() {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val inputData = workDataOf(
                SyncWorkerFactory.PARAM_ACCOUNT_NAME to account.name,
                SyncWorkerFactory.PARAM_ACCOUNT_TYPE to account.type
        )

        val updateAccountInfo = OneTimeWorkRequestBuilder<UpdateAccountInfoWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        syncInfoAndFeedWorkId = updateAccountInfo.id

        val sendTransactions = OneTimeWorkRequestBuilder<SendTransactionsWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

        val syncFeeds = OneTimeWorkRequestBuilder<SyncFeedsWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

        val syncFeedsIcons = OneTimeWorkRequestBuilder<SyncFeedsIconWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

        var work = workManager.beginWith(listOf(updateAccountInfo, sendTransactions))
                .then(syncFeeds)

         work = if (updateFeedIcons || true)
             work.then(syncFeedsIcons)
         else work

        work.enqueue().await()

        work.workInfosLiveData.asFlow()
                .takeWhile { workInfos ->
                    workInfos.any { !it.state.isFinished }
                }
                .collect()
    }

    private suspend fun collectNewArticles() {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val tag = UUID.randomUUID().toString()
        val jobRequests = databaseService.getFeeds()
            .shuffled()
            .map { feed ->
            val inputData = CollectNewArticlesWorker.getInputData(account, feed.id)
            OneTimeWorkRequestBuilder<CollectNewArticlesWorker>()
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .addTag(tag)
                    .build()
        }
        collectNewArticlesJobsTag = tag

        workManager.enqueue(jobRequests).await()

        workManager.getWorkInfosByTagLiveData(tag).asFlow()
                .takeWhile { workInfos ->
                    workInfos.any { !it.state.isFinished }
                }
                .collect()
    }

    private suspend fun updateArticlesStatus() {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val tag = UUID.randomUUID().toString()
        val jobRequests = databaseService.getFeeds()
            .filter {
                it.id == feedId || feedId == ApiService.ALL_ARTICLES_FEED_ID
            }
            .shuffled()
            .map { feed ->
                val inputData = UpdateArticleStatusWorker.getInputData(
                    account, feed.id, numberOfLatestArticlesToRefresh)

                OneTimeWorkRequestBuilder<UpdateArticleStatusWorker>()
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .addTag(tag)
                    .build()
            }
        updateStatusJobsTag = tag

        workManager.enqueue(jobRequests).await()

        workManager.getWorkInfosByTagLiveData(tag).asFlow()
                .takeWhile { workInfos ->
                    workInfos.any { !it.state.isFinished }
                }
                .collect()
    }

    override fun onSyncCancelled() {
        super.onSyncCancelled()
        syncInfoAndFeedWorkId?.let {
            workManager.cancelWorkById(it)
        }
        collectNewArticlesJobsTag?.let {
            workManager.cancelAllWorkByTag(it)
        }
        updateStatusJobsTag?.let {
            workManager.cancelAllWorkByTag(it)
        }
        Timber.i("Synchronization was cancelled")
    }

}

