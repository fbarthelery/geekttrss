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
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.launch

private const val PREF_VIEW_MODE = "view_mode"
private const val STATE_FEED_ID = "feed_id"
private const val STATE_NEED_UNREAD = "need_unread"

/**
 * [ViewModel] for the [ArticlesListFragment].
 */
class FragmentViewModel @AssistedInject constructor(
    @Assisted private val state: SavedStateHandle,
    private val articlesRepository: ArticlesRepository,
    private val feedsRepository: FeedsRepository,
    private val backgroundJobManager: BackgroundJobManager,
    private val account: Account,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val onSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (PREF_VIEW_MODE == key) {
            updateNeedUnread()
        }
    }

    private val feedId = state.getLiveData(STATE_FEED_ID, Feed.FEED_ID_ALL_ARTICLES).apply {
        // workaround for out of sync values see
        // https://issuetracker.google.com/issues/129989646
        value = value
    }
    private val _pendingArticlesSetUnread = MutableLiveData<Int>().apply { value = 0 }

    val articles: LiveData<PagedList<Article>> = feedId.switchMap {
        feedsRepository.getFeedById(it)
    }.switchMap {
        checkNotNull(it)
        getArticlesForFeed(it)
    }

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
    val haveZeroArticles = articles.map { it.size == 0 }

    init {
        prefs.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        updateNeedUnread()
    }

    private fun getArticlesForFeed(feed: Feed): LiveData<PagedList<Article>> {
        return state.getLiveData<Boolean>(STATE_NEED_UNREAD).switchMap { needUnread ->
            val factory: DataSource.Factory<Int, Article> = when {
                feed.isStarredFeed -> if (needUnread)
                    articlesRepository.getAllUnreadStarredArticles()
                else
                    articlesRepository.getAllStarredArticles()

                feed.isPublishedFeed -> if (needUnread)
                    articlesRepository.getAllUnreadPublishedArticles()
                else
                    articlesRepository.getAllPublishedArticles()

                feed.isFreshFeed -> {
                    val freshTimeSec = System.currentTimeMillis() / 1000 - 3600 * 36
                    if (needUnread)
                        articlesRepository.getAllUnreadArticlesUpdatedAfterTime(freshTimeSec)
                    else
                        articlesRepository.getAllArticlesUpdatedAfterTime(freshTimeSec)
                }

                feed.isAllArticlesFeed -> if (needUnread)
                    articlesRepository.getAllUnreadArticles()
                else
                    articlesRepository.getAllArticles()

                else -> if (needUnread)
                    articlesRepository.getAllUnreadArticlesForFeed(feed.id)
                else
                    articlesRepository.getAllArticlesForFeed(feed.id)
            }

            factory.toLiveData(pageSize = 50,
                boundaryCallback = PageBoundaryCallback())
        }
    }

    private fun updateNeedUnread() {
        state[STATE_NEED_UNREAD] = when (prefs.getString(PREF_VIEW_MODE, "adaptive")) {
            "unread", "adaptive" -> true
            else -> false
        }
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

    override fun onCleared() {
        super.onCleared()
        prefs.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }

    private inner class PageBoundaryCallback<T> : PagedList.BoundaryCallback<T>() {
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

    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<FragmentViewModel> {
        override fun create(state: SavedStateHandle): FragmentViewModel
    }
}
