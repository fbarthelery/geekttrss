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
package com.geekorum.ttrss.webapi

import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class BasicAuthInterceptorTest {

    lateinit var subject: BasicAuthInterceptor
    lateinit var server: MockWebServer

    @BeforeTest
    fun setUp() {
        subject = BasicAuthInterceptor("user", "password")
        server = MockWebServer()
        server.start()
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun testThatAuthorizationHeaderIsSent() {
        server.enqueue(MockResponse().setResponseCode(200))
        val client = OkHttpClient.Builder()
            .addInterceptor(subject)
            .build()
        val request = Request.Builder()
            .url(server.url(""))
            .build()
        client.newCall(request).execute()
        val receivedRequest = server.takeRequest()
        assertThat(receivedRequest.getHeader("Authorization"))
            .isEqualTo("Basic dXNlcjpwYXNzd29yZA==")
    }

    @Test
    fun testThatAuthorizationHeaderIsNotSentWhenNoInterceptor() {
        server.enqueue(MockResponse().setResponseCode(200))
        val client = OkHttpClient.Builder()
            .build()
        val request = Request.Builder()
            .url(server.url(""))
            .build()
        client.newCall(request).execute()
        val receivedRequest = server.takeRequest()
        assertThat(receivedRequest.getHeader("Authorization")).isNull()
    }

}