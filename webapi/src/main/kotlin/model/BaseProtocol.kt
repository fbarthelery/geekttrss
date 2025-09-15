/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
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
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/* Requests */

/**
 * Base payload for a request to the Tiny Tiny Rss Api.
 *
 * Each request payload is an object containing an operation and its parameters.
 */
abstract class BaseRequestPayload {
    abstract val operation: String
}


/**
 * Some request needs the user to be authenticated. This is done by using the [LoggedRequestPayload]
 * as a base and allows to insert the sessionId.
 */
@Serializable
abstract class LoggedRequestPayload : BaseRequestPayload() {

    @SerialName("sid")
    var sessionId: String? = null

}


/* Responses */

/**
 * The response of a Tiny Tiny Rss api call.
 *
 * Each response is a json object containing the status of the request and a content objet.
 *
 * @param <T> the type of the content object.
*/
@Keep
abstract class ResponsePayload<T : BaseContent> {

    abstract val sequence: Int?

    abstract val status: Int?

    abstract val content: BaseContent

    @Suppress("UNCHECKED_CAST")
    val typedContent: T?
        get() = content as? T

    companion object {
        val API_STATUS_OK = 0
        val API_STATUS_ERR = 1
    }

}

@Serializable
enum class Error {
    NO_ERROR,
    API_DISABLED,
    API_UNKNOWN,
    LOGIN_ERROR,
    INCORRECT_USAGE,
    NOT_LOGGED_IN,
    FEED_NOT_FOUND,
    UNKNOWN_METHOD,
    E_NOT_FOUND
}


@Keep
@Serializable
data class ErrorResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    override val content: ErrorContent
) : ResponsePayload<ErrorContent>()

/**
 * The content of an answer from the Tiny Tiny Rss api. This is a marker interface
 *
 * The Api answer with a dynamic content element.
 * This content can either an object containing an [ErrorContent] or something else depending on the request.
 * The [BaseContent] and [ErrorContent] allows to deal with the error case.
 * Other classes can extends this one to add the appropriate content.
 */
interface BaseContent

@Serializable
data class ErrorContent(
    var error: Error?
) : BaseContent

/**
 * Represent the content of an Api Response when the content is a list of objects.
 *
 * @param <T> the type of element in the list.
 */
@Serializable(ListContent.OwnSerializer::class)
data class ListContent<T>(
    val list: List<T> = emptyList(),
) : BaseContent {

    internal class OwnSerializer<E>(
        val contentSerializer: KSerializer<E>
    ) : KSerializer<ListContent<E>> {

        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ListContentSerializer") {}

        override fun serialize(encoder: Encoder, value: ListContent<E>) {
            TODO("not implemented")
        }

        override fun deserialize(decoder: Decoder): ListContent<E> {
            val listSerializer = ListSerializer(contentSerializer)
            val list = listSerializer.deserialize(decoder)
            return ListContent(list)
        }
    }

}

/**
 * The response of an Api request which returns either a List content or an error content
 *
 * @param <T> the type of element in the list.
 */
@Keep
@Serializable(ListResponsePayload.OwnSerializer::class)
data class ListResponsePayload<T>(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int? = null,
    override val content: BaseContent
) : ResponsePayload<ListContent<T>>() {

    @Transient
    val result: List<T>
        get() = (content as ListContent<T>).list

    internal class OwnSerializer<E>(
        private val contentSerializer: KSerializer<E>
    ) : KSerializer<ListResponsePayload<E>> {

        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ListResponsePayloadSerializer") {
            element("seq", Int.serializer().descriptor, isOptional = true)
            element("status", Int.serializer().descriptor, isOptional = true)
            element("content", ListContent.serializer(contentSerializer).descriptor)
        }

        override fun serialize(encoder: Encoder, value: ListResponsePayload<E>) {
            TODO("not implemented")
        }

        override fun deserialize(decoder: Decoder): ListResponsePayload<E> {
            val contentDecoder = decoder.beginStructure(descriptor)
            lateinit var content: BaseContent
            var seq: Int? = null
            var status = 0
            loop@ while (true) {
                when (val i = contentDecoder.decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    0 -> seq = contentDecoder.decodeNullableSerializableElement(descriptor, i,
                        Int.serializer().nullable)
                    1 -> status = contentDecoder.decodeIntElement(descriptor, i)
                    2 -> {
                        val listContentSerializer = ListContent.serializer(contentSerializer)
                        val baseContentSerializer = BaseContentSerializer(listContentSerializer)
                        content = baseContentSerializer.deserialize(decoder)
                    }
                }
            }
            contentDecoder.endStructure(descriptor)
            return ListResponsePayload(
                content = content,
                sequence = seq,
                status = status
            )
        }
    }
}

/**
 * Parse [content][BaseContent] as is :
 *  - if it contains an "error" field parse as an [ErrorContent]
 *  - else use specific content serializer
 */
open class BaseContentSerializer(
    private val contentSerializer: DeserializationStrategy<BaseContent>
) : JsonContentPolymorphicSerializer<BaseContent>(BaseContent::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<BaseContent> {
        return when {
            element is JsonObject && "error" in element.jsonObject -> ErrorContent.serializer()
            else -> contentSerializer
        }
    }
}


val BaseContent.error: Error?
    get() = (this as? ErrorContent)?.error