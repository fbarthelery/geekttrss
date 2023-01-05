/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.sync

import com.geekorum.ttrss.data.*

/**
 * Database access interface for the synchronization process.
 */
interface DatabaseService {
    suspend fun <R> runInTransaction(block: suspend () -> R)

    suspend fun insertFeeds(feeds: List<Feed>)
    suspend fun deleteFeedsAndArticles(feeds: List<Feed>)
    suspend fun getFeeds(): List<Feed>
    suspend fun getFeedFavIcons(): List<FeedFavIcon>
    suspend fun updateFeedIconUrl(feedId: Long, url: String)

    suspend fun insertCategories(categories: List<Category>)
    suspend fun deleteCategories(categories: List<Category>)
    suspend fun getCategories(): List<Category>

    suspend fun getTransactions(): List<Transaction>
    suspend fun deleteTransaction(transaction: Transaction)

    suspend fun getArticle(id: Long): Article?
    suspend fun getLatestArticleFromFeed(feedId: Long): Article?
    suspend fun insertArticles(articles: List<Article>)
    suspend fun insertArticleTags(articlesTags: List<ArticlesTags>)
    suspend fun updateArticle(article: Article)
    suspend fun getLatestArticleId(): Long?
    suspend fun getLatestArticleIdFromFeed(feedId: Long): Long?

    suspend fun updateArticlesMetadata(metadata: List<Metadata>)

    suspend fun getAccountInfo(username: String, apiUrl: String): AccountInfo?
    suspend fun insertAccountInfo(accountInfo: AccountInfo)
    suspend fun insertAttachments(attachments: List<Attachment>)
}
