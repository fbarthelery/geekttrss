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
import kotlinx.serialization.Transient

/**
 * Request payload to log the user in.
 */
@Keep
@Serializable
data class LoginRequestPayload(
    val user: String,
    val password: String
) : BaseRequestPayload() {

    @SerialName("op")
    override val operation = "login"
}


/**
 * The response of a Login request.
 */
@Keep
@Serializable
data class LoginResponsePayload(
    @SerialName("seq")
    override val sequence: Int? = null,
    override val status: Int = 0,
    @Serializable(with = ContentSerializer::class)
    override val content: BaseContent
) : ResponsePayload<LoginResponsePayload.Content>() {

    val sessionId: String?
        get() = typedContent?.sessionId

    val apiLevel: Int?
        get() = typedContent?.apiLevel

    @Serializable
    data class Content(
        @SerialName("session_id")
        val sessionId: String? = null,

        @SerialName("api_level")
        val apiLevel: Int? = null,

        val config: GetConfigResponsePayload.Content? = null,

    ) : BaseContent

    object ContentSerializer : BaseContentSerializer(Content.serializer())

}
