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

import com.geekorum.ttrss.accounts.ServerInformation
import com.geekorum.ttrss.data.*
import com.geekorum.ttrss.data.Attachment
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.webapi.ApiCallException
import com.geekorum.ttrss.webapi.RetrofitServiceHelper
import com.geekorum.ttrss.webapi.TinyRssApi
import com.geekorum.ttrss.webapi.TokenRetriever
import com.geekorum.ttrss.webapi.model.*
import com.geekorum.ttrss.webapi.model.GetArticlesRequestPayload.SortOrder
import com.geekorum.ttrss.webapi.model.GetArticlesRequestPayload.SortOrder.DATE_REVERSE
import com.geekorum.ttrss.webapi.model.GetArticlesRequestPayload.SortOrder.FEED_DATES
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import okhttp3.ResponseBody
import okio.BufferedSource

/**
 * Implementation of [ApiService] which use retrofit to communicate with the Api server.
 */
class ApiRetrofitService(
    tokenRetriever: TokenRetriever,
    private val tinyrssApi: TinyRssApi,
    private val serverInformation: ServerInformation
) : ApiService {

    private val helper = RetrofitServiceHelper(tokenRetriever)

    @Throws(ApiCallException::class)
    override suspend fun getArticles(feedId: Long, sinceId: Long, offset: Int,
                             showExcerpt: Boolean, showContent: Boolean, includeAttachments: Boolean): List<ArticleWithAttachments> {
        return getArticlesInt(feedId, sinceId, offset, showExcerpt, showContent, includeAttachments)
    }

    @Throws(ApiCallException::class)
    override suspend fun getArticlesOrderByDateReverse(feedId: Long, sinceId: Long, offset: Int,
                                               showExcerpt: Boolean, showContent: Boolean, includeAttachments: Boolean): List<ArticleWithAttachments> {
        return getArticlesInt(feedId, sinceId, offset, showExcerpt, showContent, includeAttachments, sortOrder = DATE_REVERSE)
    }

    private suspend fun getArticlesInt(feedId: Long, sinceId: Long, offset: Int,
                                       showExcerpt: Boolean, showContent: Boolean,
                                       includeAttachments: Boolean = false,
                                       sortOrder: SortOrder = FEED_DATES): List<ArticleWithAttachments> {
        val payload = GetArticlesRequestPayload(
            feedId = feedId,
            viewMode = GetArticlesRequestPayload.ViewMode.ALL_ARTICLES,
            showContent = showContent,
            showExcerpt = showExcerpt,
            skip = offset,
            sinceId = sinceId,
            limit =  OFFLINE_SYNC_SEQ,
            includeAttachments = includeAttachments,
            orderBy = sortOrder)
        val response = executeOrFail("Unable to get articles") {
            tinyrssApi.getArticles(payload)
        }
        val headlines = response.result
        return headlines.map { it.toDataType() }
    }

    @Throws(ApiCallException::class)
    override suspend fun getCategories(): List<Category> {
        val payload = GetCategoriesRequestPayload(includeNested = true, unreadOnly = false)
        val response = executeOrFail("Unable to get categories") {
            tinyrssApi.getCategories(payload)
        }
        val feedCategories = response.result
        return feedCategories.map { it.toDataType() }
    }

    @Throws(ApiCallException::class)
    override suspend fun getFeeds(): List<Feed> {
        val payload = GetFeedsRequestPayload(
            includeNested = true, unreadOnly = false,
            categorieId = GetFeedsRequestPayload.CATEGORY_ID_ALL_EXCLUDE_VIRTUALS
        )
        val response = executeOrFail("Unable to get feeds") {
            tinyrssApi.getFeeds(payload)
        }
        val feedlist = response.result
        return feedlist.map { it.toDataType() }
    }

    @Throws(ApiCallException::class)
    override suspend fun updateArticleField(id: Long, field: ArticlesContract.Transaction.Field, value: Boolean) {
        val payload = UpdateArticleRequestPayload(
            id.toString(),
            if (value) 1 else 0,
            field.apiInteger
        )
        executeOrFail("Unable to update article") {
            tinyrssApi.updateArticle(payload)
        }
    }

    override suspend fun markFeedAsRead(feedId: Long) {
        val payload = CatchupFeedRequestPayload(feedId, isCategory = false)
        executeOrFail("Unable to mark feed as read") {
            tinyrssApi.catchupFeed(payload)
        }
    }

    override suspend fun getServerInfo(): ServerInfo = supervisorScope {
        val versionDeferred = async {
            val payload = GetVersionRequestPayload()
            executeOrFail("Unable to get server version") {
                tinyrssApi.getVersion(payload)
            }
        }

        val configDeferred = async {
            val payload = GetConfigRequestPayload()
            executeOrFail("Unable to get server configuration") {
                tinyrssApi.getConfig(payload)
            }
        }

        val apiLevelDeferred = async {
            val payload = GetApiLevelRequestPayload()
            executeOrFail("Unable to get server api level") {
                tinyrssApi.getApiLevel(payload)
            }
        }

        ServerInfo(
            apiUrl = serverInformation.apiUrl,
            apiLevel = apiLevelDeferred.runCatching { await().level }.getOrNull() ,
            feedsIconsUrl = configDeferred.runCatching { await().iconsUrl }.getOrNull(),
            serverVersion = versionDeferred.runCatching { await().version}.getOrNull()
        )
    }

    override suspend fun getFeedIcon(feedId: Long): BufferedSource {
        val payload = GetFeedIconPayload(feedId)
        val response = getResponseBodyOrFail("Unable to get feed icon") {
            tinyrssApi.getFeedIcon(payload)
        }
        return response.source()
    }


    @Throws(ApiCallException::class)
    suspend fun <T : ResponsePayload<*>> executeOrFail(failingMessage: String, block: suspend () -> T): T {
        return helper.executeOrFail(failingMessage, block)
    }

    @Throws(ApiCallException::class)
    suspend fun <T : ResponseBody> getResponseBodyOrFail(failingMessage: String, block: suspend () -> T): T {
        return helper.getResponseBodyOrFail(failingMessage, block)
    }


    companion object {
        private const val OFFLINE_SYNC_SEQ = 50
    }

}

private fun Headline.toDataType(): ArticleWithAttachments {
    val article = Article(
        id = id,
        contentData = ArticleContentIndexed(
            title = title,
            content = content,
            author = author,
            tags = tags.joinToString(", ")),
        isUnread = unread,
        isTransientUnread = unread,
        isStarred = marked,
        isPublished = published,
        score = score,
        lastTimeUpdate = lastUpdatedTimestamp,
        isUpdated = isUpdated,
        link = link,
        feedId = feedId ?: 0,
        contentExcerpt = excerpt
    )
    val attachments = this.attachments.map {
        Attachment(id = it.id,
            postId = it.postId,
            contentUrl = it.contentUrl,
            contentType = it.contentType,
            title = it.title,
            duration = it.duration,
            width = it.width,
            height = it.height)
    }
    return ArticleWithAttachments(article, attachments)
}

private fun FeedCategory.toDataType(): Category {
    return Category().apply {
        this.id = this@toDataType.id
        this.title = this@toDataType.title
        this.unreadCount = nbUnreadArticles
    }
}

private fun com.geekorum.ttrss.webapi.model.Feed.toDataType(): Feed {
    return Feed(
        id = id,
        title = title,
        displayTitle = displayTitle,
        url = url,
        unreadCount = nbUnreadArticles,
        catId = categoryId,
        lastTimeUpdate = lastUpdatedTimestamp,
        isSubscribed = true
    )
}
