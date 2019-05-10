/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2019 by Frederic-Charles Barthelery.
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
