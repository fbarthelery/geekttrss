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
package com.geekorum.ttrss.data

import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.PrimaryKey
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone
import java.util.regex.Pattern

/**
 * Types for the data storage layer
 */

@Entity(tableName = "articles",
        foreignKeys = [ForeignKey(
            entity = Feed::class,
            parentColumns = ["_id"],
            childColumns = ["feed_id"],
            onDelete = ForeignKey.CASCADE
        )])
data class Article(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BaseColumns._ID)
    var id: Long = 0,

    @Embedded
    var contentData: ArticleContentIndexed = ArticleContentIndexed(),

    @ColumnInfo(name = "unread")
    var isUnread: Boolean = false,

    @ColumnInfo(name = "transiant_unread")
    var isTransientUnread: Boolean = false,

    @ColumnInfo(name = "marked")
    var isStarred: Boolean = false,

    @ColumnInfo(name = "published")
    var isPublished: Boolean = false,

    var score: Int = 0,

    @ColumnInfo(name = "last_time_update")
    var lastTimeUpdate: Long = 0,

    @ColumnInfo(name = "is_updated")
    var isUpdated: Boolean = false,

    var link: String = "",

    @ColumnInfo(name = "feed_id", index = true)
    var feedId: Long = 0,

    @ColumnInfo(name = "flavor_image_uri")
    var flavorImageUri: String = "",

    @ColumnInfo(name = "content_excerpt")
    var contentExcerpt: String = ""
) {

    var title: String
        get() = contentData.title
        set(value) {
            contentData.title = value
        }

    var tags: String
        get() = contentData.tags
        set(value) {
            contentData.tags = value
        }

    var content: String
        get() = contentData.content
        set(value) {
            contentData.content = value
        }

    var author: String
        get() = contentData.author
        set(value) {
            contentData.author = value
        }

    fun getDateString(): String {
        val df: DateFormat
        val today = Calendar.getInstance()
        val updateDay = Calendar.getInstance()
        updateDay.timeInMillis = lastTimeUpdate * 1000

        df = if (isSameDay(today, updateDay)) {
            TIME_FORMAT
        } else {
            DAY_FORMAT
        }

        df.timeZone = TimeZone.getDefault()
        return df.format(updateDay.timeInMillis)
    }

    fun setTagsAsList(tags: List<String>) {
        this.tags = getTagsAsString(tags)
    }

    private fun getTagsAsString(tags: List<String>): String {
        return tags.joinToString()
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean {
        return (a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.MONTH) == b.get(Calendar.MONTH)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR))
    }

    /**
     * Create an [ArticleAugmenter] to collect more information on the article
     */
    fun createAugmenter(): ArticleAugmenter {
        return ArticleAugmenter()
    }

    /** Helper class to compute additionnal information about an article  */
    inner class ArticleAugmenter internal constructor() {

        private val EXCERPT_MAX_LENGTH = 256
        private val articleDocument: Document
        private var flavorImageElement: Element?

        init {
            articleDocument = Jsoup.parse(content)
            flavorImageElement = findFlavorImageElement()
        }

        fun getFlavorImageUri(): String {
            flavorImageElement?.let { element ->
                if ("iframe" == element.tagName().toLowerCase()) {
                    val srcEmbed = element.attr("src")

                    if (srcEmbed.isNotEmpty()) {
                        val pattern = Pattern.compile("/embed/([\\w-]+)")
                        val matcher = pattern.matcher(srcEmbed)

                        if (matcher.find()) {
                            val youtubeVid = matcher.group(1)
                            return "https://img.youtube.com/vi/$youtubeVid/hqdefault.jpg"
                        }
                    }
                } else if ("img" == element.tagName().toLowerCase()) {
                    var src = element.attr("src")
                    if (src.startsWith("//")) {
                        src = "https:" + src
                    }
                    return src
                }
            }
            return ""
        }

        // calculate the excerpt
        fun getContentExcerpt(): String {
            var excerpt = articleDocument.text()
            if (excerpt.length > EXCERPT_MAX_LENGTH) {
                excerpt = excerpt.substring(0, EXCERPT_MAX_LENGTH) + "…"
            }
            return excerpt
        }

        private fun findFlavorImageElement(): Element? {
            val mediaList = articleDocument.select("img,video,iframe[src*=youtube.com/embed/]")
            val iframe = mediaList.filter { element -> "iframe" == element.tagName().toLowerCase() }.firstOrNull()
            val img = mediaList.filter { element -> "img" == element.tagName().toLowerCase() }.firstOrNull()

            return iframe ?: img
        }
    }

    companion object {

        private val TIME_FORMAT = SimpleDateFormat.getTimeInstance()
        private val DAY_FORMAT = SimpleDateFormat.getDateInstance()
    }

}

