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
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import com.geekorum.geekdroid.accounts.SyncInProgressLiveData
import com.geekorum.ttrss.background_job.BackgroundJobManager
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.session.Action
import com.geekorum.ttrss.session.SessionActivityComponent
import com.geekorum.ttrss.session.UndoManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

private const val STATE_NEED_UNREAD = "need_unread"
private const val STATE_ORDER_MOST_RECENT_FIRST = "order_most_recent_first" // most_recent_first, oldest_first

/**
 * Base [ViewModel] for a list of Articles.
 */
abstract class BaseArticlesViewModel(
    private val state: SavedStateHandle,
    componentFactory: SessionActivityComponent.Factory
) : ViewModel() {

    protected val component = componentFactory.newComponent()
    private val articlesRepository = component.articleRepository
    abstract val articles: Flow<PagingData<Article>>

    private val _pendingArticlesSetUnread = MutableLiveData<Int>().apply { value = 0 }

    abstract val isRefreshing: LiveData<Boolean>

    private var shouldRefreshOnZeroItems = true
    private val unreadActionUndoManager = UndoManager<Action>()

    // default value in databinding is False for boolean and 0 for int
    // we can't test size() == 0 in layout file because the default value will make the test true
    // and will briefly show the empty view
    // TODO
    val haveZeroArticles: LiveData<Boolean>
        get() = flowOf(false).asLiveData()

    abstract fun refresh()

    fun setSortByMostRecentFirst(mostRecentFirst: Boolean) {
        state[STATE_ORDER_MOST_RECENT_FIRST] = mostRecentFirst
    }

    fun setNeedUnread(needUnread: Boolean) {
        state[STATE_NEED_UNREAD] = needUnread
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

    // TODO replace with PagerAdapter.onRefreshFlow
    protected inner class PageBoundaryCallback<T : Any> : PagedList.BoundaryCallback<T>() {
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
        val starredArticles: PagingSource<Int, Article>
        val publishedArticles: PagingSource<Int, Article>
        val freshArticles: PagingSource<Int, Article>
        val allArticles: PagingSource<Int, Article>
        fun articlesForFeed(feedId: Long): PagingSource<Int, Article>
        fun articlesForTag(tag: String): PagingSource<Int, Article>
    }

    protected fun getArticleAccess(mostRecentFirst: Boolean, needUnread: Boolean): ArticlesAccess = when {
        needUnread && mostRecentFirst -> UnreadMostRecentAccess(articlesRepository)
        needUnread && !mostRecentFirst -> UnreadOldestAccess(articlesRepository)
        !needUnread && mostRecentFirst -> MostRecentAccess(articlesRepository)
        !needUnread && !mostRecentFirst -> OldestFirstAccess(articlesRepository)
        else -> UnreadMostRecentAccess(articlesRepository)
    }

    class UnreadMostRecentAccess(private val articlesRepository: ArticlesRepository) : ArticlesAccess {
        override val starredArticles: PagingSource<Int, Article>
            get() = articlesRepository.getAllUnreadStarredArticles()

        override val publishedArticles: PagingSource<Int, Article>
            get() = articlesRepository.getAllUnreadPublishedArticles()

        override val freshArticles: PagingSource<Int, Article>
            get() {
                val freshTimeSec = System.currentTimeMillis() / 1000 - 3600 * 36
                return articlesRepository.getAllUnreadArticlesUpdatedAfterTime(freshTimeSec)
            }

        override val allArticles: PagingSource<Int, Article>
            get() =  articlesRepository.getAllUnreadArticles()

        override fun articlesForFeed(feedId: Long): PagingSource<Int, Article> {
            return articlesRepository.getAllUnreadArticlesForFeed(feedId)
        }

        override fun articlesForTag(tag: String): PagingSource<Int, Article> {
            return articlesRepository.getAllUnreadArticlesForTag(tag)
        }
    }

    class UnreadOldestAccess(private val articlesRepository: ArticlesRepository) : ArticlesAccess {
        override val starredArticles: PagingSource<Int, Article>
            get() = articlesRepository.getAllUnreadStarredArticlesOldestFirst()

        override val publishedArticles: PagingSource<Int, Article>
            get() = articlesRepository.getAllUnreadPublishedArticlesOldestFirst()

        override val freshArticles: PagingSource<Int, Article>
            get() {
                val freshTimeSec = System.currentTimeMillis() / 1000 - 3600 * 36
                return articlesRepository.getAllUnreadArticlesUpdatedAfterTimeOldestFirst(freshTimeSec)
            }

        override val allArticles: PagingSource<Int, Article>
            get() =  articlesRepository.getAllUnreadArticlesOldestFirst()

        override fun articlesForFeed(feedId: Long): PagingSource<Int, Article> {
            return articlesRepository.getAllUnreadArticlesForFeedOldestFirst(feedId)
        }

        override fun articlesForTag(tag: String): PagingSource<Int, Article> {
            return articlesRepository.getAllUnreadArticlesForTagOldestFirst(tag)
        }
    }


    class MostRecentAccess(private val articlesRepository: ArticlesRepository) : ArticlesAccess {
        override val starredArticles: PagingSource<Int, Article>
            get() = articlesRepository.getAllStarredArticles()

        override val publishedArticles: PagingSource<Int, Article>
            get() = articlesRepository.getAllPublishedArticles()

        override val freshArticles: PagingSource<Int, Article>
            get() {
                val freshTimeSec = System.currentTimeMillis() / 1000 - 3600 * 36
                return articlesRepository.getAllArticlesUpdatedAfterTime(freshTimeSec)
            }

        override val allArticles: PagingSource<Int, Article>
            get() =  articlesRepository.getAllArticles()

        override fun articlesForFeed(feedId: Long): PagingSource<Int, Article> {
            return articlesRepository.getAllArticlesForFeed(feedId)
        }

        override fun articlesForTag(tag: String): PagingSource<Int, Article> {
            return articlesRepository.getAllArticlesForTag(tag)
        }
    }

    class OldestFirstAccess(private val articlesRepository: ArticlesRepository) : ArticlesAccess {
        override val starredArticles: PagingSource<Int, Article>
            get() = articlesRepository.getAllStarredArticlesOldestFirst()

        override val publishedArticles: PagingSource<Int, Article>
            get() = articlesRepository.getAllPublishedArticlesOldestFirst()

        override val freshArticles: PagingSource<Int, Article>
            get() {
                val freshTimeSec = System.currentTimeMillis() / 1000 - 3600 * 36
                return articlesRepository.getAllArticlesUpdatedAfterTimeOldestFirst(freshTimeSec)
            }

        override val allArticles: PagingSource<Int, Article>
            get() =  articlesRepository.getAllArticlesOldestFirst()

        override fun articlesForFeed(feedId: Long): PagingSource<Int, Article> {
            return articlesRepository.getAllArticlesForFeedOldestFirst(feedId)
        }

        override fun articlesForTag(tag: String): PagingSource<Int, Article> {
            return articlesRepository.getAllArticlesForTagOldestFirst(tag)
        }
    }

}

/**
 * ViewModel for [ArticlesListFragment]
 */
class ArticlesListViewModel @ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle,
    private val feedsRepository: FeedsRepository,
    private val backgroundJobManager: BackgroundJobManager,
    componentFactory: SessionActivityComponent.Factory
) : BaseArticlesViewModel(state, componentFactory) {

    val feedId = state.getLiveData(STATE_FEED_ID, Feed.FEED_ID_ALL_ARTICLES).apply {
        // workaround for out of sync values see
        // https://issuetracker.google.com/issues/129989646
        value = value
    }

    override val articles: Flow<PagingData<Article>> = feedId.asFlow().flatMapLatest {
        feedsRepository.getFeedById(it)
    }.flatMapLatest {
        checkNotNull(it)
        getArticlesForFeed(it)
    }.cachedIn(viewModelScope)

    private var refreshJobName: MutableLiveData<String?> = MutableLiveData<String?>().apply {
        value = null
    }

    private val account = component.account

    override val isRefreshing: LiveData<Boolean> = refreshJobName.switchMap {
        if (it == null)
            SyncInProgressLiveData(account, ArticlesContract.AUTHORITY)
        else
            backgroundJobManager.isRefreshingStatus(state.get<Long>(STATE_FEED_ID)!!)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getArticlesForFeed(feed: Feed): Flow<PagingData<Article>> {
        val isMostRecentOrderFlow = state.getLiveData<Boolean>(STATE_ORDER_MOST_RECENT_FIRST).asFlow()
        val needUnreadFlow = state.getLiveData<Boolean>(STATE_NEED_UNREAD).asFlow()
        return isMostRecentOrderFlow.combine(needUnreadFlow) { mostRecentFirst, needUnread ->
            getArticleAccess(mostRecentFirst, needUnread)
        }.flatMapLatest { access ->
            val config = PagingConfig(pageSize = 50)
            val pager = Pager(config) {
                when {
                    feed.isStarredFeed -> access.starredArticles
                    feed.isPublishedFeed -> access.publishedArticles
                    feed.isFreshFeed -> access.freshArticles
                    feed.isAllArticlesFeed -> access.allArticles
                    else -> access.articlesForFeed(feed.id)
                }
            }
            pager.flow
        }
    }


    override fun refresh() {
        viewModelScope.launch {
            val feedId: Long = state[STATE_FEED_ID]!!
            if (Feed.isVirtualFeed(feedId)) {
                backgroundJobManager.refresh(account)
            } else {
                refreshJobName.value = backgroundJobManager.refreshFeed(account, feedId)
            }
        }
    }

    companion object {
        private const val STATE_FEED_ID = "feed_id"
    }

}


class ArticlesListByTagViewModel @ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle,
    private val backgroundJobManager: BackgroundJobManager,
    componentFactory: SessionActivityComponent.Factory
) : BaseArticlesViewModel(state, componentFactory) {

    val tag = state.getLiveData<String>(STATE_TAG).apply {
        // workaround for out of sync values see
        // https://issuetracker.google.com/issues/129989646
        value = value
    }

    private val account: Account = component.account

    override val articles: Flow<PagingData<Article>> = tag.asFlow().flatMapLatest {
        getArticlesForTag(it)
    }.cachedIn(viewModelScope)

    override val isRefreshing: LiveData<Boolean> = SyncInProgressLiveData(account, ArticlesContract.AUTHORITY)

    override fun refresh() {
        backgroundJobManager.refresh(account)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getArticlesForTag(tag: String): Flow<PagingData<Article>> {
        val isMostRecentOrderFlow = state.getLiveData<Boolean>(STATE_ORDER_MOST_RECENT_FIRST).asFlow()
        val needUnreadFlow = state.getLiveData<Boolean>(STATE_NEED_UNREAD).asFlow()
        return isMostRecentOrderFlow.combine(needUnreadFlow) { mostRecentFirst, needUnread ->
            getArticleAccess(mostRecentFirst, needUnread)
        }.flatMapLatest { access ->
            val config = PagingConfig(pageSize = 50)
            val pager = Pager(config) {
                access.articlesForTag(tag)
            }
            pager.flow
        }
    }

    companion object {
        private const val STATE_TAG = "tag"
    }

}
