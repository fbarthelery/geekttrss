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
package com.geekorum.ttrss.network;

import com.geekorum.geekdroid.network.TokenRetriever;
import com.geekorum.ttrss.accounts.ServerInformation;
import com.geekorum.ttrss.webapi.LoggedRequestInterceptorFactory;
import com.geekorum.ttrss.webapi.TinyRssApi;
import com.geekorum.ttrss.webapi.BasicAuthAuthenticator;
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory;
import com.jakewharton.retrofit2.converter.kotlinx.serialization.KotlinSerializationConverterFactory;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import kotlinx.serialization.json.Json;
import okhttp3.Authenticator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import java.util.Objects;
import java.util.Optional;

/**
 * This module provides an {@link ApiService} to access a TinyRss server
 */
@Module
public abstract class TinyrssApiModule {

    @BindsOptionalOf
    abstract LoggedRequestInterceptorFactory bindLoggedRequestInterceptorFactory();

    @Provides
    static ApiService provideApiService(TokenRetriever tokenRetriever, TinyRssApi tinyRssApi) {
        return new ApiRetrofitService(tokenRetriever, tinyRssApi);
    }

    @Provides
    static TinyRssApi providesTinyRssApi(OkHttpClient okHttpClient, ServerInformation serverInformation, Optional<LoggedRequestInterceptorFactory> loggedRequestInterceptorFactory) {
        String tinyrssApiUrl = serverInformation.getApiUrl();
        if (!tinyrssApiUrl.endsWith("/")) {
            tinyrssApiUrl += "/";
        }
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(tinyrssApiUrl);

        loggedRequestInterceptorFactory.ifPresent(retrofitBuilder::addConverterFactory);

        String basicHttpAuthPassword = serverInformation.getBasicHttpAuthPassword();
        String basicHttpAuthUsername = serverInformation.getBasicHttpAuthUsername();
        if (basicHttpAuthPassword != null || basicHttpAuthUsername != null) {
            Authenticator serverAuthenticator = new BasicAuthAuthenticator(basicHttpAuthUsername, basicHttpAuthPassword);
            okHttpClient = okHttpClient.newBuilder().authenticator(serverAuthenticator).build();
        }

        Json.Companion json = Json.Companion;
        retrofitBuilder.addConverterFactory(KotlinSerializationConverterFactory.create(
                json,
                Objects.requireNonNull(MediaType.parse("application/json"))
        ))
                .addCallAdapterFactory(CoroutineCallAdapterFactory.create())
                .client(okHttpClient);

        Retrofit retrofit = retrofitBuilder.build();
        return retrofit.create(TinyRssApi.class);
    }

}
