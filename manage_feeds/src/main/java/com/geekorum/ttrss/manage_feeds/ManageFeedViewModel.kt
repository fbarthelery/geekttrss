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
package com.geekorum.ttrss.manage_feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.geekorum.ttrss.data.FeedWithFavIcon
import com.geekorum.ttrss.data.ManageFeedsDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel to manage feeds
 */
@HiltViewModel
class ManageFeedViewModel @Inject constructor(
    private val feedsDao: ManageFeedsDao
): ViewModel() {

    val feeds: Flow<PagingData<FeedWithFavIcon>> by lazy {
        Pager(PagingConfig(40)) {
            feedsDao.getAllSubscribedFeeds()
        }.flow
    }

    val subscribedFeedsByCategories = feedsDao
        .getSubscribedFeedsByCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

}

