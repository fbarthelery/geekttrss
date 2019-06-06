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

import android.accounts.Account
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.geekorum.ttrss.BackgroundJobManager
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.session.Action
import com.geekorum.ttrss.session.UndoManager
import javax.inject.Inject

private const val PREF_VIEW_MODE = "view_mode"

/**
 * [ViewModel] for the [ArticlesListFragment].
 */
class FragmentViewModel @Inject constructor(
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

    init {
        prefs.apply {
            registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        }
    }

    private val feedId = MutableLiveData<Long>()
    private val _pendingArticlesSetUnread = MutableLiveData<Int>().apply { value = 0 }
    private val feed: LiveData<Feed?> = feedId.switchMap { feedsRepository.getFeedById(it) }

    val articles: LiveData<PagedList<Article>> = feed.switchMap {
        checkNotNull(it)
        getArticlesForFeed(it)
    }

    private val needUnreadLiveData = MutableLiveData<Boolean>()
    private val unreadActionUndoManager = UndoManager<Action>()

    // default value in databinding is False for boolean and 0 for int
    // we can't test size() == 0 in layout file because the default value will make the test true
    // and will briefly show the empty view
    val haveZeroArticles = articles.map { it.size == 0 }

    init {
        updateNeedUnread()
    }

    fun init(feedId: Long) {
        this.feedId.value = feedId
    }

    private fun getArticlesForFeed(feed: Feed): LiveData<PagedList<Article>> {
        return needUnreadLiveData.switchMap { needUnread ->
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
            LivePagedListBuilder(factory, 50)
                .setBoundaryCallback(PageBoundaryCallback()).build()
        }
    }

    private fun updateNeedUnread() {
        when (prefs.getString(PREF_VIEW_MODE, "adaptive")!!) {
            "unread", "adaptive" -> needUnreadLiveData.setValue(true)
            else -> needUnreadLiveData.setValue(false)
        }
    }

    fun refresh() {
        backgroundJobManager.refreshFeed(account, feedId.value!!)
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
            // we should use PagingRequestHelper to prevent calling refresh many times
            // but as the SyncManager ensure the unicity of the synchronisation
            // there is no need to.
            refresh()
        }

        override fun onItemAtFrontLoaded(itemAtFront: T) {
            // nothing to do
        }

        override fun onItemAtEndLoaded(itemAtEnd: T) {
            // nothing to do
        }
    }
}
