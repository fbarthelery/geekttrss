/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2021 by Frederic-Charles Barthelery.
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
package com.geekorum.favikonsnoop

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

data class FaviconInfo(
    val url: HttpUrl,
    val dimension: Dimension? = null,
    val mimeType: String? = null,
    val size: Int? = null
) {
    // to ease usage in tests
    internal constructor(url: String,
                         dimension: Dimension? = null,
                         mimeType: String? = null,
                         size: Int? = null
    ) : this(url.toHttpUrl(), dimension, mimeType, size)
}


sealed class Dimension

data class FixedDimension(val width: Int, val height: Int) : Dimension() {
    init {
        require(width >= 0)
        require(height >= 0)
    }
}

object AdaptiveDimension : Dimension()
