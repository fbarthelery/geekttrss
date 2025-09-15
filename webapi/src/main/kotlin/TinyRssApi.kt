/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.webapi

import androidx.annotation.Keep
import com.geekorum.ttrss.webapi.model.*
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interface of the Tiny Tiny Rss api.
 *
 * This describes the different endpoints.
 */
@Keep
interface TinyRssApi {

    @POST("api/")
    suspend fun login(@Body loginRequestPayload: LoginRequestPayload): LoginResponsePayload

    @POST("api/")
    suspend fun getVersion(@Body getVersionRequestPayload: GetVersionRequestPayload): GetVersionResponsePayload

    @POST("api/")
    suspend fun getConfig(@Body getConfigRequestPayload: GetConfigRequestPayload): GetConfigResponsePayload

    @POST("api/")
    suspend fun getApiLevel(@Body getApiLevelRequestPayload: GetApiLevelRequestPayload): GetApiLevelResponsePayload

    @POST("api/")
    suspend fun updateArticle(@Body updateFieldRequestPayload: UpdateArticleRequestPayload): UpdateArticleResponsePayload

    @POST("api/")
    suspend fun getFeeds(@Body getFeedsRequestPayload: GetFeedsRequestPayload): ListResponsePayload<Feed>

    @POST("api/")
    suspend fun getCategories(@Body getFeedsRequestPayload: GetCategoriesRequestPayload): ListResponsePayload<FeedCategory>

    @POST("api/")
    suspend fun getArticles(@Body getFeedsRequestPayload: GetArticlesRequestPayload): ListResponsePayload<Headline>

    @POST("api/")
    suspend fun subscribeToFeed(@Body subscribeToFeedRequestPayload: SubscribeToFeedRequestPayload): SubscribeToFeedResponsePayload

    @POST("api/")
    suspend fun unsubscribeFromFeed(@Body unsubscribeFeedRequestPayload: UnsubscribeFeedRequestPayload): UnsubscribeFeedResponsePayload

    @POST("api/")
    suspend fun catchupFeed(@Body catchupFeedRequestPayload: CatchupFeedRequestPayload): CatchupFeedResponsePayload

    @POST("api/")
    suspend fun getFeedIcon(@Body getFeedIconPayload: GetFeedIconPayload): ResponseBody
}
