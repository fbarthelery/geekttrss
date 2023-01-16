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
package com.geekorum.favikonsnoop.snoopers

import com.geekorum.favikonsnoop.FaviconInfo
import com.geekorum.favikonsnoop.Snooper
import okhttp3.HttpUrl
import okio.BufferedSource
import org.jsoup.Jsoup

open class LinkRelSnooper internal constructor(
    private val relValue: String
) : Snooper() {

    override fun snoop(baseUrl: HttpUrl, content: BufferedSource): Collection<FaviconInfo> {
        val document = runCatching { Jsoup.parse(content.inputStream() , null, baseUrl.toString()) }

        return document.getOrNull()?.head()?.let { head ->
            head.getElementsByTag("link")
                .filter {
                    relValue in it.attr("rel").split("\\s".toRegex())
                }
                .flatMap {
                    val url = it.attr("abs:href")
                    val type = it.attr("type").ifEmpty { null }

                    val sizes = parseSizes(it.attr("sizes"))
                    if (sizes.isEmpty()) {
                        listOf(FaviconInfo(url, mimeType = type))
                    } else {
                        sizes.map { dimension ->
                            FaviconInfo(url,
                                mimeType = type,
                                dimension = dimension
                            )
                        }
                    }
                }
        } ?: emptyList()
    }
}
