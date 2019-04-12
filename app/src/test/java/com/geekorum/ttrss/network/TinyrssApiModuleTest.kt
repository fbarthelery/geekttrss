package com.geekorum.ttrss.network

import com.geekorum.ttrss.accounts.ServerInformation
import com.geekorum.ttrss.network.TinyrssApiModule
import com.geekorum.ttrss.network.impl.LoginRequestPayload
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
                tinyRssApi.login(requestPayload).await()
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
            tinyRssApi.login(requestPayload).await()
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

        val loginResponsePayload = runBlocking {
            val requestPayload = LoginRequestPayload("user", "password")
            tinyRssApi.login(requestPayload).await()
        }

        val request = server.takeRequest()
        assertThat(request.getHeader("Authorization")).isNull()

    }

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
            tinyRssApi.login(requestPayload).await()
        }

        assertThat(loginResponsePayload.sessionId).isEqualTo("XXX")
    }


}
