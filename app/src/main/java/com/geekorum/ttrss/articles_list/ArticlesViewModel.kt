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
package com.geekorum.ttrss.articles_list

import android.accounts.Account
import androidx.core.text.parseAsHtml
import androidx.lifecycle.*
import androidx.paging.*
import com.geekorum.geekdroid.accounts.SyncInProgressLiveData
import com.geekorum.ttrss.background_job.BackgroundJobManager
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.session.Action
import com.geekorum.ttrss.session.SessionActivityComponent
import com.geekorum.ttrss.session.UndoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val setFieldActionFactory = component.setArticleFieldActionFactory

    abstract val articles: Flow<PagingData<ArticleWithFeed>>

    private val _pendingArticlesSetUnread = MutableStateFlow(0)
    val pendingArticlesSetUnread = _pendingArticlesSetUnread.asStateFlow()

    abstract val isRefreshing: StateFlow<Boolean>
    abstract val isMultiFeed: StateFlow<Boolean>

    private val unreadActionUndoManager = UndoManager<Action>()

    abstract fun refresh()

    fun setSortByMostRecentFirst(mostRecentFirst: Boolean) {
        state[STATE_ORDER_MOST_RECENT_FIRST] = mostRecentFirst
    }

    fun setNeedUnread(needUnread: Boolean) {
        state[STATE_NEED_UNREAD] = needUnread
    }

    fun setArticleUnread(articleId: Long, newValue: Boolean) {
        setFieldActionFactory.createSetUnreadAction(viewModelScope, articleId, newValue).also {
            it.execute()
            unreadActionUndoManager.recordAction(it)
        }
        _pendingArticlesSetUnread.value = unreadActionUndoManager.nbActions
    }

    fun setArticleStarred(articleId: Long, newValue: Boolean) {
        val action = setFieldActionFactory.createSetStarredAction(viewModelScope, articleId, newValue)
        action.execute()
    }

    fun commitSetUnreadActions() {
        unreadActionUndoManager.clear()
        _pendingArticlesSetUnread.value = unreadActionUndoManager.nbActions
    }

    fun undoSetUnreadActions() {
        unreadActionUndoManager.undoAll()
        _pendingArticlesSetUnread.value = unreadActionUndoManager.nbActions
    }

    protected fun prepareArticlePagingData(pagingData: PagingData<ArticleWithFeed>) =
        pagingData.map { articleWithFeed ->
            articleWithFeed.copy(
                article = articleWithFeed.article.copy(
                    contentData = articleWithFeed.article.contentData.copy(
                        title = articleWithFeed.article.contentData.title.parseAsHtml().toString()
                    )
                )
            )
        }

    protected interface ArticlesAccess {
        val starredArticles: PagingSource<Int, ArticleWithFeed>
        val publishedArticles: PagingSource<Int, ArticleWithFeed>
        val freshArticles: PagingSource<Int, ArticleWithFeed>
        val allArticles: PagingSource<Int, ArticleWithFeed>
        fun articlesForFeed(feedId: Long): PagingSource<Int, ArticleWithFeed>
        fun articlesForTag(tag: String): PagingSource<Int, ArticleWithFeed>
    }

    protected fun getArticleAccess(mostRecentFirst: Boolean, needUnread: Boolean): ArticlesAccess = when {
        needUnread && mostRecentFirst -> UnreadMostRecentAccess(articlesRepository)
        needUnread && !mostRecentFirst -> UnreadOldestAccess(articlesRepository)
        !needUnread && mostRecentFirst -> MostRecentAccess(articlesRepository)
        !needUnread && !mostRecentFirst -> OldestFirstAccess(articlesRepository)
        else -> UnreadMostRecentAccess(articlesRepository)
    }

    class UnreadMostRecentAccess(private val articlesRepository: ArticlesRepository) : ArticlesAccess {
        override val starredArticles: PagingSource<Int, ArticleWithFeed>
            get() = articlesRepository.getAllUnreadStarredArticles()

        override val publishedArticles: PagingSource<Int, ArticleWithFeed>
            get() = articlesRepository.getAllUnreadPublishedArticles()

        override val freshArticles: PagingSource<Int, ArticleWithFeed>
            get() {
                val freshTimeSec = System.currentTimeMillis() / 1000 - 3600 * 36
                return articlesRepository.getAllUnreadArticlesUpdatedAfterTime(freshTimeSec)
            }

        override val allArticles: PagingSource<Int, ArticleWithFeed>
            get() =  articlesRepository.getAllUnreadArticles()

        override fun articlesForFeed(feedId: Long): PagingSource<Int, ArticleWithFeed> {
            return articlesRepository.getAllUnreadArticlesForFeed(feedId)
        }

        override fun articlesForTag(tag: String): PagingSource<Int, ArticleWithFeed> {
            return articlesRepository.getAllUnreadArticlesForTag(tag)
        }
    }

    class UnreadOldestAccess(private val articlesRepository: ArticlesRepository) : ArticlesAccess {
        override val starredArticles: PagingSource<Int, ArticleWithFeed>
            get() = articlesRepository.getAllUnreadStarredArticlesOldestFirst()

        override val publishedArticles: PagingSource<Int, ArticleWithFeed>
            get() = articlesRepository.getAllUnreadPublishedArticlesOldestFirst()

        override val freshArticles: PagingSource<Int, ArticleWithFeed>
            get() {
                val freshTimeSec = System.currentTimeMillis() / 1000 - 3600 * 36
                return articlesRepository.getAllUnreadArticlesUpdatedAfterTimeOldestFirst(freshTimeSec)
            }

        override val allArticles: PagingSource<Int, ArticleWithFeed>
            get() =  articlesRepository.getAllUnreadArticlesOldestFirst()

        override fun articlesForFeed(feedId: Long): PagingSource<Int, ArticleWithFeed> {
            return articlesRepository.getAllUnreadArticlesForFeedOldestFirst(feedId)
        }

        override fun articlesForTag(tag: String): PagingSource<Int, ArticleWithFeed> {
            return articlesRepository.getAllUnreadArticlesForTagOldestFirst(tag)
        }
    }


    class MostRecentAccess(private val articlesRepository: ArticlesRepository) : ArticlesAccess {
        override val starredArticles: PagingSource<Int, ArticleWithFeed>
            get() = articlesRepository.getAllStarredArticles()

        override val publishedArticles: PagingSource<Int, ArticleWithFeed>
            get() = articlesRepository.getAllPublishedArticles()

        override val freshArticles: PagingSource<Int, ArticleWithFeed>
            get() {
                val freshTimeSec = System.currentTimeMillis() / 1000 - 3600 * 36
                return articlesRepository.getAllArticlesUpdatedAfterTime(freshTimeSec)
            }

        override val allArticles: PagingSource<Int, ArticleWithFeed>
            get() =  articlesRepository.getAllArticles()

        override fun articlesForFeed(feedId: Long): PagingSource<Int, ArticleWithFeed> {
            return articlesRepository.getAllArticlesForFeed(feedId)
        }

        override fun articlesForTag(tag: String): PagingSource<Int, ArticleWithFeed> {
            return articlesRepository.getAllArticlesForTag(tag)
        }
    }

    class OldestFirstAccess(private val articlesRepository: ArticlesRepository) : ArticlesAccess {
        override val starredArticles: PagingSource<Int, ArticleWithFeed>
            get() = articlesRepository.getAllStarredArticlesOldestFirst()

        override val publishedArticles: PagingSource<Int, ArticleWithFeed>
            get() = articlesRepository.getAllPublishedArticlesOldestFirst()

        override val freshArticles: PagingSource<Int, ArticleWithFeed>
            get() {
                val freshTimeSec = System.currentTimeMillis() / 1000 - 3600 * 36
                return articlesRepository.getAllArticlesUpdatedAfterTimeOldestFirst(freshTimeSec)
            }

        override val allArticles: PagingSource<Int, ArticleWithFeed>
            get() =  articlesRepository.getAllArticlesOldestFirst()

        override fun articlesForFeed(feedId: Long): PagingSource<Int, ArticleWithFeed> {
            return articlesRepository.getAllArticlesForFeedOldestFirst(feedId)
        }

        override fun articlesForTag(tag: String): PagingSource<Int, ArticleWithFeed> {
            return articlesRepository.getAllArticlesForTagOldestFirst(tag)
        }
    }

}

