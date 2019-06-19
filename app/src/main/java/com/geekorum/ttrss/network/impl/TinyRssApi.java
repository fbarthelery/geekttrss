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
package com.geekorum.ttrss.network.impl;

import androidx.annotation.Keep;
import kotlinx.coroutines.Deferred;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Interface of the Tiny Tiny Rss api.
 *
 * This describes the different endpoints.
 */
@Keep
public interface TinyRssApi {

    @POST("api/")
    Deferred<LoginResponsePayload> login(@Body LoginRequestPayload loginRequestPayload);

    @POST("api/")
    Deferred<UpdateArticleResponsePayload> updateArticle(@Body UpdateArticleRequestPayload updateFieldRequestPayload);

    @POST("api/")
    Deferred<ListResponsePayload<Feed>> getFeeds(@Body GetFeedsRequestPayload getFeedsRequestPayload);

    @POST("api/")
    Deferred<ListResponsePayload<FeedCategory>> getCategories(@Body GetCategoriesRequestPayload getFeedsRequestPayload);


    @POST("api/")
    Deferred<ListResponsePayload<Headline>> getArticles(@Body GetArticlesRequestPayload getFeedsRequestPayload);

    @POST("api/")
    Deferred<SubscribeToFeedResponsePayload> subscribeToFeed(@Body SubscribeToFeedRequestPayload subscribeToFeedRequestPayload);

    @POST("api/")
    Deferred<UnsubscribeFeedResponsePayload> unsubscribeFromFeed(@Body UnsubscribeFeedRequestPayload unsubscribeFeedRequestPayload);

}