data class ArticleContentIndexed(
    var title: String = "",
    var tags: String = "",
    var content: String = "",
    var author: String = "")


@Entity
@Fts4(contentEntity = Article::class)
data class ArticleFTS(
    @Embedded
    val content: ArticleContentIndexed
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    @ColumnInfo(name = BaseColumns._ID)
    var id: Long = 0,

    var title: String = "",

    @ColumnInfo(name = "unread_count")
    var unreadCount: Int = 0
)


@Entity(tableName = "feeds",
        foreignKeys = [ForeignKey(entity = Category::class,
            parentColumns = ["_id"],
            childColumns = ["cat_id"],
            deferred = true)
        ])
data class Feed(

    @PrimaryKey
    @ColumnInfo(name = BaseColumns._ID)
    var id: Long = 0,

    var url: String = "",
    var title: String = "",

    @ColumnInfo(name = "cat_id", index = true)
    var catId: Long = 0,

    @ColumnInfo(name = "display_title")
    var displayTitle: String = "",

    @ColumnInfo(name = "last_time_update")
    var lastTimeUpdate: Long = 0,

    @ColumnInfo(name = "unread_count")
    var unreadCount: Int = 0
) {

    val isFreshFeed
        get() = id == FEED_ID_FRESH

    val isPublishedFeed
        get() = id == FEED_ID_PUBLISHED

    val isStarredFeed
        get() = id == FEED_ID_STARRED

    val isArchivedFeed
        get() = id == FEED_ID_ARCHIVED

    val isAllArticlesFeed
        get() = id == FEED_ID_ALL_ARTICLES

    companion object {

        // special feed ids when it's not a category
        const val FEED_ID_ARCHIVED = 0L
        const val FEED_ID_STARRED = -1L
        const val FEED_ID_PUBLISHED = -2L
        const val FEED_ID_FRESH = -3L
        const val FEED_ID_ALL_ARTICLES = -4L

        private val virtualFeedsIds by lazy {
            listOf(FEED_ID_ARCHIVED, FEED_ID_STARRED, FEED_ID_PUBLISHED, FEED_ID_FRESH, FEED_ID_ALL_ARTICLES)
        }

        @JvmStatic
        fun isVirtualFeed(id: Long) = id in virtualFeedsIds

        @JvmStatic
        fun createVirtualFeedForId(id: Long): Feed {
            return when (id) {
                FEED_ID_ARCHIVED -> createVirtualFeed(FEED_ID_ARCHIVED, "Archives")
                FEED_ID_STARRED -> createVirtualFeed(FEED_ID_STARRED, "Starred articles")
                FEED_ID_PUBLISHED -> createVirtualFeed(FEED_ID_PUBLISHED, "Published articles")
                FEED_ID_FRESH -> createVirtualFeed(FEED_ID_FRESH, "Fresh articles")
                FEED_ID_ALL_ARTICLES -> createVirtualFeed(FEED_ID_ALL_ARTICLES, "All articles")
                else -> throw IllegalArgumentException("Unknown virtual Feed id $id")
            }
        }

        private fun createVirtualFeed(feedId: Long, title: String): Feed {
            return Feed(id = feedId, title = title)
        }
    }
}


@Entity(tableName = "transactions",
        foreignKeys = [ForeignKey(
            entity = Article::class,
            parentColumns = ["_id"],
            childColumns = ["article_id"],
            onDelete = ForeignKey.CASCADE
        )])
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BaseColumns._ID)
    var id: Long = 0,

    @ColumnInfo(name = "article_id", index = true)
    var articleId: Long = 0,

    var field: String = "",
    var value: Boolean = false
)
