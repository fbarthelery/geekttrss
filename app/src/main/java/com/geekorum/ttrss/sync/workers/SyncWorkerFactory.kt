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

import android.accounts.Account
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

/**
 * Base class for a Sync worker factory.
 *
 * These workers requires input datas:
 *   account_name: the name of the account to sync
 *   account_type: the type of the account to sync
 */
abstract class SyncWorkerFactory(
        private val syncWorkerComponentBuilder: SyncWorkerComponent.Builder
) : WorkerFactory() {

    protected fun createSyncWorkerComponent(workerParameters: WorkerParameters): SyncWorkerComponent {
        val account = with(workerParameters.inputData) {
            val accountName = getString("account_name")
            val accountType = getString("account_type")
            Account(accountName, accountType)
        }
        return syncWorkerComponentBuilder
                .seedAccount(account)
                .build()
    }
}
