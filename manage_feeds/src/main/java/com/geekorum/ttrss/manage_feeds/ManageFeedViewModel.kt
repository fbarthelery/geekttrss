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
package com.geekorum.ttrss.manage_feeds

import android.accounts.Account
import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.geekorum.geekdroid.app.lifecycle.Event
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.ManageFeedsDao
import com.geekorum.ttrss.manage_feeds.workers.UnsubscribeWorker
import kotlinx.coroutines.launch

/**
 * ViewModel to manage feeds
 */
class ManageFeedViewModel @ViewModelInject constructor(
    private val application: Application,
    private val account: Account,
    private val feedsDao: ManageFeedsDao
): ViewModel() {

    private val _feedClickedEvent = MutableLiveData<Event<Feed>>()
    val feedClickedEvent: LiveData<Event<Feed>> = _feedClickedEvent

    val feeds: LiveData<PagedList<Feed>> by lazy {
        LivePagedListBuilder(feedsDao.allSubscribedFeeds, 40).build()
    }

    fun unsubscribeFeed(feedId: Long) {
        viewModelScope.launch {
            feedsDao.updateIsSubscribedFeed(feedId, false)
        }
        val workManager = WorkManager.getInstance(application)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<UnsubscribeWorker>()
            .setConstraints(constraints)
            .setInputData(UnsubscribeWorker.getInputData(account, feedId))
            .build()
        workManager.enqueue(request)
    }

    fun onFeedClicked(feed: Feed) {
        _feedClickedEvent.value = Event(feed)
    }
}

