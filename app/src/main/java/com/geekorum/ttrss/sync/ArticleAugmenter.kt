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
package com.geekorum.ttrss.sync

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.geekorum.ttrss.data.Article
import java.util.regex.Pattern

private const val EXCERPT_MAX_LENGTH = 256

/**
 * Helper class to compute additionnal information about an article.
 * It looks for a flavor_image uri and content_excerpt when they are not provided in the Article
 */
internal class ArticleAugmenter constructor(
    private val article: Article
) {

    private val articleDocument: Document by lazy { Ksoup.parse(article.content) }
    private val flavorImageElement: Element? by lazy { findFlavorImageElement() }

    fun getFlavorImageUri(): String {
        if (article.flavorImageUri.isNotBlank()) {
            return article.flavorImageUri
        }

        flavorImageElement?.let { element ->
            when (element.tagName().lowercase()) {
                "iframe" -> {
                    val srcEmbed = element.attr("src")

                    if (srcEmbed.isNotEmpty()) {
                        val pattern = Pattern.compile("/embed/([\\w-]+)")
                        val matcher = pattern.matcher(srcEmbed)

                        if (matcher.find()) {
                            val youtubeVid = matcher.group(1)
                            return "https://img.youtube.com/vi/$youtubeVid/hqdefault.jpg"
                        }
                    }
                }
                "img" -> {
                    var src = element.attr("src")
                    if (src.startsWith("//")) {
                        src = "https:$src"
                    }
                    return src
                }
            }
        }
        return ""
    }

    // calculate the excerpt
    fun getContentExcerpt(): String {
        // tt-rss api return &hellip; instead of empty excerpt when there is none in the headline
        val excerpt = article.contentExcerpt.takeIf { it.isNotBlank() && it != "&hellip;" }
                ?: articleDocument.text()
        val continuation = if (excerpt.length > EXCERPT_MAX_LENGTH) "…" else ""
        return "${excerpt.take(EXCERPT_MAX_LENGTH)}$continuation"
    }

    private fun findFlavorImageElement(): Element? {
        val mediaList = articleDocument.select("img,video,iframe[src*=youtube.com/embed/]")
        val iframe = mediaList.firstOrNull { element -> "iframe" == element.tagName().lowercase() }
        val img = mediaList.firstOrNull { element -> "img" == element.tagName().lowercase() }

        return iframe ?: img
    }
}
