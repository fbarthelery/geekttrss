/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2020 by Frederic-Charles Barthelery.
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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Request payload to get the version of the TtRss server.
 */
@Keep
@Serializable
class GetVersionRequestPayload : LoggedRequestPayload() {

    @SerialName("op")
    override val operation = "getVersion"
}

@Serializable(GetVersionResponsePayload.OwnSerializer::class)
data class GetVersionResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    override val content: Content
) : ResponsePayload<GetVersionResponsePayload.Content>() {

    val version: String?
        get() = content.version

    @Serializable
    data class Content(
        val version: String? = null,

        override var error: Error? = null

    ) : BaseContent()

    @Serializer(GetVersionResponsePayload::class)
    internal object OwnSerializer : KSerializer<GetVersionResponsePayload> {
        override fun serialize(encoder: Encoder, value: GetVersionResponsePayload) {
            TODO("not implemented")
        }

        override fun deserialize(decoder: Decoder): GetVersionResponsePayload {
            val contentDecoder = decoder.beginStructure(descriptor)
            lateinit var content: Content
            var seq: Int? = null
            var status = 0
            loop@ while (true) {
                when (val i = contentDecoder.decodeElementIndex(descriptor)) {
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
            return GetVersionResponsePayload(
                content = content,
                sequence = seq,
                status = status
            )
        }
    }
}

/**
 * Request payload to get the api level of the TtRss server.
 */
@Keep
@Serializable
class GetApiLevelRequestPayload : LoggedRequestPayload() {

    @SerialName("op")
    override val operation = "getApiLevel"
}

@Serializable(GetApiLevelResponsePayload.OwnSerializer::class)
data class GetApiLevelResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    override val content: Content
) : ResponsePayload<GetApiLevelResponsePayload.Content>() {

    val level: Int?
        get() = content.level

    @Serializable
    data class Content(
        val level: Int? = null,

        override var error: Error? = null

    ) : BaseContent()

    @Serializer(GetApiLevelResponsePayload::class)
    internal object OwnSerializer : KSerializer<GetApiLevelResponsePayload> {
        override fun serialize(encoder: Encoder, value: GetApiLevelResponsePayload) {
            TODO("not implemented")
        }

        override fun deserialize(decoder: Decoder): GetApiLevelResponsePayload {
            val contentDecoder = decoder.beginStructure(descriptor)
            lateinit var content: Content
            var seq: Int? = null
            var status = 0
            loop@ while (true) {
                when (val i = contentDecoder.decodeElementIndex(descriptor)) {
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
            return GetApiLevelResponsePayload(
                content = content,
                sequence = seq,
                status = status
            )
        }
    }
}
