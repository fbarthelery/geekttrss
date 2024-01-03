/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.app_reviews

import android.app.Activity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.datetime.Clock
import javax.inject.Inject


@HiltViewModel
class AppReviewViewModel @Inject constructor(
    private val appReviewStateManager: AppReviewStateManager,
    private val reviewManager: AppReviewManager
) : ViewModel() {

    init {
        if (appReviewStateManager.canAskForReview) {
            reviewManager.warm()
        }
    }

    fun launchReview(activity: Activity) {
        val lastReviewRequestTimeStamp = appReviewStateManager.lastReviewRequestTimestamp
        if (appReviewStateManager.canAskForReview && lastReviewRequestTimeStamp == null) { // first time asking, wait a week
            appReviewStateManager.lastReviewRequestTimestamp = Clock.System.now()
            return
        }
        if (appReviewStateManager.canAskForReview) {
            appReviewStateManager.lastReviewRequestTimestamp = Clock.System.now()
            reviewManager.launchReview(activity)
        }
    }
}
