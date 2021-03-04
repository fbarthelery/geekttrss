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
package com.geekorum.ttrss.manage_feeds.add_feed

import android.accounts.Account
import android.accounts.AccountManager
import androidx.annotation.VisibleForTesting
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.geekorum.geekdroid.accounts.AccountsLiveData
import com.geekorum.geekdroid.app.lifecycle.EmptyEvent
import com.geekorum.ttrss.accounts.AccountAuthenticator
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.manage_feeds.add_feed.FeedsFinder.FeedResult
import com.geekorum.ttrss.manage_feeds.workers.SubscribeWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import java.io.IOException
import javax.inject.Inject
import com.geekorum.geekdroid.app.lifecycle.EmptyEvent.Companion.makeEmptyEvent as CompleteEvent

/**
 * [ViewModel] to subscribe to a Feed
 */
@HiltViewModel
class AddFeedViewModel @Inject constructor(
    private val dispatchers: CoroutineDispatchersProvider,
    private val feedsFinder: FeedsFinder,
    private val workManager: WorkManager,
    accountManager: AccountManager
) : ViewModel() {

    private val _availableFeeds = MutableLiveData<Collection<FeedResult>>()
    val availableFeeds: LiveData<Collection<FeedResult>> = _availableFeeds
    val accounts = AccountsLiveData(accountManager, AccountAuthenticator.TTRSS_ACCOUNT_TYPE)
    private val _completeEvent = MutableLiveData<EmptyEvent>()
    val complete: LiveData<EmptyEvent> = _completeEvent
    val canSubscribe = ObservableBoolean(false)

    internal var selectedFeed: FeedResult? = null
        set(value) {
            field = value
            canSubscribe.set(value != null  && selectedAccount != null)
        }

    internal var selectedAccount: Account? = null
        set(value) {
            field = value
            canSubscribe.set(value != null  && selectedFeed != null)
        }


    private val accountObserver = Observer<Array<Account>> {
        val accounts = checkNotNull(it)
        selectedAccount = accounts.singleOrNull()
    }

    init {
        accounts.observeForever(accountObserver)
    }

    override fun onCleared() {
        super.onCleared()
        accounts.removeObserver(accountObserver)
    }

    fun init(documentUrl: HttpUrl?) = viewModelScope.launch {
        if (documentUrl != null) {
            initWithUrl(documentUrl)
        } else {
            _availableFeeds.value = emptyList()
        }
    }

    @VisibleForTesting
    internal suspend fun initWithUrl(documentUrl: HttpUrl) {
        try {
            val feeds = withContext(dispatchers.io) {
                feedsFinder.findFeeds(documentUrl)
            }

            _availableFeeds.value = feeds
        } catch (exception: IOException) {
            _availableFeeds.value = emptyList()
        }
    }

    fun subscribeToFeed() {
        val feed = checkNotNull(selectedFeed)
        val account = checkNotNull(selectedAccount)
        subscribeToFeed(account, feed.href, 0, "", "")
        _completeEvent.value = CompleteEvent()
    }

    fun cancel() {
        _completeEvent.value = CompleteEvent()
    }

    fun setSelectedFeed(feed: Any) {
        selectedFeed = feed as FeedResult
    }

    fun setSelectedAccount(account: Any) {
        selectedAccount = account as Account
    }

    private fun subscribeToFeed(
        account: Account, feedUrl: String,
        categoryId: Long, feedLogin: String, feedPassword: String
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

}
