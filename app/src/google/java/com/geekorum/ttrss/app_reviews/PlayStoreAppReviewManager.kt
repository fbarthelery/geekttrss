/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2023 by Frederic-Charles Barthelery.
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
import android.content.Context
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.migration.DisableInstallInCheck
import kotlinx.coroutines.*
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class PlayStoreAppReviewManager(
    private val reviewManager: ReviewManager,
    private val coroutineDispatchersProvider: CoroutineDispatchersProvider
) : AppReviewManager {

    private var reviewInfoDeferred: Deferred<ReviewInfo>? = null

    private val scope = CoroutineScope(coroutineDispatchersProvider.io)

    override fun warm() {
        if (reviewInfoDeferred == null) {
            reviewInfoDeferred = scope.async {
                reviewManager.requestReview()
            }
        }
    }

    private fun getReviewInfo() : ReviewInfo? {
        // only returns the reviewInfo if it has been warmed to not add delay in ux
        return reviewInfoDeferred
            ?.takeIf { it.isCompleted && it.getCompletionExceptionOrNull() == null }
            ?.getCompleted()
    }

    override fun launchReview(activity: Activity) {
        scope.launch(coroutineDispatchersProvider.main) {
            val reviewInfo = getReviewInfo() ?: return@launch
            Timber.i("Launch app review")
            reviewManager.launchReview(activity, reviewInfo)
        }
    }

    override fun release() {
        scope.cancel()
    }
}

@Module
@DisableInstallInCheck
object PlayStoreAppReviewModule {

    @Provides
    fun providesAppReviewManager(reviewManager: ReviewManager, coroutineDispatchersProvider: CoroutineDispatchersProvider): AppReviewManager {
        return PlayStoreAppReviewManager(reviewManager, coroutineDispatchersProvider)
    }

    @Provides
    fun providesReviewManager(@ApplicationContext context: Context): ReviewManager {
        return ReviewManagerFactory.create(context)
    }

}
