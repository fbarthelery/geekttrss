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
package com.geekorum.ttrss.add_feed

import android.accounts.Account
import com.geekorum.ttrss.network.ApiService
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class AddFeedJob @AssistedInject internal constructor(
    val account: Account,
    val apiService: ApiService,
    @Assisted val feedUrl: String,
    @Assisted val categoryId: Long,
    @Assisted val feedLogin: String,
    @Assisted val feedPassword: String
) {
    suspend fun addFeed(): Boolean {
        return apiService.subscribeToFeed(feedUrl, categoryId, feedLogin, feedPassword)
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(feedUrl: String, categoryId: Long, feedLogin: String, feedPassword: String): AddFeedJob
    }

}
