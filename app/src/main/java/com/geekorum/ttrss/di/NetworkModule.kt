/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2020 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.di

import android.app.Application
import android.os.StrictMode.allowThreadDiskWrites
import coil.ImageLoader
import coil.ImageLoaderBuilder
import com.geekorum.geekdroid.network.TaggedSocketFactory
import com.geekorum.ttrss.debugtools.withStrictMode
import com.geekorum.ttrss.logging.RetrofitInvocationLogger
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import javax.inject.Singleton
import javax.net.SocketFactory

private const val DEBUG_REQUEST = false
private const val DEBUG_RETROFIT_CALL = true
private const val TAG_OKHTTP = 1
@Deprecated("Picasso is not used anymore")
private const val TAG_PICASSO = 2
private const val TAG_COIL = 3

@Module
object NetworkModule {

    @Provides
    @Singleton
    internal fun providesOkHttpclient(
        cache: Cache,
        requestLogger: HttpLoggingInterceptor?,
        retrofitInvocationLogger: RetrofitInvocationLogger?
    ): OkHttpClient {
        val socketFactory = TaggedSocketFactory(SocketFactory.getDefault(), TAG_OKHTTP)
        return OkHttpClient.Builder().apply {
            socketFactory(socketFactory)
            cache(cache)

            retrofitInvocationLogger?.let { addInterceptor(it) }
            requestLogger?.let { addInterceptor(it) }
        }.build()
    }

    @Provides
    @Singleton
    internal fun providesHttpRequestLogger(): HttpLoggingInterceptor? {
        return if (DEBUG_REQUEST) {
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        } else null
    }

    @Provides
    @Singleton
    internal fun providesRetrofitInvocationLogger(): RetrofitInvocationLogger? =
        if (DEBUG_RETROFIT_CALL) {
            RetrofitInvocationLogger()
        } else null

    @Provides
    internal fun providesCache(application: Application): Cache {
        return withStrictMode(allowThreadDiskWrites()) {
            // Enable caching for OkHttp
            val cacheSize = 50 * 1024 * 1024 // 50 MiB
            val cacheDir = File(application.cacheDir, "httpCache")
            Cache(cacheDir, cacheSize.toLong())
        }
    }

    @Provides
    @Singleton
    internal fun providesImageLoader(application: Application, okHttpClient: OkHttpClient): ImageLoader {
        return ImageLoaderBuilder(application)
            .okHttpClient {
                val socketFactory = TaggedSocketFactory(okHttpClient.socketFactory, TAG_COIL)
                okHttpClient.newBuilder()
                    .socketFactory(socketFactory)
                    .build()
            }
            .build()
    }
}
