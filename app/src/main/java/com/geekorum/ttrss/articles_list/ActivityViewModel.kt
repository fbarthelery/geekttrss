/**
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2018 by Frederic-Charles Barthelery.
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
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.geekorum.geekdroid.accounts.SyncInProgressLiveData
import com.geekorum.geekdroid.app.lifecycle.EmptyEvent
import com.geekorum.geekdroid.app.lifecycle.Event
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.geekdroid.app.lifecycle.EmptyEvent.Companion.makeEmptyEvent as RefreshEvent

/**
 * [ViewModel] for the [ArticleListActivity]
 */
class ActivityViewModel : ViewModel() {
    private val account = MutableLiveData<Account>()
    private val _selectedFeed = MutableLiveData<Feed>()
    val selectedFeed: LiveData<Feed> = _selectedFeed
    private val _refreshEvent = MutableLiveData<EmptyEvent>()
    val refreshEvent: LiveData<EmptyEvent> = _refreshEvent
    private val _articleSelectedEvent = MutableLiveData<Event<ArticleSelectedParameters>>()
    val articleSelectedEvent: LiveData<Event<ArticleSelectedParameters>> = _articleSelectedEvent

    val isRefreshing: LiveData<Boolean> = Transformations.switchMap(account) {
        SyncInProgressLiveData(it, ArticlesContract.AUTHORITY)
    }

    fun setAccount(account: Account) {
        this.account.value = account
    }

    fun setSelectedFeed(feed: Feed) {
        _selectedFeed.value = feed
    }

    fun refresh() {
        _refreshEvent.value = RefreshEvent()
    }

    fun displayArticle(position: Int, article: Article) {
        _articleSelectedEvent.value = ArticleSelectedEvent(position, article)
    }

    class ArticleSelectedParameters internal constructor(val position: Int, val article: Article)

    @Suppress("FunctionName")
    private fun ArticleSelectedEvent(position: Int, article: Article) =
        Event(ArticleSelectedParameters(position, article))

}
