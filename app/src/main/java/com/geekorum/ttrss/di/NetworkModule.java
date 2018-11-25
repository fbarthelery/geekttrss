/**
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2018 by Frederic-Charles Barthelery.
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
import com.geekorum.geekdroid.network.PicassoOkHttp3Downloader;
import com.squareup.picasso.Picasso;
import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;

import javax.inject.Singleton;

@Module
public class NetworkModule {
    private static final boolean DEBUG_REQUEST = false;

    @Provides
    @Singleton
    static OkHttpClient providesOkHttpclient(Cache cache) {
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        if (DEBUG_REQUEST) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpBuilder.addInterceptor(logging);
        }
        okHttpBuilder.cache(cache);
        return okHttpBuilder.build();
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
        Picasso picasso = new Picasso.Builder(application)
                .downloader(new PicassoOkHttp3Downloader(okHttpClient))
                .indicatorsEnabled(DEBUG_REQUEST)
                .build();
        return picasso;
    }

}
