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

import android.accounts.Account
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.geekorum.geekdroid.accounts.SyncInProgressLiveData
import com.geekorum.geekdroid.dagger.ViewModelAssistedFactory
import com.geekorum.ttrss.background_job.BackgroundJobManager
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.session.Action
import com.geekorum.ttrss.session.UndoManager
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

private const val STATE_FEED_ID = "feed_id"
private const val STATE_NEED_UNREAD = "need_unread"
private const val STATE_ORDER_MOST_RECENT_FIRST = "order_most_recent_first" // most_recent_first, oldest_first

/**
 * Base [ViewModel] for a list of Articles.
 */
abstract class BaseArticlesViewModel(
    private val state: SavedStateHandle,
    private val articlesRepository: ArticlesRepository,
    private val backgroundJobManager: BackgroundJobManager,
    private val account: Account
) : ViewModel() {

    abstract val articles: LiveData<PagedList<Article>>

    private val _pendingArticlesSetUnread = MutableLiveData<Int>().apply { value = 0 }

    private var refreshJobName: MutableLiveData<String?> = MutableLiveData<String?>().apply {
        value = null
    }

    val isRefreshing: LiveData<Boolean> = refreshJobName.switchMap {
        if (it == null)
            SyncInProgressLiveData(account, ArticlesContract.AUTHORITY)
        else
            backgroundJobManager.isRefreshingStatus(state.get<Long>(STATE_FEED_ID)!!)
    }

    private var shouldRefreshOnZeroItems = true
    private val unreadActionUndoManager = UndoManager<Action>()

    // default value in databinding is False for boolean and 0 for int
    // we can't test size() == 0 in layout file because the default value will make the test true
    // and will briefly show the empty view
    val haveZeroArticles: LiveData<Boolean>
        get() = articles.map { it.size == 0 }

    fun setSortByMostRecentFirst(mostRecentFirst: Boolean) {
        state[STATE_ORDER_MOST_RECENT_FIRST] = mostRecentFirst
    }

    fun setNeedUnread(needUnread: Boolean) {
        state[STATE_NEED_UNREAD] = needUnread
    }

    fun refresh() {
        viewModelScope.launch {
            val feedId: Long = state[STATE_FEED_ID]!!
            if (Feed.isVirtualFeed(feedId)) {
                backgroundJobManager.refresh(account)
            } else {
                refreshJobName.value = backgroundJobManager.refreshFeed(account, feedId)
            }
        }
    }

    fun setArticleUnread(articleId: Long, newValue: Boolean) {
        val unreadAction = articlesRepository.setArticleUnread(articleId, newValue)
        unreadActionUndoManager.recordAction(unreadAction)
        _pendingArticlesSetUnread.value = unreadActionUndoManager.nbActions
    }

    fun getPendingArticlesSetUnread(): LiveData<Int> {
        return _pendingArticlesSetUnread
    }

    fun setArticleStarred(articleId: Long, newValue: Boolean) {
        articlesRepository.setArticleStarred(articleId, newValue)
    }

    fun commitSetUnreadActions() {
        unreadActionUndoManager.clear()
        _pendingArticlesSetUnread.value = unreadActionUndoManager.nbActions
    }

    fun undoSetUnreadActions() {
        unreadActionUndoManager.undoAll()
        _pendingArticlesSetUnread.value = unreadActionUndoManager.nbActions
    }

    protected inner class PageBoundaryCallback<T> : PagedList.BoundaryCallback<T>() {
        override fun onZeroItemsLoaded() {
            if (shouldRefreshOnZeroItems) {
                shouldRefreshOnZeroItems = false
                refresh()
            }
        }

        override fun onItemAtFrontLoaded(itemAtFront: T) {
            shouldRefreshOnZeroItems = true
        }

        override fun onItemAtEndLoaded(itemAtEnd: T) {
            shouldRefreshOnZeroItems = true
        }
    }

    protected interface ArticlesAccess {
        val starredArticles: DataSource.Factory<Int, Article>
        val publishedArticles: DataSource.Factory<Int, Article>
        val freshArticles: DataSource.Factory<Int, Article>
        val allArticles: DataSource.Factory<Int, Article>
        fun articlesForFeed(feedId: Long) :DataSource.Factory<Int, Article>

    }

    protected fun getArticleAccess(mostRecentFirst: Boolean, needUnread: Boolean): ArticlesAccess = when {
        needUnread && mostRecentFirst -> UnreadMostRecentAccess(articlesRepository)
        needUnread && !mostRecentFirst -> UnreadOldestAccess(articlesRepository)
        !needUnread && mostRecentFirst -> MostRecentAccess(articlesRepository)
        !needUnread && !mostRecentFirst -> OldestFirstAccess(articlesRepository)
        else -> UnreadMostRecentAccess(articlesRepository)
    }

    class UnreadMostRecentAccess(private val articlesRepository: ArticlesRepository) : ArticlesAccess {
        override val starredArticles: DataSource.Factory<Int, Article>
            get() = articlesRepository.getAllUnreadStarredArticles()

        override val publishedArticles: DataSource.Factory<Int, Article>
            get() = articlesRepository.getAllUnreadPublishedArticles()

        override val freshArticles: DataSource.Factory<Int, Article>
            get() {
                val freshTimeSec = System.currentTimeMillis() / 1000 - 3600 * 36
                return articlesRepository.getAllUnreadArticlesUpdatedAfterTime(freshTimeSec)
            }

        override val allArticles: DataSource.Factory<Int, Article>
            get() =  articlesRepository.getAllUnreadArticles()

        override fun articlesForFeed(feedId: Long): DataSource.Factory<Int, Article> {
            return articlesRepository.getAllUnreadArticlesForFeed(feedId)
        }
    }

    class UnreadOldestAccess(private val articlesRepository: ArticlesRepository) : ArticlesAccess {
        override val starredArticles: DataSource.Factory<Int, Article>
            get() = articlesRepository.getAllUnreadStarredArticlesOldestFirst()

        override val publishedArticles: DataSource.Factory<Int, Article>
            get() = articlesRepository.getAllUnreadPublishedArticlesOldestFirst()

        override val freshArticles: DataSource.Factory<Int, Article>
            get() {
                val freshTimeSec = System.currentTimeMillis() / 1000 - 3600 * 36
                return articlesRepository.getAllUnreadArticlesUpdatedAfterTimeOldestFirst(freshTimeSec)
            }

        override val allArticles: DataSource.Factory<Int, Article>
            get() =  articlesRepository.getAllUnreadArticlesOldestFirst()

        override fun articlesForFeed(feedId: Long): DataSource.Factory<Int, Article> {
            return articlesRepository.getAllUnreadArticlesForFeedOldestFirst(feedId)
        }
    }


    class MostRecentAccess(private val articlesRepository: ArticlesRepository) : ArticlesAccess {
        override val starredArticles: DataSource.Factory<Int, Article>
            get() = articlesRepository.getAllStarredArticles()

        override val publishedArticles: DataSource.Factory<Int, Article>
            get() = articlesRepository.getAllPublishedArticles()

        override val freshArticles: DataSource.Factory<Int, Article>
            get() {
                val freshTimeSec = System.currentTimeMillis() / 1000 - 3600 * 36
                return articlesRepository.getAllArticlesUpdatedAfterTime(freshTimeSec)
            }

        override val allArticles: DataSource.Factory<Int, Article>
            get() =  articlesRepository.getAllArticles()

        override fun articlesForFeed(feedId: Long): DataSource.Factory<Int, Article> {
            return articlesRepository.getAllArticlesForFeed(feedId)
        }
    }

    class OldestFirstAccess(private val articlesRepository: ArticlesRepository) : ArticlesAccess {
        override val starredArticles: DataSource.Factory<Int, Article>
            get() = articlesRepository.getAllStarredArticlesOldestFirst()

        override val publishedArticles: DataSource.Factory<Int, Article>
            get() = articlesRepository.getAllPublishedArticlesOldestFirst()

        override val freshArticles: DataSource.Factory<Int, Article>
            get() {
                val freshTimeSec = System.currentTimeMillis() / 1000 - 3600 * 36
                return articlesRepository.getAllArticlesUpdatedAfterTimeOldestFirst(freshTimeSec)
            }

        override val allArticles: DataSource.Factory<Int, Article>
            get() =  articlesRepository.getAllArticlesOldestFirst()

        override fun articlesForFeed(feedId: Long): DataSource.Factory<Int, Article> {
            return articlesRepository.getAllArticlesForFeedOldestFirst(feedId)
        }
    }

}

