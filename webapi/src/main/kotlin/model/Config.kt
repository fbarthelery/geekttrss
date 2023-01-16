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
@file:OptIn(ExperimentalSerializationApi::class)
package com.geekorum.ttrss.webapi.model

import androidx.annotation.Keep
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject

/**
 * Request payload to get the configuration of the TtRss server.
 */
@Keep
@Serializable
class GetConfigRequestPayload : LoggedRequestPayload() {

    @SerialName("op")
    override val operation = "getConfig"
}

@Serializable(GetConfigResponsePayload.OwnSerializer::class)
data class GetConfigResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    override val content: Content
) : ResponsePayload<GetConfigResponsePayload.Content>() {

    val iconsDir: String?
        get() = content.iconsDir

    val iconsUrl: String?
        get() = content.iconsUrl

    val numFeeds: Int?
        get() = content.numFeeds

    val daemonIsRunning: Boolean?
        get() = content.daemonIsRunning

    @Serializable
    data class Content(
        @SerialName("daemon_is_running")
        val daemonIsRunning: Boolean? = null,
        @SerialName("icons_dir")
        val iconsDir: String? = null,
        @SerialName("icons_url")
        val iconsUrl: String? = null,
        @SerialName("num_feeds")
        val numFeeds: Int? = null,
        @SerialName("custom_sort_types")
        val customSortTypes: List<JsonObject> = emptyList(),
        override var error: Error? = null

    ) : BaseContent()

    @Serializer(GetConfigResponsePayload::class)
    internal object OwnSerializer : KSerializer<GetConfigResponsePayload> {
        override fun serialize(encoder: Encoder, value: GetConfigResponsePayload) {
            TODO("not implemented")
        }

        override fun deserialize(decoder: Decoder): GetConfigResponsePayload {
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
                        content =
                            contentDecoder.decodeSerializableElement(contentSerializer.descriptor,
                                i,
                                contentSerializer)
                    }
                }
            }
            contentDecoder.endStructure(descriptor)
            return GetConfigResponsePayload(
                content = content,
                sequence = seq,
                status = status
            )
        }
    }
}
