/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2021 by Frederic-Charles Barthelery.
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
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Request payload to log the user in.
 */
@Keep
@Serializable
data class LoginRequestPayload(
    val user: String,
    val password: String
) : BaseRequestPayload() {

    @SerialName("op")
    override val operation = "login"
}


/**
 * The response of a Login request.
 */
@Keep
@Serializable(LoginResponsePayload.OwnSerializer::class)
data class LoginResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    override val content: Content
) : ResponsePayload<LoginResponsePayload.Content>() {

    @Transient
    val sessionId: String?
        get() = content.sessionId

    @Transient
    val apiLevel: Int?
        get() = content.apiLevel

    @Serializable
    data class Content(
        @SerialName("session_id")
        val sessionId: String? = null,

        @SerialName("api_level")
        val apiLevel: Int? = null,

        override var error: Error? = null

    ) : BaseContent()

    @Serializer(LoginResponsePayload::class)
    internal object OwnSerializer : KSerializer<LoginResponsePayload> {
        override fun serialize(encoder: Encoder, value: LoginResponsePayload) {
            TODO("not implemented")
        }

        override fun deserialize(decoder: Decoder): LoginResponsePayload {
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
            return LoginResponsePayload(
                content = content,
                sequence = seq,
                status = status
            )
        }
    }
}
