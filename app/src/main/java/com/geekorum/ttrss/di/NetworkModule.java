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
package com.geekorum.ttrss.di;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.geekorum.geekdroid.dagger.AppInitializer;
import com.geekorum.geekdroid.dagger.AppInitializersModule;
import com.geekorum.geekdroid.network.PicassoOkHttp3Downloader;
import com.geekorum.geekdroid.network.TaggedSocketFactory;
import com.geekorum.ttrss.logging.RetrofitInvocationLogger;
import com.squareup.picasso.Picasso;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.SocketFactory;

@Module(includes = {AppInitializersModule.class})
public class NetworkModule {
    private static final boolean DEBUG_REQUEST = false;
    private static final boolean DEBUG_RETROFIT_CALL = true;
    private static final int TAG_OKHTTP = 1;
    private static final int TAG_PICASSO = 2;


    @Provides
    @Singleton
    static OkHttpClient providesOkHttpclient(Cache cache,
                                             @Nullable HttpLoggingInterceptor requestLogger,
                                             @Nullable RetrofitInvocationLogger retrofitInvocationLogger) {
        SocketFactory socketFactory = new TaggedSocketFactory(SocketFactory.getDefault(), TAG_OKHTTP);
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                .socketFactory(socketFactory);
        if (retrofitInvocationLogger != null) {
            okHttpBuilder.addInterceptor(retrofitInvocationLogger);
        }
        if (requestLogger != null) {
            okHttpBuilder.addInterceptor(requestLogger);
        }
        okHttpBuilder.cache(cache);
        return okHttpBuilder.build();
    }

    @Provides
    @Singleton
    static @Nullable HttpLoggingInterceptor providesHttpRequestLogger() {
        if (DEBUG_REQUEST) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.level(HttpLoggingInterceptor.Level.BODY);
            return logging;
        }
        return null;
    }

    @Provides
    @Singleton
    static @Nullable RetrofitInvocationLogger providesRetrofitInvocationLogger() {
        if (DEBUG_RETROFIT_CALL) {
            return new RetrofitInvocationLogger();
        }
        return null;
    }

    @Provides
    static Cache providesCache(Application application) {
        // Enable caching for OkHttp
        int cacheSize = 50 * 1024 * 1024; // 50 MiB
        File cacheDir = new File(application.getCacheDir(), "httpCache");
        return new Cache(cacheDir, cacheSize);
    }

    @Provides
    @Singleton
    static Picasso providesPicasso(Application application, OkHttpClient okHttpClient) {
        SocketFactory socketFactory = new TaggedSocketFactory(okHttpClient.socketFactory(), TAG_PICASSO);
        OkHttpClient.Builder okHttpBuilder = okHttpClient.newBuilder()
                .socketFactory(socketFactory);

        Picasso picasso = new Picasso.Builder(application)
                .downloader(new PicassoOkHttp3Downloader(okHttpBuilder.build()))
                .indicatorsEnabled(DEBUG_REQUEST)
                .build();
        return picasso;
    }

    @Provides
    @IntoSet
    static AppInitializer providesPicassoInitializer(Picasso picasso) {
        return new PicassoInitializer(picasso);
    }

}

class PicassoInitializer implements AppInitializer {
    private final Picasso picasso;

    @Inject
    PicassoInitializer(Picasso picasso) {
        this.picasso = picasso;
    }

    @Override
    public void initialize(@NonNull Application application) {
        Picasso.setSingletonInstance(picasso);

    }
}
