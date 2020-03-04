/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2020 by Frederic-Charles Barthelery.
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

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlin.test.Test

@OptIn(UnstableDefault::class)
class GetConfigJsonSerializationTest {

    @Test
    fun testThatGetConfigRequestPayloadDoCorrectJson() {
        val payload = GetConfigRequestPayload().apply {
            sessionId = "SESSION_ID"
        }
        val serializer = getSerializer<GetConfigRequestPayload>()
        val result = Json.stringify(serializer, payload)
        assertThat(result).isEqualTo("""
            {"sid":"SESSION_ID","op":"getConfig"}
        """.trimIndent())
    }

    @Test
    fun testThatGetConfigResponsePayloadDoLoadCorrectly() {
        val jsonString = """
            {
                "content" : {
                    "daemon_is_running" : true,
                    "icons_dir" : "feed-icons",
                    "icons_url" : "feed-icons",
                    "num_feeds" : 65
                },
                "seq" : 0,
                "status" : 0
            }
        """.trimIndent()
        val serializer = getSerializer<GetConfigResponsePayload>()
        val result = Json.parse(serializer, jsonString)
        val expected = GetConfigResponsePayload(
            sequence = 0,
            status = 0,
            content = GetConfigResponsePayload.Content(daemonIsRunning = true,
                iconsDir = "feed-icons",
                iconsUrl = "feed-icons",
                numFeeds = 65)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content).isEqualTo(expected.content)
    }

    @Test
    fun testThatGetConfigResponsePayloadWithErrorDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 2,
              "status": 1,
              "content": {"error":"NOT_LOGGED_IN"}
            }
        """.trimIndent()
        val serializer = getSerializer<ListResponsePayload<Headline>>()
        val result = Json.parse(serializer, jsonString)
        val expected = GetConfigResponsePayload(
            sequence = 2,
            status = 1,
            content = GetConfigResponsePayload.Content(error = Error.NOT_LOGGED_IN)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content.error).isEqualTo(expected.content.error)
    }
}
