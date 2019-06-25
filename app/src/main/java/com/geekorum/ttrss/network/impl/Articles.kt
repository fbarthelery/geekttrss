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
package com.geekorum.ttrss.network.impl

import androidx.annotation.Keep
import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.makeNullable

/**
 * The payload gor a getHeadlines request.
 *
 * It allows to retrieve articles from the Tiny Tiny Rss server.
 */
@Keep
@Serializable
data class GetArticlesRequestPayload(
    @SerialName("feed_id")
    val feedId: Long,

    @SerialName("view_mode")
    val viewMode: ViewMode = ViewMode.ALL_ARTICLES,

    @SerialName("show_content")
    val showContent: Boolean = true,

    @SerialName("show_excerpt")
    val showExcerpt: Boolean = true,

    private val skip: Int = 0,

    @SerialName("since_id")
    val sinceId: Long = 0,

    val limit: Int = 200,

    @SerialName("order_by")
    val orderBy: SortOrder = SortOrder.NOTHING
) : LoggedRequestPayload() {

    @SerialName("op")
    override val operation = "getHeadlines"

    enum class ViewMode {
        @SerialName("all_articles")
        ALL_ARTICLES,
        @SerialName("unread")
        UNREAD,
        @SerialName("adaptive")
        ADAPTIVE,
        @SerialName("marked")
        MARKED,
        @SerialName("updated")
        UPDATED
    }

    enum class SortOrder {
        @SerialName("title")
        TITLE,
        @SerialName("date_reverse")
        DATE_REVERSE,
        @SerialName("feed_dates")
        FEED_DATES,
        @SerialName("")
        NOTHING
    }
}


/**
 * Request payload to update an Article.
 */
@Keep
@Serializable
data class UpdateArticleRequestPayload(
    @SerialName("article_ids")
    val articleIds: String,
    val mode: Int,
    val field: Int,
    val data: String? = null
) : LoggedRequestPayload() {

    @SerialName("op")
    override val operation = "updateArticle"
}

/**
 * The response of an update article request.
 */
@Keep
//@Serializable
data class UpdateArticleResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    override val content: UpdateArticleResponsePayload.Content
) : ResponsePayload<UpdateArticleResponsePayload.Content>() {

    @Transient
    val updated: Int = content.updated ?: 0

    @Serializable
    data class Content(
        val status: String? = null,
        val updated: Int? = null,
        override var error: Error? = null
    ): BaseContent()

  companion object {
        fun serializer(): KSerializer<UpdateArticleResponsePayload> {
            return UpdateArticleResponsePayloadSerializer()
        }
    }

    @Serializer(UpdateArticleResponsePayload::class)
    class UpdateArticleResponsePayloadSerializer : KSerializer<UpdateArticleResponsePayload> {
        override fun serialize(encoder: Encoder, obj: UpdateArticleResponsePayload) {
            TODO("not implemented")
        }

        override fun deserialize(decoder: Decoder): UpdateArticleResponsePayload {
            val contentDecoder = decoder.beginStructure(descriptor)
            lateinit var content: Content
            var seq: Int? = null
            var status = 0
            loop@ while (true) {
                when(val i = contentDecoder.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> seq = contentDecoder.decodeNullableSerializableElement(descriptor, i,
                        makeNullable(IntSerializer))
                    1 -> status = contentDecoder.decodeIntElement(descriptor, i)
                    2 -> {
                        val contentSerializer = Content.serializer()
                        content = contentDecoder.decodeSerializableElement(contentSerializer.descriptor, i,
                            contentSerializer)
                    }
                }
            }
            contentDecoder.endStructure(descriptor)
            return UpdateArticleResponsePayload(
                content = content,
                sequence = seq,
                status = status
            )
        }
    }
}
