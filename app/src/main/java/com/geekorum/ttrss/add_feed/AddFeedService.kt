/**
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2018 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.add_feed

import android.accounts.Account
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import androidx.core.app.JobIntentService
import com.geekorum.ttrss.BackgroundJobManager
import com.geekorum.ttrss.network.ApiService
import dagger.android.AndroidInjection
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject


/**
 * Service used to add a Feed in background.
 * //TODO replace with WorkManager API?
 */
class AddFeedService : JobIntentService() {

    @Inject
    internal lateinit var feedComponentBuilder: AddFeedComponent.Builder

    @Inject
    internal lateinit var backgroundJobManager: BackgroundJobManager

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    @WorkerThread
    override fun onHandleWork(work: Intent) {
        runBlocking {
            val addFeedJob = createAddFeedJob(work)
            launch {
                try {
                    if (!addFeedJob.addFeed()) {
                        Timber.e("Unable to add feed")
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Unable to add feed")
                    rescheduleJob(addFeedJob)
                }
            }
        }
    }

    private fun createAddFeedJob(work: Intent): AddFeedJob {
        val account = work.getParcelableExtra<Account>("account")
        val addFeedComponent = feedComponentBuilder.seedAccount(account).build()
        val feedUrl = work.getStringExtra("url")
        val categoryId = work.getLongExtra("categoryId", 0)
        val feedLogin = work.getStringExtra("login") ?: ""
        val feedPassword = work.getStringExtra("password") ?: ""
        return addFeedComponent.addFeedJobFactory.create(feedUrl, categoryId, feedLogin, feedPassword)
    }

    private fun rescheduleJob(job: AddFeedJob) {
        with (job) {
            backgroundJobManager.subscribeToFeed(account,feedUrl, categoryId, feedLogin, feedPassword)
        }
    }

    companion object {
        @JvmStatic
        fun subscribeToFeed(jobId: Int,
                            context: Context,
                            account: Account,
                            feedUrl: String,
                            categoryId: Long = 0,
                            feedLogin: String = "",
                            feedPassword: String = "") {
            val work = Intent().apply {
                putExtra("account", account)
                putExtra("url", feedUrl)
                putExtra("categoryId", categoryId)
                putExtra("login", feedLogin)
                putExtra("password", feedPassword)
            }
            JobIntentService.enqueueWork(context, AddFeedService::class.java, jobId, work)
        }
    }
}


class AddFeedJob private constructor(
    val application: Application,
    val account: Account,
    val apiService: ApiService,
    val feedUrl: String,
    val categoryId: Long,
    val feedLogin: String,
    val feedPassword: String
) {
    suspend fun addFeed(): Boolean {
        return apiService.subscribeToFeed(feedUrl, categoryId, feedLogin, feedPassword)
    }

    class Factory @Inject constructor(
        val application: Application,
        val account: Account,
        private val apiService: ApiService
    ) {

        fun create(feedUrl: String, categoryId: Long, feedLogin: String, feedPassword: String): AddFeedJob {
            return AddFeedJob(application, account, apiService, feedUrl, categoryId, feedLogin, feedPassword)
        }
    }

}
