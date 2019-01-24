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
import com.geekorum.ttrss.network.ApiCallException
import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.Transient
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlinx.serialization.internal.makeNullable
import kotlinx.serialization.json.JsonParsingException
import kotlinx.serialization.list
import kotlinx.serialization.serializer

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

    abstract val content: T

    val isStatusOk: Boolean
        get() = status == API_STATUS_OK

    val error: ApiCallException.ApiError?
        get() = content.getErrorAsApiError()

    companion object {

        private val API_STATUS_OK = 0
        private val API_STATUS_ERR = 1
    }

}


/**
 * The content of an answer from the Tiny Tiny Rss api.
 *
 * The Api answer with a dynamic content element.
 * This content can either an object containing an error or something else depending on the request.
 * The [BaseContent] allows to deal with the error case.
 * Other classes can extends this one to add the appropriate content.
 */
abstract class BaseContent {

    abstract var error: String?

    fun getErrorAsApiError(): ApiCallException.ApiError {
        return when (error) {
            "LOGIN_ERROR" -> ApiCallException.ApiError.LOGIN_FAILED
            "API_DISABLED" -> ApiCallException.ApiError.API_DISABLED
            "NOT_LOGGED_IN" -> ApiCallException.ApiError.NOT_LOGGED_IN
            "INCORRECT_USAGE" -> ApiCallException.ApiError.API_INCORRECT_USAGE
            "UNKNOWN_METHOD" -> ApiCallException.ApiError.API_UNKNOWN_METHOD
            else -> ApiCallException.ApiError.API_UNKNOWN
        }
    }
}


/**
 * Represent the content of an Api Response when the content is a list of objects.
 *
 * @param <T> the type of element in the list.
 */
//@Serializable
data class ListContent<T>(
    @Transient
    val list: List<T> = emptyList(),
    override var error: String? = null
) : BaseContent() {

    companion object {
        fun <E> serializer(typeSerializer: KSerializer<E>): KSerializer<ListContent<E>> {
            return ListContentSerializer(typeSerializer)
        }
    }

    // Workaround for kapt bug
    @Serializer(ListContent::class)
    class ListContentSerializer<E>(
        val contentSerializer: KSerializer<E>
    ) : KSerializer<ListContent<E>> {

        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("ListContentSerializer") {
            init {
                addElement("error")
            }
        }

        override fun serialize(output: Encoder, obj: ListContent<E>) {
            TODO("not implemented")
        }

        override fun deserialize(input: Decoder): ListContent<E> {
            // fallback to error parsing
            return deserializeList(input) ?: deserializeBaseContent(input)
        }

        private fun deserializeBaseContent(input: Decoder): ListContent<E> {
            val contentDecoder = input.beginStructure(descriptor)
            var error: String? = null
            loop@ while (true) {
                when (val i = contentDecoder.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> error = contentDecoder.decodeNullableSerializableElement(descriptor, i,
                        makeNullable(String.serializer()))
                }
            }
            contentDecoder.endStructure(descriptor)
            return ListContent(error = error)
        }

        private fun deserializeList(input: Decoder): ListContent<E>? {
            val listSerializer = contentSerializer.list
            return try {
                val list = listSerializer.deserialize(input)
                ListContent(list)
            } catch (e: JsonParsingException) {
                null
            }
        }
    }

}

/**
 * The response of an Api request which returns either a List content or an error content
 *
 * @param <T> the type of element in the list.
 */
@Keep
//@Serializable
data class ListResponsePayload<T>(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int? = null,
    override val content: ListContent<T>
) : ResponsePayload<ListContent<T>>() {

    @Transient
    val result: List<T>
        get() = content.list

    companion object {
        fun <E> serializer(typeSerializer: KSerializer<E>): KSerializer<ListResponsePayload<E>> {
            return ListResponsePayloadSerializer(typeSerializer)
        }
    }

    // Workaround for kapt bug
    @Serializer(ListResponsePayload::class)
    class ListResponsePayloadSerializer<E>(
        val contentSerializer: KSerializer<E>
    ) : KSerializer<ListResponsePayload<E>> {

        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("ListResponsePayloadSerializer") {
            init {
                addElement("seq")
                addElement("status")
                addElement("content")
            }
        }

        override fun serialize(output: Encoder, obj: ListResponsePayload<E>) {
            TODO("not implemented")
        }

        override fun deserialize(input: Decoder): ListResponsePayload<E> {
            val contentDecoder = input.beginStructure(descriptor)
            lateinit var listContent: ListContent<E>
            var seq: Int? = null
            var status = 0
            loop@ while (true) {
                when (val i = contentDecoder.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> seq = contentDecoder.decodeNullableSerializableElement(descriptor, i, makeNullable(
                        IntSerializer))
                    1 -> status = contentDecoder.decodeIntElement(descriptor, i)
                    2 -> {
                        val listContentDecoder = ListContent.serializer(contentSerializer)
                        listContent = contentDecoder.decodeSerializableElement(listContentDecoder.descriptor, i,
                            listContentDecoder)
                    }
                }
            }
            contentDecoder.endStructure(descriptor)
            return ListResponsePayload(
                content = listContent,
                sequence = seq,
                status = status
            )
        }
    }
}
