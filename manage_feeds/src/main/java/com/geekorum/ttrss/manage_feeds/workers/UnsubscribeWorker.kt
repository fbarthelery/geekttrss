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
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.geekorum.ttrss.webapi.ApiCallException
import javax.inject.Inject

/**
 * Background worker to unsubscribe from a feed.
 */
class UnsubscribeWorker(
    appContext: Context,
    private val params: WorkerParameters,
    private val apiService: ManageFeedService
) : CoroutineWorker(
    appContext, params
) {
    override suspend fun doWork(): Result {
        val feedId = params.inputData.getLong("feed_id", -1)
        if (feedId == -1L) {
            return Result.failure()
        }
        return try {
            val success = apiService.unsubscribeFromFeed(feedId)
            if (success) {
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: ApiCallException) {
            // TODO check when we can retry or not
            Result.retry()
        }
    }

    companion object {
        fun getInputData(account: Account,
                         feedId: Long): Data {
            return workDataOf(
                "account_name" to account.name,
                "account_type" to account.type,
                "feed_id" to feedId
            )
        }
    }


    class Factory @Inject constructor() : ManageFeedWorkerFactory() {

        override fun createWorker(
            appContext: Context, workerClassName: String, workerParameters: WorkerParameters
        ): ListenableWorker? {
            if (workerClassName != UnsubscribeWorker::class.java.name) {
                return null
            }
            val account = with(workerParameters.inputData) {
                val accountName = getString("account_name")
                val accountType = getString("account_type")
                Account(accountName, accountType)
            }

            val manageFeedComponent = createManageFeedComponent(appContext)
            val apiService = manageFeedComponent.createWorkerComponent()
                .setAccount(account)
                .build()
                .getManageFeedService()

            return UnsubscribeWorker(appContext, workerParameters, apiService)
        }
    }
}


