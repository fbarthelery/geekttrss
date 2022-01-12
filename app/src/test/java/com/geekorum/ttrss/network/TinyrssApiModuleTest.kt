/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.network

import com.geekorum.ttrss.accounts.ServerInformation
import com.geekorum.ttrss.network.TinyrssApiModule
import com.geekorum.ttrss.webapi.model.LoginRequestPayload
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.HttpException
import java.util.Optional
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class TinyrssApiModuleTest {

    private data class DataServerInformation(
        override val apiUrl: String,
        override val basicHttpAuthUsername: String? = null,
        override val basicHttpAuthPassword: String? = null
    ) : ServerInformation()

    lateinit var server: MockWebServer
    lateinit var okHttpClient: OkHttpClient
    private lateinit var nonAuthServerInformation: ServerInformation
    private lateinit var authServerInformation: ServerInformation

    @BeforeTest
    fun setUp() {
        // workaround ssl error on jdk 11
        // https://github.com/robolectric/robolectric/issues/5115
        System.setProperty("javax.net.ssl.trustStoreType", "JKS")
        okHttpClient = OkHttpClient.Builder().build()
        server = MockWebServer()
        server.start()
        nonAuthServerInformation = DataServerInformation("http://localhost:${server.port}/")
        authServerInformation = DataServerInformation("http://localhost:${server.port}/", "username", "password")
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun testThatProtectedResourcesWithoutHttpAuthenticationInfoReturnsError() {
        server.enqueue(MockResponse().setResponseCode(401))
        val tinyRssApi = TinyrssApiModule.providesTinyRssApi(okHttpClient, nonAuthServerInformation, Optional.empty())

        try {
            runBlocking {
                val requestPayload = LoginRequestPayload("user", "password")
                tinyRssApi.login(requestPayload)
            }
        } catch (e: HttpException) {
            assertThat(e.code()).isEqualTo(401)
        }
    }

    @Test
    fun testThatProtectedResourcesWithHttpAuthenticationInfoSendAuthorizationHeader() {
        server.enqueue(MockResponse().setResponseCode(401))
        val mockResponse = MockResponse().apply {
            setBody("""
                    {
                      "seq": null,
                      "status": 0,
                      "content": {"session_id": "XXX", "api_level": 3}
                    }
                    """.trimIndent())
        }
        server.enqueue(mockResponse)

        val tinyRssApi = TinyrssApiModule.providesTinyRssApi(okHttpClient, authServerInformation, Optional.empty())

        val loginResponsePayload = runBlocking {
            val requestPayload = LoginRequestPayload("user", "password")
            tinyRssApi.login(requestPayload)
        }

        val unauthenticatedRequest = server.takeRequest()
        assertThat(unauthenticatedRequest.getHeader("Authorization")).isNull()

        val authenticatedRequest = server.takeRequest()
        assertThat(authenticatedRequest.getHeader("Authorization")).isNotEmpty()
        assertThat(loginResponsePayload.sessionId).isEqualTo("XXX")

    }

    @Test
    fun testThatNonProtectedResourcesWithHttpAuthenticationInfoDontSendAuthorizationHeader(): Unit {
        val mockResponse = MockResponse().apply {
            setBody("""
                    {
                      "seq": null,
                      "status": 0,
                      "content": {"session_id": "XXX", "api_level": 3}
                    }
                    """.trimIndent())
        }
        server.enqueue(mockResponse)

        val tinyRssApi = TinyrssApiModule.providesTinyRssApi(okHttpClient, authServerInformation, Optional.empty())

        runBlocking {
            val requestPayload = LoginRequestPayload("user", "password")
            tinyRssApi.login(requestPayload)
        }

        val request = server.takeRequest()
        assertThat(request.getHeader("Authorization")).isNull()

    }

    @Test
    fun testThatNonProtectedResourcesWithoutHttpAuthenticationInfoReturnsResult(): Unit {
        val mockResponse = MockResponse().apply {
            setBody("""
                    {
                      "seq": null,
                      "status": 0,
                      "content": {"session_id": "XXX", "api_level": 3}
                    }
                    """.trimIndent())
        }
        server.enqueue(mockResponse)

        val tinyRssApi = TinyrssApiModule.providesTinyRssApi(okHttpClient, nonAuthServerInformation, Optional.empty())

        val loginResponsePayload = runBlocking {
            val requestPayload = LoginRequestPayload("user", "password")
            tinyRssApi.login(requestPayload)
        }

        assertThat(loginResponsePayload.sessionId).isEqualTo("XXX")
    }


}
