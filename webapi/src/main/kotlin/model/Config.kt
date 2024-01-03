/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Request payload to get the configuration of the TtRss server.
 */
@Keep
@Serializable
class GetConfigRequestPayload : LoggedRequestPayload() {

    @SerialName("op")
    override val operation = "getConfig"
}

@Serializable
data class GetConfigResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    @Serializable(with = ContentSerializer::class)
    override val content: BaseContent
) : ResponsePayload<GetConfigResponsePayload.Content>() {

    val iconsDir: String?
        get() = typedContent?.iconsDir

    val iconsUrl: String?
        get() = typedContent?.iconsUrl

    val numFeeds: Int?
        get() = typedContent?.numFeeds

    val daemonIsRunning: Boolean?
        get() = typedContent?.daemonIsRunning

    @Serializable
    data class Content(
        @SerialName("daemon_is_running")
        val daemonIsRunning: Boolean? = null,
        @SerialName("icons_dir")
        val iconsDir: String? = null,
        @SerialName("icons_url")
        val iconsUrl: String? = null,
        @SerialName("num_feeds")
        val numFeeds: Int? = null,
        @SerialName("custom_sort_types")
        val customSortTypes: List<JsonObject> = emptyList(),
    ) : BaseContent

    object ContentSerializer : BaseContentSerializer(Content.serializer())

}
