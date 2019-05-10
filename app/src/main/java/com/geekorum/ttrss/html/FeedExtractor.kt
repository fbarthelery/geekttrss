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
package com.geekorum.ttrss.html

import okhttp3.HttpUrl
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.inject.Inject

/**
 * Allows to extract [FeedInformation] from an Html Document.
 */
class FeedExtractor @Inject constructor() : HtmlExtractor<FeedInformation>() {

    private val VALID_FEED_MIMETYPE = listOf("application/rss+xml", "application/atom+xml")

    override fun extract(document: Document): Collection<FeedInformation>  {
        document.head()?.let {head ->
            return head.getElementsByTag("link")
                .filter { isValidLinkFeed(it) }
                .map {
                    FeedInformation(href = HttpUrl.parse(it.attr("abs:href"))!!,
                                    type = it.attr("type"),
                                    title = it.attr("title"))
                }
        }
        return emptyList()
    }

    private fun isValidLinkFeed(elem: Element): Boolean {
        val type = elem.attr("type")
        val url = elem.attr("abs:href")
        return elem.attr("rel") == "alternate"
                && type in VALID_FEED_MIMETYPE
                && HttpUrl.parse(url) != null
    }
}

/**
 * Information about a Feed.
 */
data class FeedInformation(
    val href: HttpUrl,
    val type: String = "",
    val title: String = "")
