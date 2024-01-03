/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
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
import com.geekorum.ttrss.webapi.ApiCallException


internal class MockManageFeedService : ManageFeedService {
    var unsubscribeFromFeedResult = true
    var subscribeToFeedResult = ResultCode.SUCCESS

    var apiCallException: ApiCallException? = null

    override suspend fun unsubscribeFromFeed(feedId: Long): Boolean {
        apiCallException?.let {
            throw it
        }
        return unsubscribeFromFeedResult
    }

    override suspend fun subscribeToFeed(
        feedUrl: String, categoryId: Long, feedLogin: String, feedPassword: String
    ): ResultCode {
        apiCallException?.let {
            throw it
        }
        return subscribeToFeedResult
    }

}

internal class MockWorkerComponent(
    private val manageFeedService: MockManageFeedService
): WorkerComponent {
    override fun getManageFeedService(): ManageFeedService = manageFeedService

    class Builder(
        private val manageFeedService: MockManageFeedService
    ) : WorkerComponent.Builder {
        override fun setAccount(account: Account): WorkerComponent.Builder = this

        override fun build(): WorkerComponent = MockWorkerComponent(manageFeedService)
    }
}
