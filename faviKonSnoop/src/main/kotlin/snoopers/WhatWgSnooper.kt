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

import com.geekorum.favikonsnoop.FaviconInfo
import okio.BufferedSource

/**
 * https://html.spec.whatwg.org/#rel-icon
 */
class WhatWgSnooper : LinkRelSnooper("icon") {
    override fun snoop(baseUrl: String, content: BufferedSource): Collection<FaviconInfo> {
        val linksResult = super.snoop(baseUrl, content)
        val faviconLegacyUrl = "$baseUrl/favicon.ico"
        val legacy = FaviconInfo(faviconLegacyUrl)
        return linksResult + legacy
    }
}
