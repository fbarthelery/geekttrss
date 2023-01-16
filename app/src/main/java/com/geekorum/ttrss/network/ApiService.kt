/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2023 by Frederic-Charles Barthelery.
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

import com.geekorum.ttrss.data.ArticleWithAttachments
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.webapi.ApiCallException

/**
 * Interface needed to communicate with the Tiny Tiny Rss server.
 */
interface ApiService {

    @Throws(ApiCallException::class)
    suspend fun getArticles(
        feedId: Long, sinceId: Long, offset: Int,
        showExcerpt: Boolean, showContent: Boolean,
        includeAttachments: Boolean
    ): List<ArticleWithAttachments>

    @Throws(ApiCallException::class)
    suspend fun getArticlesOrderByDateReverse(
        feedId: Long, sinceId: Long, offset: Int,
        showExcerpt: Boolean, showContent: Boolean,
        includeAttachments: Boolean
    ): List<ArticleWithAttachments>

    @Throws(ApiCallException::class)
    suspend fun getCategories(): List<Category>

    @Throws(ApiCallException::class)
    suspend fun getFeeds(): List<Feed>

    @Throws(ApiCallException::class)
    suspend fun getServerInfo(): ServerInfo

    @Throws(ApiCallException::class)
    suspend fun updateArticleField(id: Long, field: ArticlesContract.Transaction.Field, value: Boolean)

    companion object {
        val ALL_ARTICLES_FEED_ID: Long = -4
    }

}

/**
 * Contains information on the server.
 * Each optional field is null if it couldn't be retrieved
 */
data class ServerInfo(
    val apiUrl: String,
    val apiLevel: Int?,
    /** relative path to access feed icons */
    val feedsIconsUrl: String?,
    val serverVersion: String?
)
