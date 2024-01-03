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

import com.geekorum.ttrss.webapi.ApiCallException
import com.geekorum.ttrss.webapi.RetrofitServiceHelper
import com.geekorum.ttrss.webapi.TinyRssApi
import com.geekorum.ttrss.webapi.TokenRetriever
import com.geekorum.ttrss.webapi.model.SubscribeResultCode
import com.geekorum.ttrss.webapi.model.SubscribeToFeedRequestPayload
import com.geekorum.ttrss.webapi.model.UnsubscribeFeedRequestPayload
import timber.log.Timber

/**
 * ApiService to unsubscribe from a feed
 */
interface ManageFeedService {

    @Throws(ApiCallException::class)
    suspend fun unsubscribeFromFeed(feedId: Long): Boolean

    @Throws(ApiCallException::class)
    suspend fun subscribeToFeed(feedUrl: String, categoryId: Long = 0,
                                feedLogin: String = "", feedPassword: String = ""): ResultCode

}

enum class ResultCode {
    SUCCESS, INVALID_URL, UNKNOWN_ERROR
}



/* Implementation */


internal class RetrofitManageFeedService(
    tokenRetriever: TokenRetriever,
    private val tinyrssApi: TinyRssApi
) : ManageFeedService {

    private val helper = RetrofitServiceHelper(tokenRetriever)

    override suspend fun unsubscribeFromFeed(feedId: Long): Boolean {
        val payload = UnsubscribeFeedRequestPayload(feedId)
        val unsubscribeResult = helper.executeOrFail("Unable to unsubscribe from feed") {
            tinyrssApi.unsubscribeFromFeed(payload)
        }
        if (!unsubscribeResult.success) {
            Timber.w("Unable to unsubscribe feed. Error: ${unsubscribeResult.error}")
            return false
        }
        return true
    }

    override suspend fun subscribeToFeed(
        feedUrl: String, categoryId: Long, feedLogin: String, feedPassword: String
    ): ResultCode {
        val payload = SubscribeToFeedRequestPayload(feedUrl, categoryId, feedLogin, feedPassword)
        val subscribeResult = helper.executeOrFail("Unable to subscribe to feed") {
            tinyrssApi.subscribeToFeed(payload)
        }
        val subscribeResultCode = subscribeResult.resultCode
        return when (subscribeResultCode) {
            SubscribeResultCode.FEED_ALREADY_EXIST, SubscribeResultCode.FEED_ADDED -> ResultCode.SUCCESS
            SubscribeResultCode.INVALID_URL -> ResultCode.INVALID_URL
            else -> ResultCode.UNKNOWN_ERROR
        }
    }

}