/**
 * ViewModel for [ArticlesListScreen]
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ArticlesListViewModel @Inject constructor(
    private val state: SavedStateHandle,
    feedsRepository: FeedsRepository,
    private val backgroundJobManager: BackgroundJobManager,
    componentFactory: SessionActivityComponent.Factory
) : BaseArticlesViewModel(state, componentFactory) {

    val feedId: Long = state[STATE_FEED_ID]!!

    override val isMultiFeed: StateFlow<Boolean> = MutableStateFlow(Feed.isVirtualFeed(feedId))

    override val articles: Flow<PagingData<ArticleWithFeed>> = feedsRepository.getFeedById(feedId)
        .filterNotNull()
        .flatMapLatest { getArticlesForFeed(it) }
        .map(::prepareArticlePagingData)
        .cachedIn(viewModelScope)

    private val refreshJobName = MutableStateFlow<String?>(null)

    private val account = component.account

    override val isRefreshing = refreshJobName.flatMapLatest {
        if (it == null)
            SyncInProgressLiveData(account, ArticlesContract.AUTHORITY).asFlow()
        else
            backgroundJobManager.isRefreshingStatus(state.get<Long>(STATE_FEED_ID)!!).asFlow()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getArticlesForFeed(feed: Feed): Flow<PagingData<ArticleWithFeed>> {
        val isMostRecentOrderFlow = state.getStateFlow(STATE_ORDER_MOST_RECENT_FIRST, false)
        val needUnreadFlow = state.getStateFlow(STATE_NEED_UNREAD, false)
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ArticlesListByTagViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val backgroundJobManager: BackgroundJobManager,
    componentFactory: SessionActivityComponent.Factory
) : BaseArticlesViewModel(state, componentFactory) {

    val tag = state.getStateFlow<String>(STATE_TAG, "")
    override val isMultiFeed: StateFlow<Boolean> = MutableStateFlow(true)

    private val account: Account = component.account

    override val articles: Flow<PagingData<ArticleWithFeed>> = tag
        .flatMapLatest {
            getArticlesForTag(it)
        }.map(::prepareArticlePagingData)
        .cachedIn(viewModelScope)

    override val isRefreshing = SyncInProgressLiveData(account, ArticlesContract.AUTHORITY).asFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    override fun refresh() {
        backgroundJobManager.refresh(account)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getArticlesForTag(tag: String): Flow<PagingData<ArticleWithFeed>> {
        val isMostRecentOrderFlow = state.getStateFlow(STATE_ORDER_MOST_RECENT_FIRST, false)
        val needUnreadFlow = state.getStateFlow(STATE_NEED_UNREAD, false)
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
