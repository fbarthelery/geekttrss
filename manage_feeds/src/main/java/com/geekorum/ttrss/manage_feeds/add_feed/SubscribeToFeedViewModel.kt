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
package com.geekorum.ttrss.manage_feeds.add_feed

import android.accounts.Account
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.geekorum.geekdroid.app.lifecycle.Event
import com.geekorum.ttrss.htmlparsers.FeedInformation
import com.geekorum.ttrss.manage_feeds.workers.SubscribeWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import timber.log.Timber
import javax.inject.Inject


class SubscribeToFeedViewModel @Inject constructor(
    private val feedsFinder: FeedsFinder,
    private val workManager: WorkManager,
    private val account: Account
) : ViewModel() {

    private val _feedsFound = MutableLiveData<List<FeedsFinder.FeedResult>?>()
    val feedsFound = _feedsFound as LiveData<List<FeedsFinder.FeedResult>?>

    private val _invalidUrlError = MutableLiveData<Event<String>>()
    val invalidUrlEvent: LiveData<Event<String>> = _invalidUrlError

    var urlTyped: String = ""

    internal var selectedFeed: FeedInformation? = null

    fun submitUrl(urlString: String) = viewModelScope.launch {
        val url = checkUrl(urlString) ?: return@launch

        val feeds = withContext(Dispatchers.IO) {
            feedsFinder.findFeeds(url)
        }

        when {
            feeds.isEmpty() -> TODO("show error")
            else ->  {
                _feedsFound.value = feeds.toList()
            }
        }
    }

    internal fun checkUrl(url: String): HttpUrl? {
        return HttpUrl.parse(url)
            .also {
                if (it == null) {
                    _invalidUrlError.value = Event(url)
                }
            }
    }


    fun subscribeToFeed(feedInfo: FeedInformation) {
        subscribeToFeed(account, feedInfo.href)
    }

    private fun subscribeToFeed(
        account: Account, feedUrl: String,
        categoryId: Long = 0,
        feedLogin: String = "",
        feedPassword: String = ""
    ) {
        val inputData = SubscribeWorker.getInputData(account, feedUrl, categoryId, feedLogin, feedPassword)
        val workRequest = OneTimeWorkRequestBuilder<SubscribeWorker>()
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())
            .setInputData(inputData)
            .build()

        workManager.enqueue(workRequest)
    }

    fun setSelectedFeed(feed: Any) {
        selectedFeed = feed as FeedInformation
    }

    fun resetAvailableFeeds() {
        _feedsFound.value = null
        selectedFeed = null
    }

}
