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

import com.geekorum.ttrss.webapi.Json
import com.google.common.truth.Truth.assertThat
import kotlin.test.Test

class GetVersionJsonSerializationTest {

    @Test
    fun testThatGetVersionRequestPayloadDoCorrectJson() {
        val payload = GetVersionRequestPayload().apply {
            sessionId = "SESSION_ID"
        }
        val serializer = getSerializer<GetVersionRequestPayload>()
        val result = Json.encodeToString(serializer, payload)
        assertThat(result).isEqualTo("""
            {"sid":"SESSION_ID","op":"getVersion"}
        """.trimIndent())
    }

    @Test
    fun testThatGetVersionResponsePayloadDoLoadCorrectly() {
        val jsonString = """
            {"seq":0,"status":0,"content":{"version":"19.8"}}
        """.trimIndent()
        val serializer = getSerializer<GetVersionResponsePayload>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = GetVersionResponsePayload(
            sequence = 0,
            status = 0,
            content = GetVersionResponsePayload.Content(version = "19.8")
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content).isEqualTo(expected.content)
    }

    @Test
    fun testThatGetVersionResponsePayloadWithErrorDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 2,
              "status": 1,
              "content": {"error":"NOT_LOGGED_IN"}
            }
        """.trimIndent()
        val serializer = getSerializer<ListResponsePayload<Headline>>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = GetVersionResponsePayload(
            sequence = 2,
            status = 1,
            content = GetVersionResponsePayload.Content(error = Error.NOT_LOGGED_IN)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content.error).isEqualTo(expected.content.error)
    }
}


class GetApiLevelJsonSerializationTest {

    @Test
    fun testThatGetApiLevelRequestPayloadDoCorrectJson() {
        val payload = GetApiLevelRequestPayload().apply {
            sessionId = "SESSION_ID"
        }
        val serializer = getSerializer<GetApiLevelRequestPayload>()
        val result = Json.encodeToString(serializer, payload)
        assertThat(result).isEqualTo("""
            {"sid":"SESSION_ID","op":"getApiLevel"}
        """.trimIndent())
    }

    @Test
    fun testThatGetApiLevelResponsePayloadDoLoadCorrectly() {
        val jsonString = """
            {"seq":0,"status":0,"content":{"level":14}}
        """.trimIndent()
        val serializer = getSerializer<GetApiLevelResponsePayload>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = GetApiLevelResponsePayload(
            sequence = 0,
            status = 0,
            content = GetApiLevelResponsePayload.Content(level = 14)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content).isEqualTo(expected.content)
    }

    @Test
    fun testThatGetApiLevelResponsePayloadWithErrorDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 2,
              "status": 1,
              "content": {"error":"NOT_LOGGED_IN"}
            }
        """.trimIndent()
        val serializer = getSerializer<GetApiLevelResponsePayload>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = GetApiLevelResponsePayload(
            sequence = 2,
            status = 1,
            content = GetApiLevelResponsePayload.Content(error = Error.NOT_LOGGED_IN)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content.error).isEqualTo(expected.content.error)
    }
}
