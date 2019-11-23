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
package com.geekorum.ttrss.webapi.model

import androidx.annotation.Keep
import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.nullable

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
            return values().firstOrNull { it.code == code } ?: throw IllegalArgumentException()
        }
    }
}

/**
 * Response payload of subscribe to feed request
 */
@Keep
@Serializable(SubscribeToFeedResponsePayload.OwnSerializer::class)
data class SubscribeToFeedResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    override val content: Content
) : ResponsePayload<SubscribeToFeedResponsePayload.Content>() {

    private val resultCode
        get() = content.status?.let { SubscribeResultCode.valueOf(it.resultCode) }

    val success: Boolean
        get() = (resultCode == SubscribeResultCode.FEED_ALREADY_EXIST || resultCode == SubscribeResultCode.FEED_ADDED)

    @Serializer(SubscribeToFeedResponsePayload::class)
    object OwnSerializer : KSerializer<SubscribeToFeedResponsePayload> {
        override fun serialize(encoder: Encoder, obj: SubscribeToFeedResponsePayload) {
            TODO("not implemented")
        }

        override fun deserialize(decoder: Decoder): SubscribeToFeedResponsePayload {
            val contentDecoder = decoder.beginStructure(descriptor)
            lateinit var content: Content
            var seq: Int? = null
            var status = 0
            loop@ while (true) {
                when (val i = contentDecoder.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> seq = contentDecoder.decodeNullableSerializableElement(descriptor, i,
                        IntSerializer.nullable)
                    1 -> status = contentDecoder.decodeIntElement(descriptor, i)
                    2 -> {
                        val contentSerializer = Content.serializer()
                        content = contentDecoder.decodeSerializableElement(contentSerializer.descriptor, i,
                            contentSerializer)
                    }
                }
            }
            contentDecoder.endStructure(descriptor)
            return SubscribeToFeedResponsePayload(
                content = content,
                sequence = seq,
                status = status
            )
        }
    }


    @Serializable
    data class Content(
        val status: Status? = null,
        override var error: Error? = null
    ) : BaseContent() {

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
@Serializable(UnsubscribeFeedResponsePayload.OwnSerializer::class)
data class UnsubscribeFeedResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    override val content: Content
) : ResponsePayload<UnsubscribeFeedResponsePayload.Content>() {

    @Serializer(UnsubscribeFeedResponsePayload::class)
    internal object OwnSerializer : KSerializer<UnsubscribeFeedResponsePayload> {
        override fun serialize(encoder: Encoder, obj: UnsubscribeFeedResponsePayload) {
            TODO("not implemented")
        }

        override fun deserialize(decoder: Decoder): UnsubscribeFeedResponsePayload {
            val contentDecoder = decoder.beginStructure(descriptor)
            lateinit var content: Content
            var seq: Int? = null
            var status = 0
            loop@ while (true) {
                when (val i = contentDecoder.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> seq = contentDecoder.decodeNullableSerializableElement(descriptor, i,
                        IntSerializer.nullable)
                    1 -> status = contentDecoder.decodeIntElement(descriptor, i)
                    2 -> {
                        val contentSerializer = Content.serializer()
                        content = contentDecoder.decodeSerializableElement(contentSerializer.descriptor, i,
                            contentSerializer)
                    }
                }
            }
            contentDecoder.endStructure(descriptor)
            return UnsubscribeFeedResponsePayload(
                content = content,
                sequence = seq,
                status = status
            )
        }
    }

    @Serializable
    data class Content(
        val status: Status? = null,
        override var error: Error? = null
    ) : BaseContent() {

        enum class Status {
            OK,
            KO
        }
    }
}
