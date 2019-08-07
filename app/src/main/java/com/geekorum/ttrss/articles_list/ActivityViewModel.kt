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
import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.geekorum.geekdroid.accounts.SyncInProgressLiveData
import com.geekorum.geekdroid.app.lifecycle.EmptyEvent
import com.geekorum.geekdroid.app.lifecycle.Event
import com.geekorum.geekdroid.dagger.ViewModelAssistedFactory
import com.geekorum.ttrss.background_job.BackgroundJobManager
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.network.TtRssBrowserLauncher
import com.geekorum.ttrss.providers.ArticlesContract
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.geekorum.geekdroid.app.lifecycle.EmptyEvent.Companion.makeEmptyEvent as SearchClosedEvent
import com.geekorum.geekdroid.app.lifecycle.EmptyEvent.Companion.makeEmptyEvent as SearchOpenedEvent

private const val STATE_FEED_ID = "feed_id"

/**
 * [ViewModel] for the [ArticleListActivity]
 */
class ActivityViewModel @AssistedInject constructor(
    @Assisted private val state: SavedStateHandle,
    private val feedsRepository: FeedsRepository,
    private val backgroundJobManager: BackgroundJobManager,
    private val browserLauncher: TtRssBrowserLauncher
) : ViewModel() {
    private val account = MutableLiveData<Account>()
    val selectedFeed: LiveData<Feed?> =  state.getLiveData(STATE_FEED_ID, Feed.FEED_ID_ALL_ARTICLES).switchMap {
        feedsRepository.getFeedById(it)
    }

    private val _feedSelectedEvent = MutableLiveData<Event<Long>>()
    val feedSelectedEvent: LiveData<Event<Long>> = _feedSelectedEvent

    private val _articleSelectedEvent = MutableLiveData<Event<ArticleSelectedParameters>>()
    val articleSelectedEvent: LiveData<Event<ArticleSelectedParameters>> = _articleSelectedEvent

    private val _searchOpenedEvent = MutableLiveData<EmptyEvent>()
    val searchOpenedEvent: LiveData<EmptyEvent> = _searchOpenedEvent

    private val _searchClosedEvent = MutableLiveData<EmptyEvent>()
    val searchClosedEvent: LiveData<EmptyEvent> = _searchClosedEvent

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    val isRefreshing: LiveData<Boolean> = Transformations.switchMap(account) {
        SyncInProgressLiveData(it, ArticlesContract.AUTHORITY)
    }

    init {
        browserLauncher.warmUp()
    }

    fun setAccount(account: Account) {
        this.account.value = account
    }

    fun setSelectedFeed(id: Long) {
        state[STATE_FEED_ID] = id
        _feedSelectedEvent.value = Event(id)
    }

    fun refresh() {
        //TODO state.getLiveData(key, value) should save the value but it doesn't. Look for a bug report
        val feedId: Long? = state[STATE_FEED_ID]
        if (feedId == null || Feed.isVirtualFeed(feedId)) {
            backgroundJobManager.refresh(account.value!!)
        } else {
            backgroundJobManager.refreshFeed(account.value!!, feedId)
        }
    }

    fun displayArticle(position: Int, article: Article) {
        _articleSelectedEvent.value = ArticleSelectedEvent(position, article)
    }

    fun displayArticleInBrowser(context: Context, article: Article) {
        browserLauncher.launchUrl(context, article.link.toUri())
    }

    fun onSearchOpened() {
        _searchOpenedEvent.value = SearchOpenedEvent()
    }

    fun onSearchClosed() {
        _searchClosedEvent.value = SearchClosedEvent()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    override fun onCleared() {
        browserLauncher.shutdown()
    }

    data class ArticleSelectedParameters(val position: Int, val article: Article)

    @Suppress("FunctionName")
    private fun ArticleSelectedEvent(position: Int, article: Article) =
        Event(ArticleSelectedParameters(position, article))


    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<ActivityViewModel> {
        override fun create(state: SavedStateHandle): ActivityViewModel
    }
}
