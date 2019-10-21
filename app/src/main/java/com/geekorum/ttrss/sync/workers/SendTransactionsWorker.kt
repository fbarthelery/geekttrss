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
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.sync.DatabaseService
import com.geekorum.ttrss.webapi.ApiCallException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import timber.log.Timber
import javax.inject.Inject

/**
 * Send the saved transactions to the tinyrss service.
 */
class SendTransactionsWorker(
        context: Context,
        workerParams: WorkerParameters,
        private val dispatchers: CoroutineDispatchersProvider,
        private val apiService: ApiService,
        private val databaseService: DatabaseService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(dispatchers.io)  {
        try {
            sendTransactions()
            Result.success()
        } catch (e: ApiCallException) {
            Timber.w(e, "unable to send transactions")
            Result.failure()
        }
    }

    @Throws(ApiCallException::class)
    private suspend fun sendTransactions() {
        val transactions = databaseService.getTransactions()
        Timber.i("Sending ${transactions.size} pending transactions")
        transactions.forEach { transaction ->
            databaseService.runInTransaction {
                val field = ArticlesContract.Transaction.Field.valueOf(transaction.field)
                val article = checkNotNull(databaseService.getArticle(transaction.articleId)) {
                    "article ${transaction.articleId} does not exists"
                }
                val value = transaction.value
                updateArticleField(transaction.articleId, field, value)
                when (field) {
                    ArticlesContract.Transaction.Field.PUBLISHED -> article.isPublished = value
                    ArticlesContract.Transaction.Field.UNREAD -> {
                        article.isUnread = value
                        article.isTransientUnread = value
                    }
                    ArticlesContract.Transaction.Field.STARRED -> article.isStarred = value
                    else -> throw IllegalArgumentException("Unknown field type")
                }
                databaseService.updateArticle(article)
                databaseService.deleteTransaction(transaction)
            }
            yield()
        }
    }

    @Throws(ApiCallException::class)
    private fun updateArticleField(id: Long, field: ArticlesContract.Transaction.Field, value: Boolean) = runBlocking {
        apiService.updateArticleField(id, field, value)
    }

    class WorkerFactory @Inject constructor(
            syncWorkerComponentBuilder: SyncWorkerComponent.Builder
    ) : SyncWorkerFactory(syncWorkerComponentBuilder) {

        override fun createWorker(
                appContext: Context, workerClassName: String, workerParameters: WorkerParameters
        ): ListenableWorker? {
            if (workerClassName != SendTransactionsWorker::class.java.name) {
                return null
            }

            val syncWorkerComponent = createSyncWorkerComponent( workerParameters)
            return with(syncWorkerComponent) {
                SendTransactionsWorker(appContext, workerParameters,
                dispatchers,
                apiService,
                databaseService)
            }
        }
    }
}
