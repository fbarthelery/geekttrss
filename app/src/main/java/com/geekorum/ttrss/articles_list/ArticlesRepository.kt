/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2021 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.articles_list

import androidx.paging.PagingSource
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleDao
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.data.Transaction
import com.geekorum.ttrss.data.TransactionsDao
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.session.Action
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * A Facade to access and modify Articles.
 */
class ArticlesRepository
@Inject constructor(
    private val articleDao: ArticleDao,
) {

    fun getAllArticles(): PagingSource<Int, ArticleWithFeed> = articleDao.getAllArticles()
    fun getAllArticlesOldestFirst(): PagingSource<Int, ArticleWithFeed> = articleDao.getAllArticlesOldestFirst()

    fun getAllUnreadArticles(): PagingSource<Int, ArticleWithFeed> {
        return articleDao.getAllUnreadArticles()
    }
    fun getAllUnreadArticlesOldestFirst(): PagingSource<Int, ArticleWithFeed> {
        return articleDao.getAllUnreadArticlesOldestFirst()
    }

    fun getAllPublishedArticles(): PagingSource<Int, ArticleWithFeed> = articleDao.getAllPublishedArticles()
    fun getAllPublishedArticlesOldestFirst(): PagingSource<Int, ArticleWithFeed> = articleDao.getAllPublishedArticlesOldestFirst()

    fun getAllUnreadPublishedArticles(): PagingSource<Int, ArticleWithFeed> = articleDao.getAllUnreadPublishedArticles()
    fun getAllUnreadPublishedArticlesOldestFirst(): PagingSource<Int, ArticleWithFeed> = articleDao.getAllUnreadPublishedArticlesOldestFirst()

    fun getAllStarredArticles(): PagingSource<Int, ArticleWithFeed> = articleDao.getAllStarredArticles()
    fun getAllStarredArticlesOldestFirst(): PagingSource<Int, ArticleWithFeed> = articleDao.getAllStarredArticlesOldestFirst()

    fun getAllUnreadStarredArticles(): PagingSource<Int, ArticleWithFeed> = articleDao.getAllUnreadStarredArticles()
    fun getAllUnreadStarredArticlesOldestFirst(): PagingSource<Int, ArticleWithFeed> = articleDao.getAllUnreadStarredArticlesOldestFirst()

    fun getAllArticlesForFeed(feedId: Long): PagingSource<Int, ArticleWithFeed> = articleDao.getAllArticlesForFeed(feedId)
    fun getAllArticlesForFeedOldestFirst(feedId: Long): PagingSource<Int, ArticleWithFeed> = articleDao.getAllArticlesForFeedOldestFirst(feedId)

    fun getAllUnreadArticlesForFeed(feedId: Long): PagingSource<Int, ArticleWithFeed> {
        return articleDao.getAllUnreadArticlesForFeed(feedId)
    }
    fun getAllUnreadArticlesForFeedOldestFirst(feedId: Long): PagingSource<Int, ArticleWithFeed> {
        return articleDao.getAllUnreadArticlesForFeedOldestFirst(feedId)
    }

    fun getAllArticlesForTag(tag: String): PagingSource<Int, ArticleWithFeed> = articleDao.getAllArticlesForTag(tag)
    fun getAllArticlesForTagOldestFirst(tag: String): PagingSource<Int, ArticleWithFeed> = articleDao.getAllArticlesForTagOldestFirst(tag)

    fun getAllUnreadArticlesForTag(tag: String): PagingSource<Int, ArticleWithFeed> {
        return articleDao.getAllUnreadArticlesForTag(tag)
    }
    fun getAllUnreadArticlesForTagOldestFirst(tag: String): PagingSource<Int, ArticleWithFeed> {
        return articleDao.getAllUnreadArticlesForTagOldestFirst(tag)
    }

    fun getAllArticlesUpdatedAfterTime(time: Long): PagingSource<Int, ArticleWithFeed> {
        return articleDao.getAllArticlesUpdatedAfterTime(time)
    }
    fun getAllArticlesUpdatedAfterTimeOldestFirst(time: Long): PagingSource<Int, ArticleWithFeed> {
        return articleDao.getAllArticlesUpdatedAfterTimeOldestFirst(time)
    }

    fun getAllUnreadArticlesUpdatedAfterTime(time: Long): PagingSource<Int, ArticleWithFeed> {
        return articleDao.getAllUnreadArticlesUpdatedAfterTime(time)
    }

    fun getAllUnreadArticlesUpdatedAfterTimeOldestFirst(time: Long): PagingSource<Int, ArticleWithFeed> {
        return articleDao.getAllUnreadArticlesUpdatedAfterTimeOldestFirst(time)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getArticleById(articleId: Long): Flow<Article?> = articleDao.getArticleById(articleId).distinctUntilChanged()

    suspend fun setArticleUnread(articleId: Long, newValue: Boolean) {
        articleDao.updateArticleUnread(articleId, newValue)
    }

    suspend fun setArticleStarred(articleId: Long, newValue: Boolean) {
        articleDao.updateArticleMarked(articleId, newValue)
    }

    fun searchArticles(query: String): PagingSource<Int, ArticleWithFeed> {
        return articleDao.searchArticles(query)
    }

    suspend fun getMostUnreadTags(count: Int): List<String> {
        return articleDao.getMostUnreadTags(count)
    }
}


open class SetArticleFieldAction(
    private val dispatchers: CoroutineDispatchersProvider,
    private val scope: CoroutineScope,
    private val transactionsDao: TransactionsDao,
    private val apiService: ApiService,
    private val articleId: Long,
    private val field: ArticlesContract.Transaction.Field,
    private val newValue: Boolean
) : Action {

    private var executionJob: Job? = null

    override fun execute() {
        executionJob = scope.launch(dispatchers.io) {
            updateArticleField(newValue)
        }
    }

    override fun undo() {
        scope.launch(dispatchers.io) {
            // wait for the request to be cancelled and done
            // to be sure that the new one happens after
            executionJob?.cancelAndJoin()
            updateArticleField(!newValue)
        }
    }

    protected open suspend fun updateArticleField(value: Boolean) {
        try {
            apiService.updateArticleField(articleId, field, value)
        } catch (e: Exception) {
            saveTransaction(articleId, field, value)
        }
    }

    private fun saveTransaction(articleId: Long, field: ArticlesContract.Transaction.Field, value: Boolean) {
        val transaction = Transaction(articleId = articleId,
            field = field.toString(),
            value = value)
        transactionsDao.insertUniqueTransaction(transaction)
    }

    class Factory @Inject internal constructor(
        private val setUnreadActionFactory: SetUnreadAction.Factory,
        private val setStarredActionFactory: SetStarredAction.Factory
    ) : SetUnreadAction.Factory by setUnreadActionFactory, SetStarredAction.Factory by setStarredActionFactory

}


/**
 * Action to set the unread field value of an article.
 */
internal class SetUnreadAction @AssistedInject internal constructor(
    dispatchers: CoroutineDispatchersProvider,
    private val articleDao: ArticleDao,
    transactionsDao: TransactionsDao,
    apiService: ApiService,
    @Assisted private val articleId: Long,
    @Assisted newValue: Boolean,
    @Assisted scope: CoroutineScope,
) : SetArticleFieldAction(dispatchers, scope, transactionsDao, apiService, articleId,
    ArticlesContract.Transaction.Field.UNREAD, newValue) {

    override suspend fun updateArticleField(value: Boolean) {
        val changed = articleDao.updateArticleUnread(articleId, value)
        if (changed > 0) {
            super.updateArticleField(value)
        }
    }

    @AssistedFactory
    interface Factory {
        fun createSetUnreadAction(scope: CoroutineScope, articleId: Long, newValue: Boolean): SetUnreadAction
    }
}

/**
 * Action to set the starred field value of an article.
 */
internal class SetStarredAction @AssistedInject internal constructor(
     dispatchers: CoroutineDispatchersProvider,
    private val articleDao: ArticleDao,
    transactionsDao: TransactionsDao,
    val apiService: ApiService,
    @Assisted val articleId: Long,
    @Assisted newValue: Boolean,
     @Assisted scope: CoroutineScope
) : SetArticleFieldAction(dispatchers, scope, transactionsDao, apiService, articleId,
        ArticlesContract.Transaction.Field.STARRED, newValue) {

    override suspend fun updateArticleField(value: Boolean) {
        val changed = articleDao.updateArticleMarked(articleId, value)
        if (changed > 0) {
            super.updateArticleField(value)
        }
    }

    @AssistedFactory
    interface Factory {
        fun createSetStarredAction(scope: CoroutineScope, articleId: Long, newValue: Boolean): SetStarredAction
    }

}
