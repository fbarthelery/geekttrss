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
package com.geekorum.ttrss.sync

import androidx.annotation.WorkerThread
import okhttp3.CacheControl
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Retrieve data from an url and save it in cache
 */
class HttpCacher @Inject constructor(httpClient: OkHttpClient) {

    /** Dangerous interceptor that rewrites the server's cache-control header.  */
    private object RewriteCacheControlInterceptor : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val originalResponse = chain.proceed(chain.request())
            val cacheControl = CacheControl.Builder()
                .maxAge(90, TimeUnit.DAYS)
                .build()
            return originalResponse.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .build()
        }
    }

    private val okHttpClient: OkHttpClient = httpClient.newBuilder()
        .addNetworkInterceptor(RewriteCacheControlInterceptor)
        .build()

    @Throws(IOException::class)
    fun cacheHttpRequest(url: String) = cacheHttpRequest(url.toHttpUrl())

    @Throws(IOException::class)
    @WorkerThread
    fun cacheHttpRequest(url: HttpUrl) {
        val request = Request.Builder()
            .get()
            .url(url)
            .build()
        okHttpClient.newCall(request).execute().close()
    }
}

