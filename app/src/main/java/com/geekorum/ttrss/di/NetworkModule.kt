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
package com.geekorum.ttrss.di

import android.app.Application
import com.geekorum.geekdroid.dagger.AppInitializer
import com.geekorum.geekdroid.dagger.AppInitializersModule
import com.geekorum.geekdroid.network.PicassoOkHttp3Downloader
import com.geekorum.geekdroid.network.TaggedSocketFactory
import com.geekorum.ttrss.logging.RetrofitInvocationLogger
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import java.io.File

import javax.inject.Inject
import javax.inject.Singleton
import javax.net.SocketFactory

private const val DEBUG_REQUEST = false
private const val DEBUG_RETROFIT_CALL = true
private const val TAG_OKHTTP = 1
private const val TAG_PICASSO = 2

@Module(includes = [AppInitializersModule::class])
class NetworkModule {

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
        // Enable caching for OkHttp
        val cacheSize = 50 * 1024 * 1024 // 50 MiB
        val cacheDir = File(application.cacheDir, "httpCache")
        return Cache(cacheDir, cacheSize.toLong())
    }

    @Provides
    @Singleton
    internal fun providesPicasso(application: Application, okHttpClient: OkHttpClient): Picasso {
        val socketFactory = TaggedSocketFactory(okHttpClient.socketFactory, TAG_PICASSO)
        val okHttpBuilder = okHttpClient.newBuilder()
            .socketFactory(socketFactory)

        return Picasso.Builder(application)
            .downloader(PicassoOkHttp3Downloader(okHttpBuilder.build()))
            .indicatorsEnabled(DEBUG_REQUEST)
            .build()
    }

    @Provides
    @IntoSet
    internal fun providesPicassoInitializer(picasso: Picasso): AppInitializer {
        return PicassoInitializer(picasso)
    }

}

private class PicassoInitializer @Inject constructor(
    private val picasso: Picasso
) : AppInitializer {

    override fun initialize(application: Application) {
        Picasso.setSingletonInstance(picasso)
    }
}
