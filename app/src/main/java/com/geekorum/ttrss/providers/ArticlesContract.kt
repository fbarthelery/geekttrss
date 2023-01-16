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
package com.geekorum.ttrss.providers

import android.net.Uri
import android.provider.BaseColumns
import androidx.core.net.toUri
import com.geekorum.ttrss.BuildConfig

/**
 * Allows to interact with the ArticlesProvider.
 */
object ArticlesContract {
    const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.providers.articles"
    @JvmStatic
    @get:JvmName("AUTHORITY_URI")
    val AUTHORITY_URI = "content://$AUTHORITY".toUri()

    object Article {
        val CONTENT_URI: Uri = Uri.withAppendedPath(AUTHORITY_URI, "articles")
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.geekorum.ttrss.article"
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.geekorum.ttrss.article"

        // columns of an Article
        // booleans
        const val _ID = BaseColumns._ID
        const val UNREAD = "unread"
        // only use temporary to prevent cursors to remove the articles to be shown after reading it
        const val TRANSIENT_UNREAD = "transiant_unread"
        const val STARRED = "marked"
        const val PUBLISHED = "published"
        const val IS_UPDATED = "is_updated"
        // integers
        const val SCORE = "score"
        // longs
        const val LAST_TIME_UPDATE = "last_time_update"
        const val FEED_ID = "feed_id"
        const val TITLE = "title"
        const val LINK = "link"
        @Deprecated("has moved to another table")
        const val TAGS = "tags"
        const val CONTENT = "content"
        const val AUTHOR = "author"
        const val FLAVOR_IMAGE_URI = "flavor_image_uri"
        const val CONTENT_EXCERPT = "content_excerpt"
    }

    object Feed {
        val CONTENT_URI: Uri = Uri.withAppendedPath(AUTHORITY_URI, "feeds")
        // columns of a Feed
        const val _ID = BaseColumns._ID
        const val URL = "url"
        const val TITLE = "title"
        const val CAT_ID = "cat_id"
        const val LAST_TIME_UPDATE = "last_time_update"
        const val DISPLAY_TITLE = "display_title"
        const val UNREAD_COUNT = "unread_count"
        const val IS_SUBSCRIBED = "is_subscribed"
        @Deprecated("has moved to another table")
        const val ICON_URL = "feed_icon_url"
    }

    object Category {
        val CONTENT_URI: Uri = Uri.withAppendedPath(AUTHORITY_URI, "categories")
        // columns of a Category
        const val _ID = BaseColumns._ID
        const val TITLE = "title"
        const val UNREAD_COUNT = "unread_count"
    }

    object Transaction {
        val CONTENT_URI: Uri = Uri.withAppendedPath(AUTHORITY_URI, "transactions")
        // columns of a Transaction
        // long
        const val _ID = BaseColumns._ID
        const val ARTICLE_ID = "article_id"
        // String
        const val FIELD = "field"
        //boolean
        const val VALUE = "value"

        enum class Field(val apiInteger: Int) {
            STARRED(0), PUBLISHED(1), UNREAD(2), NOTE(3);

            @Deprecated("use property", ReplaceWith("apiInteger"))
            fun asApiInteger(): Int = apiInteger

        }
    }

    object ArticleTags{
        val CONTENT_URI: Uri = Uri.withAppendedPath(AUTHORITY_URI, "article_tags")
        // columns of a ArticleTags
        // long
        const val ARTICLE_ID = "article_id"
        const val TAG = "tag"
    }
}
