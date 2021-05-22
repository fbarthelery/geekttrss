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
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.geekorum.ttrss.accounts.ServerInformation
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.AccountInfo
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.network.ServerInfo
import com.geekorum.ttrss.sync.DatabaseService
import com.geekorum.ttrss.webapi.ApiCallException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.geekorum.ttrss.data.Account as DataAccount

/**
 * Update account information from the network to the local database.
 */
@HiltWorker
class UpdateAccountInfoWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    syncWorkerComponentBuilder: SyncWorkerComponent.Builder,
    private val dispatchers: CoroutineDispatchersProvider
) : BaseSyncWorker(context, workerParams, syncWorkerComponentBuilder) {

    private val account: Account = syncWorkerComponent.account
    private val serverInformation: ServerInformation = syncWorkerComponent.serverInformation
    private val apiService: ApiService = syncWorkerComponent.apiService
    private val databaseService: DatabaseService = syncWorkerComponent.databaseService

    override suspend fun doWork(): Result = withContext(dispatchers.io) {
        try {
            updateAccountInfo()
            Result.success()
        } catch (e: ApiCallException) {
            Timber.w(e, "unable to update account info")
            Result.failure()
        }
    }

    private suspend fun updateAccountInfo() {
        Timber.d("update account info")
        val serverInfoResult = apiService.getServerInfo()
        val accountInfo = databaseService.getAccountInfo(account.name, serverInformation.apiUrl)
                ?:  AccountInfo(DataAccount(account.name, serverInformation.apiUrl),
                        "", 0)
        val updatedInfo = makeUpdatedAccountInfo(accountInfo, serverInfoResult)
        databaseService.insertAccountInfo(updatedInfo)
    }

    private fun makeUpdatedAccountInfo(
            currentInfo: AccountInfo, serverInfoResult: ServerInfo
    ): AccountInfo {
        return currentInfo.copy(
                serverVersion = serverInfoResult.serverVersion ?: currentInfo.serverVersion,
                apiLevel = serverInfoResult.apiLevel ?: currentInfo.apiLevel)
    }


}
