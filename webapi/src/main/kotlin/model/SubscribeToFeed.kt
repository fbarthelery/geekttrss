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
@file:OptIn(ExperimentalSerializationApi::class)
package com.geekorum.ttrss.webapi.model

import androidx.annotation.Keep
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request Payload to subscribe to a new feed
 */
@Keep
@Serializable
data class SubscribeToFeedRequestPayload(
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
enum class SubscribeResultCode(val code: Int) {
    FEED_ALREADY_EXIST(0), FEED_ADDED(1), INVALID_URL(2),
    NO_FEED_IN_HTML_URL(3), MULTIPLE_FEEDS_IN_HTML_URL(4),
    ERROR_DOWNLOADING_URL(5), INVALID_CONTENT_URL(6);

    companion object {
        fun valueOf(code: Int): SubscribeResultCode {
            return entries.firstOrNull { it.code == code } ?: throw IllegalArgumentException()
        }
    }
}

/**
 * Response payload of subscribe to feed request
 */
@Keep
@Serializable
data class SubscribeToFeedResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    @Serializable(with = ContentSerializer::class)
    override val content: BaseContent
) : ResponsePayload<SubscribeToFeedResponsePayload.Content>() {

    val resultCode
        get() = typedContent?.status?.let { SubscribeResultCode.valueOf(it.resultCode) }

    val success: Boolean
        get() = (resultCode == SubscribeResultCode.FEED_ALREADY_EXIST || resultCode == SubscribeResultCode.FEED_ADDED)

    @Serializable
    data class Content(
        val status: Status? = null,
    ) : BaseContent {

        @Serializable
        data class Status(
            @SerialName("code")
            val resultCode: Int = 0,

            @SerialName("message")
            val message: String = "",

            @SerialName("feed_id")
            val feedId: Long = 0
        )
    }

    object ContentSerializer : BaseContentSerializer(Content.serializer())
}



/**
 * Request Payload to unsubscribe from a feed
 */
@Keep
@Serializable
data class UnsubscribeFeedRequestPayload(
    @SerialName("feed_id")
    private val feedId: Long
) : LoggedRequestPayload() {

    @SerialName("op")
    override val operation = "unsubscribeFeed"
}


/**
 * Response payload of unsubscribe from feed request
 */
@Keep
@Serializable
data class UnsubscribeFeedResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    @Serializable(with = ContentSerializer::class)
    override val content: BaseContent
) : ResponsePayload<UnsubscribeFeedResponsePayload.Content>() {

    private val resultCode
        get() = typedContent?.status

    val success: Boolean
        get() = resultCode == Content.Status.OK

    val error: Error?
        get() = (content as? ErrorContent)?.error

    @Serializable
    data class Content(
        val status: Status? = null,
    ) : BaseContent {

        enum class Status {
            OK,
            KO
        }
    }

    object ContentSerializer : BaseContentSerializer(Content.serializer())

}