/**
 * ViewModel for [ArticlesListFragment]
 */
class ArticlesListViewModel @AssistedInject constructor(
    @Assisted private val state: SavedStateHandle,
    articlesRepository: ArticlesRepository,
    private val feedsRepository: FeedsRepository,
    backgroundJobManager: BackgroundJobManager,
    account: Account
) : BaseArticlesViewModel(state, articlesRepository, backgroundJobManager, account) {

    val feedId = state.getLiveData(STATE_FEED_ID, Feed.FEED_ID_ALL_ARTICLES).apply {
        // workaround for out of sync values see
        // https://issuetracker.google.com/issues/129989646
        value = value
    }

    override val articles: LiveData<PagedList<Article>> = feedId.switchMap {
        feedsRepository.getFeedById(it)
    }.switchMap {
        checkNotNull(it)
        getArticlesForFeed(it)
    }

    private fun getArticlesForFeed(feed: Feed): LiveData<PagedList<Article>> {
        val isMostRecentOrderFlow = state.getLiveData<Boolean>(STATE_ORDER_MOST_RECENT_FIRST).asFlow()
        val needUnreadFlow = state.getLiveData<Boolean>(STATE_NEED_UNREAD).asFlow()
        return isMostRecentOrderFlow.combine(needUnreadFlow) { mostRecentFirst, needUnread ->
            getArticleAccess(mostRecentFirst, needUnread)
        }.mapLatest { access ->
            when {
                feed.isStarredFeed -> access.starredArticles
                feed.isPublishedFeed -> access.publishedArticles
                feed.isFreshFeed -> access.freshArticles
                feed.isAllArticlesFeed -> access.allArticles
                else -> access.articlesForFeed(feed.id)
            }
        }.asLiveData()
            .switchMap { factory ->
                val liveData = factory.toLiveData(pageSize = 50,
                    boundaryCallback = PageBoundaryCallback())
                liveData
            }
    }


    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<ArticlesListViewModel> {
        override fun create(state: SavedStateHandle): ArticlesListViewModel
    }
}
