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
package com.geekorum.ttrss.manage_feeds.workers

import com.geekorum.geekdroid.network.TokenRetriever
import com.geekorum.ttrss.network.ApiCallException
import com.geekorum.ttrss.network.RetrofitServiceHelper
import com.geekorum.ttrss.network.impl.TinyRssApi
import com.geekorum.ttrss.network.impl.UnsubscribeFeedRequestPayload
import com.geekorum.ttrss.network.impl.UnsubscribeFeedResponsePayload.Content.Status.OK
import timber.log.Timber

/**
 * ApiService to unsubscribe from a feed
 */
interface UnsubscribeFromFeedService {

    @Throws(ApiCallException::class)
    suspend fun unsubscribeFromFeed(feedId: Long): Boolean
}


/* Implementation */


internal class RetrofitUnsubscribeFromFeedService(
    tokenRetriever: TokenRetriever,
    private val tinyrssApi: TinyRssApi
) : UnsubscribeFromFeedService {

    private val helper = RetrofitServiceHelper(tokenRetriever)

    override suspend fun unsubscribeFromFeed(feedId: Long): Boolean {
        val payload = UnsubscribeFeedRequestPayload(feedId)
        val unsubscribeResult = helper.executeOrFail("Unable to unsubscribe from feed") {
            tinyrssApi.unsubscribeFromFeed(payload)
        }
        val status = unsubscribeResult.content.status
        return when (status) {
            OK -> true
            else -> {
                Timber.w("Unable to unsubscribe feed. Error: ${unsubscribeResult.content.error}")
                false
            }
        }
    }
}
