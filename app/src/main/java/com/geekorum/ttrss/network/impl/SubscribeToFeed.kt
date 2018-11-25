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
package com.geekorum.ttrss.network.impl

import androidx.annotation.Keep
import kotlinx.serialization.SerialName

/**
 * Request Payload to subscribe to a new feed
 */
@Keep
internal class SubscribeToFeedRequestPayload(
    @SerialName("feed_url")
    private val feedUrl: String,

    @SerialName("category_id")
    private val categoryId: Long = 0,

    @SerialName("login")
    private val feedLogin: String = "",

    @SerialName("password")
    private val feedPassword: String = ""
) : LoggedRequestPayload() {

    @SerialName("op")
    override val operation = "subscribeToFeed"
}


/**
 * Result code for the SubscribeToFeedResponsePayload
 */
internal enum class SubscribeResultCode(val code: Int) {
    FEED_ALREADY_EXIST(0), FEED_ADDED(1), INVALID_URL(2),
    NO_FEED_IN_HTML_URL(3), MULTIPLE_FEEDS_IN_HTML_URL(4),
    ERROR_DOWNLOADING_URL(5), INVALID_CONTENT_URL(6);

    companion object {
        fun valueOf(code: Int): SubscribeResultCode {
            return SubscribeResultCode.values().firstOrNull { it.code == code } ?: throw IllegalArgumentException()
        }
    }
}

/**
 * Response payload of subscribe to feed request
 */
@Keep
internal data class SubscribeToFeedResponsePayload(
    override val sequence: Int? = null,
    override val status: Int = 0,
    override val content: SubscribeToFeedResponseContent
) : ResponsePayload<SubscribeToFeedResponseContent>() {

    private val resultCode
        get() = SubscribeResultCode.valueOf(content.resultCode)

    val success: Boolean
        get() = (resultCode == SubscribeResultCode.FEED_ALREADY_EXIST || resultCode == SubscribeResultCode.FEED_ADDED)

}

internal data class SubscribeToFeedResponseContent(
    @SerialName("code")
    val resultCode: Int = 0,

    @SerialName("message")
    val message: String = "",

    @SerialName("feed_id")
    val feedId: Long = 0,
    override var error: String? = null
) : BaseContent()
