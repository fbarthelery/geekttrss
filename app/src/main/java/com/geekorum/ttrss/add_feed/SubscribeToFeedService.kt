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

import com.geekorum.ttrss.network.ApiCallException
import com.geekorum.ttrss.network.ApiService

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


internal class SubscribeToFeedServiceApiDelegate(
    private val apiService: ApiService
) : SubscribeToFeedService {
    override suspend fun subscribeToFeed(
        feedUrl: String, categoryId: Long, feedLogin: String, feedPassword: String
    ): ResultCode {
        if (apiService.subscribeToFeed(feedUrl, categoryId, feedLogin, feedPassword)) {
            return ResultCode.SUCCESS
        }
        return ResultCode.UNKNOWN_ERROR
    }
}
