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
package com.geekorum.ttrss.providers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class PurgeArticlesWorker(
    appContext: Context,
    params: WorkerParameters,
    private val articlesProvidersDao: ArticlesProvidersDao
) : Worker(appContext, params) {
    override fun doWork(): Result {
        purgeOldArticles()
        return Result.success()
    }

    private fun purgeOldArticles() {
        // older than 3 months
        val oldTimeSec = System.currentTimeMillis() / 1000 - TimeUnit.DAYS.toSeconds(90)

        val deleted = articlesProvidersDao.deleteNonImportantArticlesBeforeTime(oldTimeSec)
        Timber.i("Purge $deleted old articles")
    }

    class Factory @Inject constructor(
        private val articlesProvidersDao: ArticlesProvidersDao
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context, workerClassName: String, workerParameters: WorkerParameters
        ): ListenableWorker? {
            if (workerClassName != PurgeArticlesWorker::class.java.name) {
                return null
            }
            return PurgeArticlesWorker(appContext, workerParameters, articlesProvidersDao)
        }
    }

}
