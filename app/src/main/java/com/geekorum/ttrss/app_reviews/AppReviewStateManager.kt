/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import javax.inject.Inject
import kotlin.time.ExperimentalTime


const val PREF_LAST_REVIEW_REQUEST_TIMESTAMP = "last_review_request_timestamp"

class AppReviewStateManager @Inject constructor(
    private val appPreferences: SharedPreferences
) {
    var lastReviewRequestTimestamp: Instant?
        get() = appPreferences.getString(PREF_LAST_REVIEW_REQUEST_TIMESTAMP, null)?.toInstant()
        set(value) = appPreferences.edit { putString(PREF_LAST_REVIEW_REQUEST_TIMESTAMP, value?.toString()) }

    val canAskForReview: Boolean
    get() {
        return lastReviewRequestTimestamp?.let { lastReviewRequestTimestamp ->
            val timePastLastReview = Clock.System.now() - (lastReviewRequestTimestamp)
            timePastLastReview.inWholeDays > 7
        } ?: true
    }
}
