/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
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
import androidx.lifecycle.*
import com.geekorum.geekdroid.app.lifecycle.Event
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticlesSearchHistoryRepository
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.network.TtRssBrowserLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STATE_FEED_ID = "feed_id"
private const val STATE_ACCOUNT = "account"

/**
 * [ViewModel] for the [ArticleListActivity]
 */
@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val browserLauncher: TtRssBrowserLauncher,
    private val articlesListPreferencesRepository: ArticlesListPreferencesRepository,
    private val articlesSearchHistoryRepository: ArticlesSearchHistoryRepository,
) : ViewModel() {

    private val account = MutableStateFlow<Account?>(null)

    private val _articleSelectedEvent = MutableLiveData<Event<ArticleSelectedParameters>>()
    val articleSelectedEvent: LiveData<Event<ArticleSelectedParameters>> = _articleSelectedEvent

    private val _refreshClickedEvent = MutableLiveData<Event<Any>>()
    val refreshClickedEvent: LiveData<Event<Any>> = _refreshClickedEvent

    // we need to delay history update until the search screen results are displayed
    // otherwise the suggestions list will update during the transition
    private var delayHistoryUpdate = false
    val articlesSearchHistory = articlesSearchHistoryRepository.searchHistory
        .onEach {
            if (delayHistoryUpdate) {
                delay(1000)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostRecentSortOrder = articlesListPreferencesRepository.getSortOrder().map {
        when (it) {
            "most_recent_first" -> true
            "oldest_first" -> false
            else -> false
        }
    }

    val sortOrder = mostRecentSortOrder.map { mostRecent ->
            if (mostRecent) SortOrder.MOST_RECENT_FIRST else SortOrder.OLDEST_FIRST
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortOrder.OLDEST_FIRST)

    val onlyUnreadArticles = articlesListPreferencesRepository.getViewMode().map {
        when (it) {
            "unread", "adaptive" -> true
            else -> false
        }
    }

    val displayCompactItems = articlesListPreferencesRepository.getDisplayCompactArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _undoReadSnackBarMessage = MutableStateFlow<UndoReadSnackbarMessage?>(null)
    val undoReadSnackBarMessage = _undoReadSnackBarMessage.asStateFlow()

    private val _isScrollingUp = MutableStateFlow(true)
    val isScrollingUp = _isScrollingUp.asStateFlow()

    val browserIcon = browserLauncher.browserIcon.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        browserLauncher.warmUp()
    }

    fun setAccount(account: Account) {
        this.account.value = account
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

    fun recordSearchQueryInHistory(query: String) = viewModelScope.launch {
        try {
            delayHistoryUpdate = true
            articlesSearchHistoryRepository.recordSearchQuery(query)
        } finally {
            delayHistoryUpdate = false
        }
    }

    fun setSortByMostRecentFirst(mostRecentFirst: Boolean) {
        articlesListPreferencesRepository.setSortByMostRecentFirst(mostRecentFirst)
    }

    fun setNeedUnread(needUnread: Boolean) {
        if (needUnread) {
            articlesListPreferencesRepository.setViewMode("adaptive")
        } else {
            articlesListPreferencesRepository.setViewMode("all")
        }
    }

    override fun onCleared() {
        browserLauncher.shutdown()
    }

    data class ArticleSelectedParameters(val position: Int, val article: Article)

    @Suppress("FunctionName")
    private fun ArticleSelectedEvent(position: Int, article: Article) =
        Event(ArticleSelectedParameters(position, article))

    fun setUndoReadSnackBarMessage(snackBarMessage: UndoReadSnackbarMessage?) {
        _undoReadSnackBarMessage.value = snackBarMessage
    }

    fun setIsScrollingUp(up: Boolean) {
        _isScrollingUp.value = up
    }
}

