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

import com.geekorum.ttrss.webapi.Json
import com.geekorum.ttrss.webapi.error
import com.geekorum.ttrss.webapi.model.Error.E_NOT_FOUND
import com.geekorum.ttrss.webapi.model.Error.NOT_LOGGED_IN
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.typeOf
import kotlin.test.Test

class JsonSerializationTest {

    @Test
    fun testThatLoginRequestPayloadDoCorrectJson() {
        val payload = LoginRequestPayload("myuser", "mypassword")
        val serializer = getSerializer<LoginRequestPayload>()
        val result = Json.encodeToString(serializer, payload)
        assertThat(result).isEqualTo("""
            {"user":"myuser","password":"mypassword","op":"login"}
        """.trimIndent())
    }

    @Test
    fun testThatLoginResponsePayloadDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": null,
              "status": 0,
              "content": {
                "session_id": "XXX",
                 "config":{
                    "icons_dir":"feed-icons",
                    "icons_url":"feed-icons",
                    "daemon_is_running":true,
                    "custom_sort_types":[],
                    "num_feeds":33
                 },
                 "api_level": 18
              }
            }
        """.trimIndent()
        val serializer = getSerializer<LoginResponsePayload>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = LoginResponsePayload(
            status = 0,
            content = LoginResponsePayload.Content(
                sessionId = "XXX",
                apiLevel = 18,
                config = GetConfigResponsePayload.Content(
                    daemonIsRunning = true,
                    iconsDir = "feed-icons",
                    iconsUrl = "feed-icons",
                    numFeeds = 33,
                    customSortTypes = emptyList()
                )
            )
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content).isEqualTo(expected.content)
    }

    @Test
    fun testThatLoginResponsePayloadWithErrorDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": null,
              "status": 1,
              "content": {"error":"NOT_LOGGED_IN"}
            }
        """.trimIndent()
        val serializer = getSerializer<LoginResponsePayload>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = LoginResponsePayload(
            status = 1,
            content = ErrorContent(error = NOT_LOGGED_IN)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content).isEqualTo(expected.content)
    }


    @Test
    fun testThatUpdateArticleRequestPayloadDoCorrectJson() {
        val payload = UpdateArticleRequestPayload(
            "23",
            1,
            2 // UNREAD
        ).apply {
            sessionId = "SESSION_ID"
        }
        val serializer = getSerializer<UpdateArticleRequestPayload>()
        val result = Json.encodeToString(serializer, payload)
        assertThat(result).isEqualTo("""
            {"sid":"SESSION_ID","article_ids":"23","mode":1,"field":2,"data":null,"op":"updateArticle"}
        """.trimIndent())
    }

    @Test
    fun testThatUpdateArticleResponsePayloadDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 0,
              "status": 0,
              "content": {"status": "OK", "updated": 1}
            }
        """.trimIndent()
        val serializer = getSerializer<UpdateArticleResponsePayload>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = UpdateArticleResponsePayload(
            sequence = 0,
            status = 0,
            content = UpdateArticleResponsePayload.Content("OK", 1)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content).isEqualTo(expected.content)
    }

    @Test
    fun testThatUpdateArticleResponsePayloadWithErrorDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 0,
              "status": 1,
              "content": {"error":"NOT_LOGGED_IN"}
            }
        """.trimIndent()
        val serializer = getSerializer<UpdateArticleResponsePayload>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = UpdateArticleResponsePayload(
            sequence = 0,
            status = 1,
            content = ErrorContent(error = NOT_LOGGED_IN)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content).isEqualTo(expected.content)
    }


    @Test
    fun testThatGetFeedsRequestPayloadDoCorrectJson() {
        val payload = GetFeedsRequestPayload(
            includeNested = true,
            unreadOnly = false,
            categorieId = GetFeedsRequestPayload.CATEGORY_ID_ALL_EXCLUDE_VIRTUALS).apply {
            sessionId = "SESSION_ID"
        }
        val serializer = getSerializer<GetFeedsRequestPayload>()
        val result = Json.encodeToString(serializer, payload)
        assertThat(result).isEqualTo("""
            {"sid":"SESSION_ID","include_nested":true,"unread_only":false,"cat_id":-3,"op":"getFeeds"}
        """.trimIndent())
    }

    @Test
    fun testThatGetFeedsResponsePayloadDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 2,
              "status": 1,
              "content": [
                {
                  "feed_url": "https:\/\/medium.com\/feed\/google-developers",
                  "title": "Google Developers \u2014 Medium",
                  "id": 256,
                  "unread": 0,
                  "has_icon": true,
                  "cat_id": 2,
                  "last_updated": 1541010158,
                  "order_id": 4
                },
                {
                  "feed_url": "https:\/\/blog.mozilla.org\/beyond-the-code\/feed\/",
                  "title": "Beyond the Code",
                  "id": 192,
                  "unread": null,
                  "has_icon": true,
                  "cat_id": 3,
                  "last_updated": 1541010042,
                  "order_id": 2
                },
                {
                  "feed_url": "https:\/\/www.genymotion.com\/blog\/feed\/",
                  "title": "Blog \u2013 Genymotion \u2013 Android Emulator for app testing",
                  "id": 272,
                  "unread": 0,
                  "has_icon": true,
                  "cat_id": 26,
                  "last_updated": 1541010322,
                  "order_id": 0
                }
              ]
            }
        """.trimIndent()
        val serializer = getSerializer<ListResponsePayload<Feed>>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = ListResponsePayload<Feed>(
            sequence = 2,
            status = 1,
            content = ListContent(listOf(
                Feed(id = 256,
                    title = "Google Developers \u2014 Medium",
                    url = "https://medium.com/feed/google-developers",
                    nbUnreadArticles = 0,
                    hasIcon = true,
                    categoryId = 2,
                    lastUpdatedTimestamp = 1541010158,
                    orderId = 4),
                Feed(id = 192,
                    title = "Beyond the Code",
                    url = "https://blog.mozilla.org/beyond-the-code/feed/",
                    nbUnreadArticles = 0,
                    hasIcon = true,
                    categoryId = 3,
                    lastUpdatedTimestamp = 1541010042,
                    orderId = 2),
                Feed(id = 272,
                    title = "Blog \u2013 Genymotion \u2013 Android Emulator for app testing",
                    url = "https://www.genymotion.com/blog/feed/",
                    nbUnreadArticles = 0,
                    hasIcon = true,
                    categoryId = 26,
                    lastUpdatedTimestamp = 1541010322,
                    orderId = 0)
            ))
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.result).isEqualTo(expected.result)
    }

    @Test
    fun testThatGetFeedsResponsePayloadWithErrorDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 2,
              "status": 1,
              "content": {"error":"NOT_LOGGED_IN"}
            }
        """.trimIndent()
        val serializer = getSerializer<ListResponsePayload<Feed>>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = ListResponsePayload<Feed>(
            sequence = 2,
            status = 1,
            content = ErrorContent(error = NOT_LOGGED_IN)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.error).isEqualTo(expected.error)
    }


    @Test
    fun testThatGetCategoriesRequestPayloadDoCorrectJson() {
        val payload = GetCategoriesRequestPayload(
            includeNested = true,
            unreadOnly = false
        ).apply {
            sessionId = "SESSION_ID"
        }
        val serializer = getSerializer<GetCategoriesRequestPayload>()
        val result = Json.encodeToString(serializer, payload)
        assertThat(result).isEqualTo("""
            {"sid":"SESSION_ID","include_nested":true,"unread_only":false,"op":"getCategories"}
        """.trimIndent())
    }

    @Test
    fun testThatGetCategoriesResponsePayloadDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 2,
              "status": 1,
              "content": [
                {
                  "id": "39",
                  "title": "microservices",
                  "unread": 0,
                  "order_id": 0
                },
                {
                  "id": "2",
                  "title": "Android",
                  "unread": 12,
                  "order_id": 1
                }
              ]
            }
        """.trimIndent()
        val serializer = getSerializer<ListResponsePayload<FeedCategory>>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = ListResponsePayload<FeedCategory>(
            sequence = 2,
            status = 1,
            content = ListContent(listOf(
                FeedCategory(id = 39, title = "microservices", nbUnreadArticles = 0, orderId = 0),
                FeedCategory(id = 2, title = "Android", nbUnreadArticles = 12, orderId = 1)
            ))
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.result).isEqualTo(expected.result)
    }


    @Test
    fun testThatGetCategoriesResponsePayloadWithErrorDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 2,
              "status": 1,
              "content": {"error":"NOT_LOGGED_IN"}
            }
        """.trimIndent()
        val serializer = getSerializer<ListResponsePayload<FeedCategory>>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = ListResponsePayload<FeedCategory>(
            sequence = 2,
            status = 1,
            content = ErrorContent(error = NOT_LOGGED_IN)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.error).isEqualTo(expected.error)
    }

    @Test
    fun testThatErrorResponsePayloaoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 0,
              "status": 1,
              "content": {"error":"E_NOT_FOUND"}
            }
        """.trimIndent()
        val serializer = getSerializer<ErrorResponsePayload>()
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = ErrorResponsePayload(
            sequence = 0,
            status = 1,
            content = ErrorContent(error = E_NOT_FOUND)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content.error).isEqualTo(expected.content.error)
    }

}

internal inline fun <reified T> getSerializer(): KSerializer<T> {
    val typeToken = typeOf<T>()
    // we use type token because that's what RetrofitConverter use to get the serializer
    @Suppress("UNCHECKED_CAST")
    return serializer(typeToken) as KSerializer<T>

}
