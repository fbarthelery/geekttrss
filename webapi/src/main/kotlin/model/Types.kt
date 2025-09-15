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
package com.geekorum.ttrss.webapi.model

import androidx.annotation.RequiresApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*
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
    @Serializable(DefaultNullableIntSerializer::class)
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
    val alwaysDisplayAsFeed: Boolean = false,

    @SerialName("last_error")
    val lastError:String = "",

    @SerialName("update_interval")
    val updateInterval: Long = 0
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
    @SerialName("flavor_kind")
    @Serializable(with = FlavorKind.OrdinalIntSerializer::class)
    val flavorKind: FlavorKind = FlavorKind.NONE,
    val attachments: List<Attachment> = emptyList(),

    // unuseful for now
    val labels: List<@Serializable(LabelInfo.LabelInfoAsListSerializer::class) LabelInfo> = emptyList(),
    val lang: String? = "",
    @SerialName("site_url")
    val siteUrl: String? = ""
) {

    @delegate:Transient
    val lastUpdatedDate: LocalDateTime by lazy @RequiresApi(26) {
        LocalDateTime.ofEpochSecond(lastUpdatedTimestamp, 0, ZoneOffset.UTC)
    }

}

@Serializable
enum class FlavorKind {
    NONE,
    ALBUM,
    VIDEO,
    YOUTUBE;

    /**
     * Enum values are written as int
     */
    internal object OrdinalIntSerializer : JsonTransformingSerializer<FlavorKind>(serializer()) {
        override fun transformDeserialize(element: JsonElement): JsonElement {
            val enumOrdinal = element.jsonPrimitive.intOrNull ?: return element
            val enumValue = values()[enumOrdinal]
            return JsonPrimitive(enumValue.toString())
        }

        override fun transformSerialize(element: JsonElement): JsonElement {
            val enumValue = valueOf(element.jsonPrimitive.content)
            return JsonPrimitive(enumValue.ordinal)
        }
    }
}


@Serializable
data class LabelInfo(
    val id: Long,
    val title: String = "",
    val foregroundColor: String = "",
    val backgroundColor: String = ""
) {

    internal object LabelInfoAsListSerializer : JsonTransformingSerializer<LabelInfo>(LabelInfo.serializer()) {
        override fun transformDeserialize(element: JsonElement): JsonElement {
            return buildJsonObject {
                val array = element.jsonArray
                put("id", array[0])
                put("title", array[1])
                put("foregroundColor", array[2])
                put("backgroundColor", array[3])
            }
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

        @Serializable(DefaultNullableLongSerializer::class)
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
        @Serializable(DefaultNullableLongSerializer::class)
        private val indexedDuration: Long = 0,
        @SerialName("5")
        private val indexedPostId: Long = 0,
        @SerialName("6")
        private val indexedWidth: Int = 0,
        @SerialName("7")
        private val indexedHeight: Int = 0
)


/**
 * An Int serializer that replace null value with default 0.
 */
object DefaultNullableIntSerializer : JsonTransformingSerializer<Int>(Int.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return JsonPrimitive(element.jsonPrimitive.intOrNull ?: 0)
    }
}

/**
 * A Long serializer that replace null value with default 0.
 */
object DefaultNullableLongSerializer : JsonTransformingSerializer<Long>(Long.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return JsonPrimitive(element.jsonPrimitive.longOrNull ?: 0)
    }
}
