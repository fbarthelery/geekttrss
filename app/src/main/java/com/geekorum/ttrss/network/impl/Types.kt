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

import android.os.Build.VERSION_CODES.O
import androidx.annotation.RequiresApi
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.Feed
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
    @Optional
    val displayTitle: String = "",

    @Optional
    @SerialName("feed_url")
    val url: String = "",

    @Optional
    @SerialName("unread")
    val nbUnreadArticles: Int = 0,

    @Optional
    @SerialName("has_icon")
    val hasIcon: Boolean = false,

    @Optional
    @SerialName("cat_id")
    val categoryId: Long = 0,

    @Optional
    @SerialName("last_updated")
    val lastUpdatedTimestamp: Long = 0,

    @Optional
    @SerialName("order_id")
    val orderId: Int = 0,

    @Optional
    @SerialName("is_cat")
    val isCategory: Boolean = false,

    @Optional
    @SerialName("always_display_as_feed")
    val alwaysDisplayAsFeed: Boolean = false
) {

    @delegate:Transient
    @kotlinx.serialization.Transient
    val lastUpdatedDate: LocalDateTime by lazy @RequiresApi(O) {
        LocalDateTime.ofEpochSecond(lastUpdatedTimestamp, 0, ZoneOffset.UTC)
    }

    fun toDataType(): Feed {
        return Feed().apply {
            this.id = this@Feed.id
            this.title = this@Feed.title
            this.displayTitle = this@Feed.displayTitle
            this.url = this@Feed.url
            this.unreadCount = nbUnreadArticles
            this.catId = categoryId
            this.lastTimeUpdate = lastUpdatedTimestamp
        }
    }
}

@Serializable
data class FeedCategory(
    val id: Long = 0,
    val title: String = "",

    @SerialName("unread")
    @Optional
    val nbUnreadArticles: Int = 0,

    @SerialName("order_id")
    @Optional
    val orderId: Int = 0
) {

    fun toDataType(): Category {
        return Category().apply {
            this.id = this@FeedCategory.id
            this.title = this@FeedCategory.title
            this.unreadCount = nbUnreadArticles
        }
    }
}

@Serializable
data class Headline(
    val id: Long = 0,
    val title: String = "",
    val link: String = "",
    @Optional
    val content: String = "",
    @Optional
    val excerpt: String = "",
    val author: String = "",
    val note: String? = "",
    val unread: Boolean = false,
    val marked: Boolean = false,
    val published: Boolean = false,
    val score: Int = 0,
    @SerialName("is_updated")
    val isUpdated: Boolean = false,
    @Optional
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

    // unuseful
    @Optional
    val attachments: List<Any> = emptyList(),
    val labels: List<String> = emptyList(),
    val lang: String? = ""

) {

    @delegate:Transient
    @kotlinx.serialization.Transient
    val lastUpdatedDate: LocalDateTime by lazy @RequiresApi(O) {
        LocalDateTime.ofEpochSecond(lastUpdatedTimestamp, 0, ZoneOffset.UTC)
    }


    fun toDataType(): Article {
        return Article().apply {
            this.id = this@Headline.id
            this.title = this@Headline.title
            this.content = this@Headline.content
            this.isUnread = this@Headline.unread
            this.isTransientUnread = this.isUnread
            this.isStarred = this@Headline.marked
            this.isPublished = this@Headline.published
            this.score = this@Headline.score
            this.lastTimeUpdate = this@Headline.lastUpdatedTimestamp
            this.isUpdated = this@Headline.isUpdated
            this.link = this@Headline.link
            this.feedId = this@Headline.feedId ?: 0
            this.tags = this@Headline.tags.joinToString(", ")
            this.author = this@Headline.author
            this.contentExcerpt = this@Headline.excerpt
        }
    }
}


