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
package com.geekorum.ttrss.html

import okhttp3.HttpUrl
import org.jsoup.nodes.Document
import javax.inject.Inject

/**
 * Allows to extract urls of images from an Html Document.
 */
class ImageUrlExtractor @Inject constructor() : HtmlExtractor<HttpUrl>() {

    override fun extract(document: Document): Collection<HttpUrl> {
        val imgs = document.select("img")
        return imgs.asSequence()
            .mapNotNull {
                HttpUrl.parse(it.attr("abs:src") ?: "")
            }
            .toSet()
    }

}