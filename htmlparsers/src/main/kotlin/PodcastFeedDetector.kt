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
package com.geekorum.ttrss.htmlparsers

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element

/**
 * Check if a podcast feed has the required elements according to [The Podcast RSS Standard](https://github.com/Podcast-Standards-Project/PSP-1-Podcast-RSS-Specification?tab=readme-ov-file#rss-feed-elements)
 */
class PodcastFeedDetector {

    fun isPodcastFeed(feedDocument: String): Boolean {
        val document = Ksoup.parseXml(feedDocument)
        val rssElement = document.getElementsByTag("rss").singleOrNull() ?: return false
        if (!hasValidRssElement(rssElement)) return false
        val channelElement = rssElement.getElementsByTag("channel").singleOrNull() ?: return false
        if (!hasRequiredChannelElements(channelElement)) return false
        channelElement.getElementsByTag("item").firstOrNull()?.let {
            if (!hasValidItemTag(it)) return false
        }
        return true
    }

    private fun hasRequiredChannelElements(element: Element): Boolean {
        val atomLink = element.children().firstOrNull { it.tagName() == "atom:link" } ?: return false
        val title = element.children().firstOrNull { it.tagName() == "title" } ?: return false
        val description = element.children().firstOrNull { it.tagName() == "description" } ?: return false
        val link = element.children().firstOrNull { it.tagName() == "link" } ?: return false
        val language = element.children().firstOrNull { it.tagName() == "language" } ?: return false
        val itunesCategory = element.children().firstOrNull { it.tagName() == "itunes:category" } ?: return false
        val itunesExplicit = element.children().firstOrNull { it.tagName() == "itunes:explicit" } ?: return false
        val itunesImage = element.children().firstOrNull { it.tagName() == "itunes:image" } ?: return false
        return true
    }

    private fun hasValidItemTag(item: Element): Boolean {
        val title = item.children().firstOrNull { it.tagName() == "title" } ?: return false
        val enclosure = item.children().firstOrNull { it.tagName() == "enclosure" } ?: return false
        val guid = item.children().firstOrNull { it.tagName() == "guid" } ?: return false
        return true
    }


    private fun hasValidRssElement(rssElement: Element): Boolean {
        return rssElement.hasItunesNamespaceDeclaration() && rssElement.hasPodcastNamespaceDeclaration() &&
                rssElement.hasAtomNamespaceDeclaration() && rssElement.attr("version") == "2.0"
    }

    private fun Element.hasItunesNamespaceDeclaration() =
        attr("xmlns:itunes") == "http://www.itunes.com/dtds/podcast-1.0.dtd"

    private fun Element.hasPodcastNamespaceDeclaration() =
        attr("xmlns:podcast") == "https://podcastindex.org/namespace/1.0"

    private fun Element.hasAtomNamespaceDeclaration() =
        attr("xmlns:atom") == "http://www.w3.org/2005/Atom"
}