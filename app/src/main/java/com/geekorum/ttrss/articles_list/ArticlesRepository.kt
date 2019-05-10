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
package com.geekorum.ttrss.articles_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.paging.DataSource
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleDao
import com.geekorum.ttrss.data.Transaction
import com.geekorum.ttrss.data.TransactionsDao
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.session.Action
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
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

    fun getAllUnreadArticles(): DataSource.Factory<Int, Article> {
        return articleDao.getAllUnreadArticles()
    }

    fun getAllPublishedArticles(): DataSource.Factory<Int, Article> = articleDao.getAllPublishedArticles()

    fun getAllUnreadPublishedArticles(): DataSource.Factory<Int, Article> = articleDao.getAllUnreadPublishedArticles()

    fun getAllStarredArticles(): DataSource.Factory<Int, Article> = articleDao.getAllStarredArticles()

    fun getAllUnreadStarredArticles(): DataSource.Factory<Int, Article> = articleDao.getAllUnreadStarredArticles()

    fun getAllArticlesForFeed(feedId: Long): DataSource.Factory<Int, Article> = articleDao.getAllArticlesForFeed(feedId)

    fun getAllUnreadArticlesForFeed(feedId: Long): DataSource.Factory<Int, Article> {
        return articleDao.getAllUnreadArticlesForFeed(feedId)
    }

    fun getAllArticlesUpdatedAfterTime(time: Long): DataSource.Factory<Int, Article> {
        return articleDao.getAllArticlesUpdatedAfterTime(time)
    }

    fun getAllUnreadArticlesUpdatedAfterTime(time: Long): DataSource.Factory<Int, Article> {
        return articleDao.getAllUnreadArticlesUpdatedAfterTime(time)
    }

    fun getArticleById(articleId: Long): LiveData<Article?> = articleDao.getArticleById(articleId).distinctUntilChanged()

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
}


open class SetArticleFieldAction(
    private val transactionsDao: TransactionsDao,
    private val apiService: ApiService,
    private val articleId: Long,
    private val field: ArticlesContract.Transaction.Field,
    private val newValue: Boolean
) : Action {

    private var executionJob: Job? = null

    override fun execute() {
        executionJob = GlobalScope.launch(Dispatchers.IO) {
            updateArticleField(newValue)
        }
    }

    override fun undo() {
        GlobalScope.launch(Dispatchers.IO) {
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
    private val articleDao: ArticleDao,
    transactionsDao: TransactionsDao,
    apiService: ApiService,
    @Assisted private val articleId: Long,
    @Assisted newValue: Boolean
) : SetArticleFieldAction(transactionsDao, apiService, articleId,
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
    private val articleDao: ArticleDao,
    transactionsDao: TransactionsDao,
    val apiService: ApiService,
    @Assisted val articleId: Long,
    @Assisted newValue: Boolean
) : SetArticleFieldAction(transactionsDao, apiService, articleId, ArticlesContract.Transaction.Field.STARRED,
    newValue) {

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
