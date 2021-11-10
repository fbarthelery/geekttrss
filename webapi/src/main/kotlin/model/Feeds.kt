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
package com.geekorum.ttrss.webapi.model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The payload gor a getCategories request.
 *
 * It allows to retrieve categories from the Tiny Tiny Rss server.
 */
@Keep
@Serializable
data class GetCategoriesRequestPayload(

    @SerialName("include_nested")
    val includeNested: Boolean,

    @SerialName("unread_only")
    val unreadOnly: Boolean

) : LoggedRequestPayload() {

    @SerialName("op")
    override val operation = "getCategories"
}


/**
 * The payload gor a getFeeds request.
 *
 * It allows to retrieve feeds from the Tiny Tiny Rss server.
 */
@Keep
@Serializable
data class GetFeedsRequestPayload(
    @SerialName("include_nested")
    val includeNested: Boolean,

    @SerialName("unread_only")
    val unreadOnly: Boolean,

    @SerialName("cat_id")
    val categorieId: Int
) : LoggedRequestPayload() {

    @SerialName("op")
    override val operation = "getFeeds"

    companion object {

        val CATEGORY_ID_UNCATEGORIZED = 0
        val CATEGORY_ID_SPECIALS = -1
        val CATEGORY_ID_LABELS = -2
        val CATEGORY_ID_ALL_EXCLUDE_VIRTUALS = -3
        val CATEGORY_ID_ALL_INCLUDE_VIRTUALS = -4
    }
}
