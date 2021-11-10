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

import android.accounts.Account
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.geekorum.geekdroid.app.lifecycle.Event
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.network.TtRssBrowserLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val STATE_FEED_ID = "feed_id"
private const val STATE_ACCOUNT = "account"
private const val STATE_NEED_UNREAD = "need_unread"
private const val STATE_SORT_ORDER = "sort_order" // most_recent_first, oldest_first

private const val PREF_VIEW_MODE = "view_mode"
private const val PREF_SORT_ORDER = "sort_order"

/**
 * [ViewModel] for the [ArticleListActivity]
 */
@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val browserLauncher: TtRssBrowserLauncher,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val onSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            PREF_VIEW_MODE -> updateNeedUnread()
            PREF_SORT_ORDER -> updateSortOrder()
        }
    }

    private val _articleSelectedEvent = MutableLiveData<Event<ArticleSelectedParameters>>()
    val articleSelectedEvent: LiveData<Event<ArticleSelectedParameters>> = _articleSelectedEvent

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    private val _refreshClickedEvent = MutableLiveData<Event<Any>>()
    val refreshClickedEvent: LiveData<Event<Any>> = _refreshClickedEvent

    val mostRecentSortOrder = state.getLiveData<String>(STATE_SORT_ORDER).map {
        when (it) {
            "most_recent_first" -> true
            "oldest_first" -> false
            else -> false
        }
    }

    val onlyUnreadArticles = state.getLiveData(STATE_NEED_UNREAD, true)

    var appBarHeight: Int by mutableStateOf(0)

    init {
        browserLauncher.warmUp()
        prefs.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        updateNeedUnread()
        updateSortOrder()
    }

    fun setAccount(account: Account) {
        state[STATE_ACCOUNT] = account
    }

    fun setSelectedFeed(feed: Feed) {
        state[STATE_FEED_ID] = feed.id
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

    fun setSortByMostRecentFirst(mostRecentFirst: Boolean) {
        if (mostRecentFirst) {
            prefs.edit().putString(PREF_SORT_ORDER, "most_recent_first").apply()
        } else {
            prefs.edit().putString(PREF_SORT_ORDER, "oldest_first").apply()
        }
    }

    fun setNeedUnread(needUnread: Boolean) {
        if (needUnread) {
            prefs.edit().putString(PREF_VIEW_MODE, "adaptive").apply()
        } else {
            prefs.edit().putString(PREF_VIEW_MODE, "all").apply()
        }
    }

    private fun updateNeedUnread() {
        state[STATE_NEED_UNREAD] = when (prefs.getString(PREF_VIEW_MODE, "adaptive")) {
            "unread", "adaptive" -> true
            else -> false
        }
    }

    private fun updateSortOrder() {
        state[STATE_SORT_ORDER] = prefs.getString(PREF_SORT_ORDER, "most_recent_first")
    }

    override fun onCleared() {
        browserLauncher.shutdown()
        prefs.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }

    data class ArticleSelectedParameters(val position: Int, val article: Article)

    @Suppress("FunctionName")
    private fun ArticleSelectedEvent(position: Int, article: Article) =
        Event(ArticleSelectedParameters(position, article))

}
