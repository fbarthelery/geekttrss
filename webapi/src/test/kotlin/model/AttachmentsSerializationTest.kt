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

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test


class AttachmentsSerializationTest {

    @Test
    fun testThatAttachmentsDoLoadCorrectlyWhenUsingFixerSerializer() {
        val jsonString = """
             [
                {
                  "0" : 313085,
                  "1" : "https://traffic.libsyn.com/rands/theimportantthing0024.mp3",
                  "2" : "audio/mpeg",
                  "3" : "",
                  "4" : "0",
                  "5" : 694227,
                  "6" : 0,
                  "7" : 0,
                  "content_type" : "audio/mpeg",
                  "content_url" : "https://traffic.libsyn.com/rands/theimportantthing0024.mp3",
                  "duration" : "0",
                  "height" : 0,
                  "id" : 313085,
                  "post_id" : 694227,
                  "title" : "",
                  "width" : 0
                },
                {
                   "0" : 189026,
                   "1" : "http://0.gravatar.com/avatar/0c686ee27e835834c44ad17115185e12?s=96&d=identicon&r=PG",
                   "2" : "",
                   "3" : "",
                   "4" : "",
                   "5" : 499753,
                   "6" : 0,
                   "7" : 0,
                   "content_type" : "",
                   "content_url" : "http://0.gravatar.com/avatar/0c686ee27e835834c44ad17115185e12?s=96&d=identicon&r=PG",
                   "duration" : "",
                   "height" : 0,
                   "id" : 189026,
                   "post_id" : 499753,
                   "title" : "",
                   "width" : 0
                }
             ]
        """.trimIndent()
        val serializer = getSerializer<Attachment>()
        val result = Json.decodeFromString(ListSerializer(serializer), jsonString)
        val expected = listOf(
            Attachment(id = 313085,
                indexedId = 313085,
                postId = 694227,
                indexedPostId = 694227,
                contentUrl = "https://traffic.libsyn.com/rands/theimportantthing0024.mp3",
                indexedContentUrl = "https://traffic.libsyn.com/rands/theimportantthing0024.mp3",
                contentType = "audio/mpeg", indexedContentType = "audio/mpeg",
                title = "", indexedTitle = "",
                duration = 0, indexedDuration = 0,
                width = 0, indexedWidth = 0,
                height = 0, indexedHeight = 0
            ),
            Attachment(id = 189026,
                indexedId = 189026,
                postId = 499753,
                indexedPostId = 499753,
                contentUrl = "http://0.gravatar.com/avatar/0c686ee27e835834c44ad17115185e12?s=96&d=identicon&r=PG",
                indexedContentUrl = "http://0.gravatar.com/avatar/0c686ee27e835834c44ad17115185e12?s=96&d=identicon&r=PG",
                contentType = "", indexedContentType = "",
                title = "", indexedTitle = "",
                duration = 0, indexedDuration = 0,
                width = 0, indexedWidth = 0,
                height = 0, indexedHeight = 0
            )
        )
        assertThat(result).isEqualTo(expected)
    }
}
