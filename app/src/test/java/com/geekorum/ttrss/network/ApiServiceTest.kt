package com.geekorum.ttrss.network

import com.geekorum.geekdroid.network.TokenRetriever
import com.geekorum.ttrss.accounts.ServerInformation
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test


/**
 * Test the implementation of [ApiService]
 */
class ApiServiceTest {

    lateinit var subject: ApiService
    lateinit var server: MockWebServer

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        val component = DaggerTestComponent.builder()
            .serverModule(ServerModule(server.url("/").toString()))
            .build()
        subject = component.getApiService()
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    @Test(expected = ApiCallException::class)
    fun testThatHttpErrorThrowsApiCallException() {
        server.enqueue(MockResponse().setResponseCode(500))
        runBlocking {
            subject.getFeeds()
        }
    }
}




@Module
private class ServerModule(val serverUrl: String) {

    @Provides
    fun providesServerInformation(): ServerInformation = object : ServerInformation() {
        override val apiUrl: String = serverUrl
        override val basicHttpAuthUsername: String? = null
        override val basicHttpAuthPassword: String? = null
    }

    @Provides
    fun providesTokenRetriever(): TokenRetriever = object : TokenRetriever {
        override fun getToken(): String = ""

        override fun invalidateToken() {}
    }

    @Provides
    fun providesOkHttpClient(): OkHttpClient = OkHttpClient()
}

@Component(modules = [TinyrssApiModule::class, ServerModule::class])
private interface TestComponent {
    fun getApiService(): ApiService
}
