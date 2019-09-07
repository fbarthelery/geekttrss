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
package com.geekorum.favikonsnoop.snoopers

import com.geekorum.favikonsnoop.AdaptiveDimension
import com.geekorum.favikonsnoop.FaviconInfo
import com.geekorum.favikonsnoop.FixedDimension
import com.geekorum.favikonsnoop.Dimension
import com.geekorum.favikonsnoop.Snooper
import org.jsoup.Jsoup
import java.io.InputStream

/**
 * https://html.spec.whatwg.org/#rel-icon
 */
class LinkRelSnooper(
    val relValue: String
) : Snooper() {

    override fun snoop(baseUrl: String, content: InputStream): Collection<FaviconInfo> {
        val document = Jsoup.parse(content, null, baseUrl)

        val linksRel = document.head()?.let { head ->
            head.getElementsByTag("link")
                .filter {
                    it.attr("rel") == relValue
                }
                .flatMap {
                    val url = it.attr("abs:href")
                    val type = it.attr("type").ifEmpty { null }
                    parseSizes(it.attr("sizes")).map { dimension ->
                        FaviconInfo(url,
                            mimeType = type,
                            dimension = dimension
                            )
                    }
                }
        } ?: emptyList()
        val faviconLegacyUrl = "$baseUrl/favicon.ico"
        val legacy = FaviconInfo(faviconLegacyUrl)
        return linksRel + legacy
    }

    internal fun parseSizes(size: String): Collection<Dimension> {
        return size.splitToSequence(" ").mapNotNull { word ->
            when (word) {
                "any" -> AdaptiveDimension
                else -> {
                    runCatching {
                        val (widthStr, heightStr) = word.split("x",ignoreCase = true, limit = 2)
                        FixedDimension(widthStr.toInt(), heightStr.toInt())
                    }.getOrNull()
                }
            }
        }.toList()
    }

}
