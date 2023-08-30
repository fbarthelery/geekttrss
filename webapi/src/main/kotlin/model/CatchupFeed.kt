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
package com.geekorum.ttrss.webapi.model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request Payload to catch up (mark as read) a feed
 */
@Keep
@Serializable
data class CatchupFeedRequestPayload(
    @SerialName("feed_id")
    private val feedId: Long,
    @SerialName("is_cat")
    private val isCategory: Boolean,
    private val mode: Mode = Mode.ALL
) : LoggedRequestPayload() {

    @SerialName("op")
    override val operation = "catchupFeed"

    enum class Mode {
        @SerialName("all")
        ALL,
        @SerialName("1day")
        ONE_DAY,
        @SerialName("1week")
        ONE_WEEK,
        @SerialName("2week")
        TWO_WEEK
    }
}


@Keep
@Serializable
data class CatchupFeedResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    @Serializable(with = ContentSerializer::class)
    override val content: BaseContent
) : ResponsePayload<CatchupFeedResponsePayload.Content>() {

    @Serializable
    data class Content(
        val status: String? = null,
    ): BaseContent

    object ContentSerializer : BaseContentSerializer(Content.serializer())
}
