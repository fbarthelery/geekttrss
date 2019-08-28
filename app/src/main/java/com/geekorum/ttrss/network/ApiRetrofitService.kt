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
package com.geekorum.ttrss.network

import com.geekorum.geekdroid.network.TokenRetriever
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleContentIndexed
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.webapi.ApiCallException
import com.geekorum.ttrss.webapi.TinyRssApi
import com.geekorum.ttrss.webapi.checkStatus
import com.geekorum.ttrss.webapi.model.FeedCategory
import com.geekorum.ttrss.webapi.model.GetArticlesRequestPayload
import com.geekorum.ttrss.webapi.model.GetArticlesRequestPayload.SortOrder
import com.geekorum.ttrss.webapi.model.GetArticlesRequestPayload.SortOrder.DATE_REVERSE
import com.geekorum.ttrss.webapi.model.GetArticlesRequestPayload.SortOrder.FEED_DATES
import com.geekorum.ttrss.webapi.model.GetCategoriesRequestPayload
import com.geekorum.ttrss.webapi.model.GetFeedsRequestPayload
import com.geekorum.ttrss.webapi.model.Headline
import com.geekorum.ttrss.webapi.model.ResponsePayload
import com.geekorum.ttrss.webapi.model.UpdateArticleRequestPayload
import retrofit2.HttpException
import java.io.IOException


class RetrofitServiceHelper(
    private val tokenRetriever: TokenRetriever
) {

    @Throws(ApiCallException::class)
    suspend fun <T : ResponsePayload<*>> executeOrFail(failingMessage: String, block: suspend () -> T): T {
        try {
            val body = retryIfInvalidToken(block)
            body.checkStatus { failingMessage }
            return body
        } catch (e: IOException) {
            throw ApiCallException(failingMessage, e)
        } catch (e: HttpException) {
            throw ApiCallException(failingMessage, e)
        }
    }

    @Throws(IOException::class)
    suspend fun <T : ResponsePayload<*>> retryIfInvalidToken(block: suspend () -> T): T {
        val body = block()
        try {
            body.checkStatus()
        } catch (e: ApiCallException) {
            val error = e.errorCode
            if (error == ApiCallException.ApiError.LOGIN_FAILED || error == ApiCallException.ApiError.NOT_LOGGED_IN) {
                tokenRetriever.invalidateToken()
                return block()
            }
        }
        return body
    }

}


/**
 * Implementation of [ApiService] which use retrofit to communicate with the Api server.
 */
class ApiRetrofitService(
        tokenRetriever: TokenRetriever,
        private val tinyrssApi: TinyRssApi
) : ApiService {

    private val helper = RetrofitServiceHelper(tokenRetriever)

    @Throws(ApiCallException::class)
    override suspend fun getArticles(feedId: Long, sinceId: Long, offset: Int,
                             showExcerpt: Boolean, showContent: Boolean): List<Article> {
        return getArticlesInt(feedId, sinceId, offset, showExcerpt, showContent)
    }

    @Throws(ApiCallException::class)
    override suspend fun getArticlesOrderByDateReverse(feedId: Long, sinceId: Long, offset: Int,
                                               showExcerpt: Boolean, showContent: Boolean): List<Article> {
        return getArticlesInt(feedId, sinceId, offset, showExcerpt, showContent, DATE_REVERSE)
    }

    private suspend fun getArticlesInt(feedId: Long, sinceId: Long, offset: Int,
                                       showExcerpt: Boolean, showContent: Boolean,
                                       sortOrder: SortOrder = FEED_DATES): List<Article> {
        val payload = GetArticlesRequestPayload(
            feedId = feedId,
            viewMode = GetArticlesRequestPayload.ViewMode.ALL_ARTICLES,
            showContent = showContent,
            showExcerpt = showExcerpt,
            skip = offset,
            sinceId = sinceId,
            limit =  OFFLINE_SYNC_SEQ,
            orderBy = sortOrder)
        val response = executeOrFail("Unable to get articles") {
            tinyrssApi.getArticles(payload)
        }
        val headlines = response.result
        return headlines.map { it.toDataType() }
    }

    @Throws(ApiCallException::class)
    override suspend fun getCategories(): List<Category> {
        val payload = GetCategoriesRequestPayload(true, false)
        val response = executeOrFail("Unable to get categories") {
            tinyrssApi.getCategories(payload)
        }
        val feedCategories = response.result
        return feedCategories.map { it.toDataType() }
    }

    @Throws(ApiCallException::class)
    override suspend fun getFeeds(): List<Feed> {
        val payload = GetFeedsRequestPayload(true, false,
                    GetFeedsRequestPayload.CATEGORY_ID_ALL_EXCLUDE_VIRTUALS)
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
            field.asApiInteger()
        )
        executeOrFail("Unable to update article") {
            tinyrssApi.updateArticle(payload)
        }
    }

    @Throws(ApiCallException::class)
    suspend fun <T : ResponsePayload<*>> executeOrFail(failingMessage: String, block: suspend () -> T): T {
        return helper.executeOrFail(failingMessage, block)
    }


    companion object {
        private const val OFFLINE_SYNC_SEQ = 50
    }

}

private fun Headline.toDataType(): Article {
    return Article(
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
