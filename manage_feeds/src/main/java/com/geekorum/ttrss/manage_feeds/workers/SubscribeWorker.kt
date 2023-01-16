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
package com.geekorum.ttrss.manage_feeds.workers

import android.accounts.Account
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.geekorum.ttrss.webapi.ApiCallException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class SubscribeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    workerComponentBuilder: WorkerComponent.Builder
) : BaseManageFeedWorker(context, params, workerComponentBuilder) {

    private val apiService: ManageFeedService = workerComponent.getManageFeedService()

    override suspend fun doWork(): Result {
        val feedUrl = requireNotNull(inputData.getString("url"))
        val categoryId = inputData.getLong("categoryId", 0)
        val feedLogin = inputData.getString("login") ?: ""
        val feedPassword = inputData.getString("password") ?: ""

        return try {
            val result = apiService.subscribeToFeed(feedUrl, categoryId, feedLogin, feedPassword)
            if (result != ResultCode.SUCCESS) {
                Timber.e("Unable to add feed $result")
                Result.failure()
            } else {
                Result.success()
            }
        } catch (e: ApiCallException) {
            Result.retry()
        }
    }

    companion object {
        fun getInputData(account: Account,
                         feedUrl: String,
                         categoryId: Long = 0,
                         feedLogin: String = "",
                         feedPassword: String = ""): Data {
            return workDataOf(
                "account_name" to account.name,
                "account_type" to account.type,
                "url" to feedUrl,
                "categoryId" to categoryId,
                "login" to feedLogin,
                "password" to feedPassword
            )
        }
    }

}
