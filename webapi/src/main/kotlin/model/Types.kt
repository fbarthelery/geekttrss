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
@file:UseSerializers(Attachment.FixInvalidJson::class)
package com.geekorum.ttrss.webapi.model

import androidx.annotation.RequiresApi
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.StructureKind
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.longOrNull
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Types of the TinyTinyRss API.
 */
@Serializable
data class Feed(
    val id: Long = 0,

    val title: String = "",

    @SerialName("display_title")
    val displayTitle: String = "",

    @SerialName("feed_url")
    val url: String = "",

    @SerialName("unread")
    val nbUnreadArticles: Int = 0,

    @SerialName("has_icon")
    val hasIcon: Boolean = false,

    @SerialName("cat_id")
    val categoryId: Long = 0,

    @SerialName("last_updated")
    val lastUpdatedTimestamp: Long = 0,

    @SerialName("order_id")
    val orderId: Int = 0,

    @SerialName("is_cat")
    val isCategory: Boolean = false,

    @SerialName("always_display_as_feed")
    val alwaysDisplayAsFeed: Boolean = false
) {

    @delegate:Transient
    val lastUpdatedDate: LocalDateTime by lazy @RequiresApi(26) {
        LocalDateTime.ofEpochSecond(lastUpdatedTimestamp, 0, ZoneOffset.UTC)
    }

}

@Serializable
data class FeedCategory(
    val id: Long = 0,
    val title: String = "",

    @SerialName("unread")
    val nbUnreadArticles: Int = 0,

    @SerialName("order_id")
    val orderId: Int = 0
)

@Serializable
data class Headline(
    val id: Long = 0,
    val guid: String = "",
    val title: String = "",
    val link: String = "",
    val content: String = "",
    val excerpt: String = "",
    val author: String = "",
    val note: String? = "",
    val unread: Boolean = false,
    val marked: Boolean = false,
    val published: Boolean = false,
    val score: Int = 0,
    @SerialName("is_updated")
    val isUpdated: Boolean = false,
    val selected: Boolean = false,
    val tags: List<String> = emptyList(),

    @SerialName("feed_id")
    val feedId: Long? = 0,

    @SerialName("feed_title")
    val feedTitle: String = "",

    @SerialName("comments_link")
    val commentsLink: String = "",

    @SerialName("updated")
    val lastUpdatedTimestamp: Long = 0,

    @SerialName("comments_count")
    val nbComments: Long = 0,

    @SerialName("always_display_attachments")
    val alwaysDisplayAttachment: Boolean = false,

    @SerialName("flavor_image")
    var flavorImage: String? = "",

    @SerialName("flavor_stream")
    val flavorStream: String? = "",
    val attachments: List<Attachment> = emptyList(),

    // unuseful for now
    val labels: List<LabelInfo> = emptyList(),
    val lang: String? = ""

) {

    @delegate:Transient
    val lastUpdatedDate: LocalDateTime by lazy @RequiresApi(26) {
        LocalDateTime.ofEpochSecond(lastUpdatedTimestamp, 0, ZoneOffset.UTC)
    }

}


@Serializable(LabelInfo.OwnSerializer::class)
data class LabelInfo(
    val id: Long,
    val title: String = "",
    val foregroundColor: String = "",
    val backgroundColor: String = ""
) {
    @Serializer(LabelInfo::class)
    internal object OwnSerializer : KSerializer<LabelInfo> {
        @OptIn(ImplicitReflectionSerializer::class)
        override val descriptor: SerialDescriptor = SerialDescriptor(LabelInfo::class.qualifiedName!!, StructureKind.LIST)

        override fun deserialize(decoder: Decoder): LabelInfo {
            val contentDecoder = decoder.beginStructure(descriptor)
            val id: Long = contentDecoder.decodeLongElement(descriptor, contentDecoder.decodeElementIndex(descriptor))
            val title = contentDecoder.decodeStringElement(descriptor, contentDecoder.decodeElementIndex(descriptor))
            val foregroundColor = contentDecoder.decodeStringElement(descriptor, contentDecoder.decodeElementIndex(descriptor))
            val backgroundColor = contentDecoder.decodeStringElement(descriptor, contentDecoder.decodeElementIndex(descriptor))
            contentDecoder.endStructure(descriptor)
            return LabelInfo(id, title, foregroundColor, backgroundColor)
        }

        override fun serialize(encoder: Encoder, value: LabelInfo) {
            TODO("not implemented")
        }
    }
}

/**
 * An Attachment.
 * This object seems to be an HashMap with some indexed keys.
 * Not sure for the order of duration and width/heigh
 */
@Serializable
data class Attachment(
        val id: Long,
        @SerialName("post_id")
        val postId: Long,
        @SerialName("content_url")
        val contentUrl: String,
        @SerialName("content_type")
        val contentType: String = "",
        val title: String = "",
        val duration: Long = 0,
        val width: Int = 0,
        val height: Int = 0,
        @SerialName("0")
        private val indexedId: Long = 0,
        @SerialName("1")
        private val indexedContentUrl: String = "",
        @SerialName("2")
        private val indexedContentType: String = "",
        @SerialName("3")
        private val indexedTitle: String = "",
        @SerialName("4")
        private val indexedDuration: Long = 0,
        @SerialName("5")
        private val indexedPostId: Long = 0,
        @SerialName("6")
        private val indexedWidth: Int = 0,
        @SerialName("7")
        private val indexedHeight: Int = 0
) {

    @Serializer(Attachment::class)
    internal object FixInvalidJson : JsonTransformingSerializer<Attachment>(
        serializer(),
        "FixInvalidJson") {
        override fun readTransform(element: JsonElement): JsonElement {
            val correctedJson = element.jsonObject.mapValues { (k, v) ->
                when (k) {
                    "duration", "4" -> JsonPrimitive(v.longOrNull ?: 0)
                    else -> v
                }
            }
            return JsonObject(correctedJson)
        }
    }

}
