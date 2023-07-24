/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2023 by Frederic-Charles Barthelery.
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
import kotlinx.serialization.*
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Request Payload to catch up (mark as read) a feed
 */
@Keep
@Serializable
data class CatchupFeedRequestPayload(
    @SerialName("feed_id")
    private val feedId: Long,
    @SerialName("is_cat")
    private val isCategory: Boolean,
    private val mode: Mode = Mode.ALL
) : LoggedRequestPayload() {

    @SerialName("op")
    override val operation = "catchupFeed"

    enum class Mode {
        @SerialName("all")
        ALL,
        @SerialName("1day")
        ONE_DAY,
        @SerialName("1week")
        ONE_WEEK,
        @SerialName("2week")
        TWO_WEEK
    }
}


@Keep
@Serializable(CatchupFeedResponsePayload.OwnSerializer::class)
data class CatchupFeedResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    override val content: Content
) : ResponsePayload<CatchupFeedResponsePayload.Content>() {

    @Serializable
    data class Content(
        val status: String? = null,
        override var error: Error? = null
    ): BaseContent()

    @OptIn(ExperimentalSerializationApi::class)
    @Serializer(CatchupFeedResponsePayload::class)
    internal object OwnSerializer : KSerializer<CatchupFeedResponsePayload> {
        override fun serialize(encoder: Encoder, value: CatchupFeedResponsePayload) {
            TODO("not implemented")
        }

        override fun deserialize(decoder: Decoder): CatchupFeedResponsePayload {
            val contentDecoder = decoder.beginStructure(descriptor)
            lateinit var content: Content
            var seq: Int? = null
            var status = 0
            loop@ while (true) {
                when(val i = contentDecoder.decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    0 -> seq = contentDecoder.decodeNullableSerializableElement(descriptor, i,
                        Int.serializer().nullable)
                    1 -> status = contentDecoder.decodeIntElement(descriptor, i)
                    2 -> {
                        val contentSerializer = Content.serializer()
                        content = contentDecoder.decodeSerializableElement(contentSerializer.descriptor, i,
                            contentSerializer)
                    }
                }
            }
            contentDecoder.endStructure(descriptor)
            return CatchupFeedResponsePayload(
                content = content,
                sequence = seq,
                status = status
            )
        }
    }
}
