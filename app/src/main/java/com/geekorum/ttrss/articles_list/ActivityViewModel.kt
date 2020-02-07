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
import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.geekorum.geekdroid.app.lifecycle.Event
import com.geekorum.geekdroid.dagger.ViewModelAssistedFactory
import com.geekorum.ttrss.background_job.BackgroundJobManager
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.network.TtRssBrowserLauncher
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

private const val STATE_FEED_ID = "feed_id"
private const val STATE_ACCOUNT = "account"

/**
 * [ViewModel] for the [ArticleListActivity]
 */
class ActivityViewModel @AssistedInject constructor(
    @Assisted private val state: SavedStateHandle,
    private val feedsRepository: FeedsRepository,
    private val backgroundJobManager: BackgroundJobManager,
    private val browserLauncher: TtRssBrowserLauncher
) : ViewModel() {
    val selectedFeed: LiveData<Feed?> = state.getLiveData(STATE_FEED_ID, Feed.FEED_ID_ALL_ARTICLES).apply{
        // workaround for out of sync values see
        // https://issuetracker.google.com/issues/129989646
        value = value
    }.switchMap {
        feedsRepository.getFeedById(it)
    }

    private val _feedSelectedEvent = MutableLiveData<Event<Feed>>()
    val feedSelectedEvent: LiveData<Event<Feed>> = _feedSelectedEvent

    private val _articleSelectedEvent = MutableLiveData<Event<ArticleSelectedParameters>>()
    val articleSelectedEvent: LiveData<Event<ArticleSelectedParameters>> = _articleSelectedEvent

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    private val _refreshClickedEvent = MutableLiveData<Event<Any>>()
    val refreshClickedEvent: LiveData<Event<Any>> = _refreshClickedEvent

    init {
        browserLauncher.warmUp()
    }

    fun setAccount(account: Account) {
        state[STATE_ACCOUNT] = account
    }

    fun setSelectedFeed(feed: Feed) {
        state[STATE_FEED_ID] = feed.id
        _feedSelectedEvent.value = Event(feed)
    }

    fun refresh() {
        _refreshClickedEvent.value = Event(Any())
    }

    fun displayArticle(position: Int, article: Article) {
        _articleSelectedEvent.value = ArticleSelectedEvent(position, article)
    }

    fun displayArticleInBrowser(context: Context, article: Article) {
        browserLauncher.launchUrl(context, article.link.toUri())
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
