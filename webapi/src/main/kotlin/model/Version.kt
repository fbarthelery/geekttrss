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
package com.geekorum.ttrss.webapi.model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload to get the version of the TtRss server.
 */
@Keep
@Serializable
class GetVersionRequestPayload : LoggedRequestPayload() {

    @SerialName("op")
    override val operation = "getVersion"
}

@Serializable
data class GetVersionResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    @Serializable(with = ContentSerializer::class)
    override val content: BaseContent
) : ResponsePayload<GetVersionResponsePayload.Content>() {

    val version: String?
        get() = typedContent?.version

    @Serializable
    data class Content(
        val version: String? = null,
    ) : BaseContent

    object ContentSerializer : BaseContentSerializer(Content.serializer())
}

/**
 * Request payload to get the api level of the TtRss server.
 */
@Keep
@Serializable
class GetApiLevelRequestPayload : LoggedRequestPayload() {

    @SerialName("op")
    override val operation = "getApiLevel"
}

@Serializable
data class GetApiLevelResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    @Serializable(with = ContentSerializer::class)
    override val content: BaseContent
) : ResponsePayload<GetApiLevelResponsePayload.Content>() {

    val level: Int?
        get() = typedContent?.level

    @Serializable
    data class Content(
        val level: Int? = null,
    ) : BaseContent

    object ContentSerializer : BaseContentSerializer(Content.serializer())

}
