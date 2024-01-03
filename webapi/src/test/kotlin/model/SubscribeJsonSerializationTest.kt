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
import com.geekorum.ttrss.webapi.model.UnsubscribeFeedResponsePayload.Content
import com.google.common.truth.Truth
import org.junit.Test

class SubscribeToFeedJsonSerializationTest {
    @Test
    fun testThatSubscribeToFeedRequestPayloadDoCorrectJson() {
        val payload = SubscribeToFeedRequestPayload(
            "http://my.feed.url/feed", 2,
            "user", "password"
        ).apply {
            sessionId = "SESSION_ID"
        }

        val serializer = getSerializer<SubscribeToFeedRequestPayload>()
        val result = Json.encodeToString(serializer, payload)
        Truth.assertThat(result).isEqualTo("""
            {"sid":"SESSION_ID","feed_url":"http://my.feed.url/feed","category_id":2,"login":"user","password":"password","op":"subscribeToFeed"}
        """.trimIndent())
    }


    @Test
    fun testThatSubscribeToFeedResponsePayloadDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 2,
              "status": 1,
              "content": {
                "status": {
                  "code": 1,
                  "message": "Feed successfully added",
                  "feed_id": 42
                }
              }
            }
        """.trimIndent()
        val serializer = getSerializer<SubscribeToFeedResponsePayload>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = SubscribeToFeedResponsePayload(
            sequence = 2,
            status = 1,
            content = SubscribeToFeedResponsePayload.Content(
                SubscribeToFeedResponsePayload.Content.Status(1, "Feed successfully added", 42))
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
        val serializer = getSerializer<SubscribeToFeedResponsePayload>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = SubscribeToFeedResponsePayload(
            sequence = 0,
            status = 1,
            content = ErrorContent(error = Error.NOT_LOGGED_IN)
        )
        Truth.assertThat(result.sequence).isEqualTo(expected.sequence)
        Truth.assertThat(result.status).isEqualTo(expected.status)
        Truth.assertThat(result.content).isEqualTo(expected.content)
    }
}


class UnsubscribeFromFeedJsonSerializationTest {
    @Test
    fun testThatUnsubscribeFeedRequestPayloadDoCorrectJson() {
        val payload = UnsubscribeFeedRequestPayload(
            42
        ).apply {
            sessionId = "SESSION_ID"
        }

        val serializer = getSerializer<UnsubscribeFeedRequestPayload>()
        val result = Json.encodeToString(serializer, payload)
        Truth.assertThat(result).isEqualTo("""
            {"sid":"SESSION_ID","feed_id":42,"op":"unsubscribeFeed"}
        """.trimIndent())
    }


    @Test
    fun testThatUnsubscribeFeedResponsePayloadDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 2,
              "status": 1,
              "content": {
                "status": "OK"
              }
            }
        """.trimIndent()
        val serializer = getSerializer<UnsubscribeFeedResponsePayload>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = UnsubscribeFeedResponsePayload(
            sequence = 2,
            status = 1,
            content = Content(Content.Status.OK)
        )
        Truth.assertThat(result.sequence).isEqualTo(expected.sequence)
        Truth.assertThat(result.status).isEqualTo(expected.status)
        Truth.assertThat(result.content).isEqualTo(expected.content)
    }

    @Test
    fun testThatUnsubscribeFeedResponsePayloadWithErrorDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 0,
              "status": 1,
              "content": {"error":"FEED_NOT_FOUND"}
            }
        """.trimIndent()
        val serializer = getSerializer<UnsubscribeFeedResponsePayload>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = UnsubscribeFeedResponsePayload(
            sequence = 0,
            status = 1,
            content = ErrorContent(error = Error.FEED_NOT_FOUND)
        )
        Truth.assertThat(result.sequence).isEqualTo(expected.sequence)
        Truth.assertThat(result.status).isEqualTo(expected.status)
        Truth.assertThat(result.content).isEqualTo(expected.content)
    }


}

