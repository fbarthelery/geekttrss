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
package com.geekorum.ttrss.add_feed

import com.geekorum.geekdroid.network.TokenRetriever
import com.geekorum.ttrss.network.ApiCallException
import com.geekorum.ttrss.network.RetrofitServiceHelper
import com.geekorum.ttrss.network.impl.SubscribeResultCode
import com.geekorum.ttrss.network.impl.SubscribeToFeedRequestPayload
import com.geekorum.ttrss.network.impl.TinyRssApi

/**
 * ApiService to subscribe to a feed
 */
interface SubscribeToFeedService {

    @Throws(ApiCallException::class)
    suspend fun subscribeToFeed(feedUrl: String, categoryId: Long = 0,
                                feedLogin: String = "", feedPassword: String = ""): ResultCode
}

enum class ResultCode {
    SUCCESS, INVALID_URL, UNKNOWN_ERROR
}


/* Implementation */

internal class RetrofitSubscribeToFeedService(
    tokenRetriever: TokenRetriever,
    private val tinyrssApi: TinyRssApi
) : SubscribeToFeedService {

    private val helper = RetrofitServiceHelper(tokenRetriever)

    override suspend fun subscribeToFeed(
        feedUrl: String, categoryId: Long, feedLogin: String, feedPassword: String
    ): ResultCode {
        val payload = SubscribeToFeedRequestPayload(feedUrl, categoryId, feedLogin, feedPassword)
        val subscribeResult = helper.executeOrFail("Unable to subscribe to feed") {
            tinyrssApi.subscribeToFeed(payload)
        }
        val subscribeResultCode = subscribeResult.content.status?.let { SubscribeResultCode.valueOf(it.resultCode) }
        return when (subscribeResultCode) {
            SubscribeResultCode.FEED_ALREADY_EXIST, SubscribeResultCode.FEED_ADDED -> ResultCode.SUCCESS
            SubscribeResultCode.INVALID_URL -> ResultCode.INVALID_URL
            else -> ResultCode.UNKNOWN_ERROR
        }
    }
}
