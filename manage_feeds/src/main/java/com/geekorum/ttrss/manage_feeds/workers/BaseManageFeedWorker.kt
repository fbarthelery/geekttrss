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
package com.geekorum.ttrss.manage_feeds.workers

import android.accounts.Account
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.geekorum.ttrss.Application
import com.geekorum.ttrss.manage_feeds.DaggerManageFeedComponent
import com.geekorum.ttrss.sync.workers.SyncWorkerComponent

abstract class BaseManageFeedWorker(
    context: Context,
    params: WorkerParameters,
    workerComponentBuilder: WorkerComponent.Builder
) : CoroutineWorker(context, params) {

    val account = with(params.inputData) {
        val accountName = getString("account_name")
        val accountType = getString("account_type")
        Account(accountName, accountType)
    }

    protected val workerComponent = createWorkerComponent(context, workerComponentBuilder)

    private fun createWorkerComponent(appContext: Context, workerComponentBuilder: WorkerComponent.Builder): WorkerComponent {
        return workerComponentBuilder
            .setAccount(account)
            .build()
    }
}

