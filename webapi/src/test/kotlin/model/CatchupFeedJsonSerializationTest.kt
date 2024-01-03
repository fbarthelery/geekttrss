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

import com.geekorum.ttrss.webapi.Json
import com.google.common.truth.Truth
import org.junit.Test


class CatchupFeedJsonSerializationTest {
    @Test
    fun testThatCatchupFeedRequestPayloadDoCorrectJson() {
        val payload = CatchupFeedRequestPayload(
            feedId = 43, isCategory = false, mode = CatchupFeedRequestPayload.Mode.ONE_DAY
        ).apply {
            sessionId = "SESSION_ID"
        }

        val serializer = getSerializer<CatchupFeedRequestPayload>()
        val result = Json.encodeToString(serializer, payload)
        Truth.assertThat(result).isEqualTo("""
            {"sid":"SESSION_ID","feed_id":43,"is_cat":false,"mode":"1day","op":"catchupFeed"}
        """.trimIndent())
    }


    @Test
    fun testThatCatchupFeedResponsePayloadDoLoadCorrectly() {
        val jsonString = """
            {"seq":2,"status":1,"content":{"status":"OK"}}
        """.trimIndent()
        val serializer = getSerializer<CatchupFeedResponsePayload>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = CatchupFeedResponsePayload(
            sequence = 2,
            status = 1,
            content = CatchupFeedResponsePayload.Content("OK")
        )
        Truth.assertThat(result.sequence).isEqualTo(expected.sequence)
        Truth.assertThat(result.status).isEqualTo(expected.status)
        Truth.assertThat(result.content).isEqualTo(expected.content)
    }

    @Test
    fun testThatSubscribeToFeedResponsePayloadWithErrorDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 0,
              "status": 1,
              "content": {"error":"NOT_LOGGED_IN"}
            }
        """.trimIndent()
        val serializer = getSerializer<CatchupFeedResponsePayload>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = CatchupFeedResponsePayload(
            sequence = 0,
            status = 1,
            content = ErrorContent(error = Error.NOT_LOGGED_IN)
        )
        Truth.assertThat(result.sequence).isEqualTo(expected.sequence)
        Truth.assertThat(result.status).isEqualTo(expected.status)
        Truth.assertThat(result.content).isEqualTo(expected.content)
    }
}
