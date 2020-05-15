/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2020 by Frederic-Charles Barthelery.
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

import androidx.paging.DataSource
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleDao
import com.geekorum.ttrss.data.Transaction
import com.geekorum.ttrss.data.TransactionsDao
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.session.Action
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A Facade to access and modify Articles.
 */
class ArticlesRepository
@Inject constructor(
    private val articleDao: ArticleDao,
    private val setFieldActionFactory: SetArticleFieldAction.Factory
) {

    fun getAllArticles(): DataSource.Factory<Int, Article> = articleDao.getAllArticles()
    fun getAllArticlesOldestFirst(): DataSource.Factory<Int, Article> = articleDao.getAllArticlesOldestFirst()

    fun getAllUnreadArticles(): DataSource.Factory<Int, Article> {
        return articleDao.getAllUnreadArticles()
    }
    fun getAllUnreadArticlesOldestFirst(): DataSource.Factory<Int, Article> {
        return articleDao.getAllUnreadArticlesOldestFirst()
    }

    fun getAllPublishedArticles(): DataSource.Factory<Int, Article> = articleDao.getAllPublishedArticles()
    fun getAllPublishedArticlesOldestFirst(): DataSource.Factory<Int, Article> = articleDao.getAllPublishedArticlesOldestFirst()

    fun getAllUnreadPublishedArticles(): DataSource.Factory<Int, Article> = articleDao.getAllUnreadPublishedArticles()
    fun getAllUnreadPublishedArticlesOldestFirst(): DataSource.Factory<Int, Article> = articleDao.getAllUnreadPublishedArticlesOldestFirst()

    fun getAllStarredArticles(): DataSource.Factory<Int, Article> = articleDao.getAllStarredArticles()
    fun getAllStarredArticlesOldestFirst(): DataSource.Factory<Int, Article> = articleDao.getAllStarredArticlesOldestFirst()

    fun getAllUnreadStarredArticles(): DataSource.Factory<Int, Article> = articleDao.getAllUnreadStarredArticles()
    fun getAllUnreadStarredArticlesOldestFirst(): DataSource.Factory<Int, Article> = articleDao.getAllUnreadStarredArticlesOldestFirst()

    fun getAllArticlesForFeed(feedId: Long): DataSource.Factory<Int, Article> = articleDao.getAllArticlesForFeed(feedId)
    fun getAllArticlesForFeedOldestFirst(feedId: Long): DataSource.Factory<Int, Article> = articleDao.getAllArticlesForFeedOldestFirst(feedId)

    fun getAllUnreadArticlesForFeed(feedId: Long): DataSource.Factory<Int, Article> {
        return articleDao.getAllUnreadArticlesForFeed(feedId)
    }
    fun getAllUnreadArticlesForFeedOldestFirst(feedId: Long): DataSource.Factory<Int, Article> {
        return articleDao.getAllUnreadArticlesForFeedOldestFirst(feedId)
    }

    fun getAllArticlesUpdatedAfterTime(time: Long): DataSource.Factory<Int, Article> {
        return articleDao.getAllArticlesUpdatedAfterTime(time)
    }
    fun getAllArticlesUpdatedAfterTimeOldestFirst(time: Long): DataSource.Factory<Int, Article> {
        return articleDao.getAllArticlesUpdatedAfterTimeOldestFirst(time)
    }

    fun getAllUnreadArticlesUpdatedAfterTime(time: Long): DataSource.Factory<Int, Article> {
        return articleDao.getAllUnreadArticlesUpdatedAfterTime(time)
    }

    fun getAllUnreadArticlesUpdatedAfterTimeOldestFirst(time: Long): DataSource.Factory<Int, Article> {
        return articleDao.getAllUnreadArticlesUpdatedAfterTimeOldestFirst(time)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getArticleById(articleId: Long): Flow<Article?> = articleDao.getArticleById(articleId).distinctUntilChanged()

    fun setArticleUnread(articleId: Long, newValue: Boolean): Action {
        val setUnreadAction = setFieldActionFactory.createSetUnreadAction(articleId, newValue)
        setUnreadAction.execute()
        return setUnreadAction
    }

    fun setArticleStarred(articleId: Long, newValue: Boolean) {
        val setStarredAction = setFieldActionFactory.createSetStarredAction(articleId, newValue)
        setStarredAction.execute()
    }

    fun searchArticles(query: String): DataSource.Factory<Int, Article> {
        return articleDao.searchArticles(query)
    }

    suspend fun getMostUnreadTags(count: Int): List<String> {
        return articleDao.getMostUnreadTags(count)
    }
}


open class SetArticleFieldAction(
    private val dispatchers: CoroutineDispatchersProvider,
    private val transactionsDao: TransactionsDao,
    private val apiService: ApiService,
    private val articleId: Long,
    private val field: ArticlesContract.Transaction.Field,
    private val newValue: Boolean
) : Action {

    private var executionJob: Job? = null

    override fun execute() {
        executionJob = GlobalScope.launch(dispatchers.io) {
            updateArticleField(newValue)
        }
    }

    override fun undo() {
        GlobalScope.launch(dispatchers.io) {
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
    @Assisted newValue: Boolean
) : SetArticleFieldAction(dispatchers, transactionsDao, apiService, articleId,
    ArticlesContract.Transaction.Field.UNREAD, newValue) {

    override suspend fun updateArticleField(value: Boolean) {
        val changed = articleDao.updateArticleUnread(articleId, value)
        if (changed > 0) {
            super.updateArticleField(value)
        }
    }

    @AssistedInject.Factory
    interface Factory {
        fun createSetUnreadAction(articleId: Long, newValue: Boolean): Action
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
    @Assisted newValue: Boolean
) : SetArticleFieldAction(dispatchers, transactionsDao, apiService, articleId,
        ArticlesContract.Transaction.Field.STARRED, newValue) {

    override suspend fun updateArticleField(value: Boolean) {
        val changed = articleDao.updateArticleMarked(articleId, value)
        if (changed > 0) {
            super.updateArticleField(value)
        }
    }

    @AssistedInject.Factory
    interface Factory {
        fun createSetStarredAction(articleId: Long, newValue: Boolean): Action
    }

}
