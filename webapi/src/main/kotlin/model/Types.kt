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

import androidx.annotation.RequiresApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
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
    var flavorStream: String? = "",

    // unuseful
    val attachments: List<JsonObject> = emptyList(),
    val labels: List<String> = emptyList(),
    val lang: String? = ""

) {

    @delegate:Transient
    val lastUpdatedDate: LocalDateTime by lazy @RequiresApi(26) {
        LocalDateTime.ofEpochSecond(lastUpdatedTimestamp, 0, ZoneOffset.UTC)
    }

}
