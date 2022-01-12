/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2021 by Frederic-Charles Barthelery.
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
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck


interface AppReviewManager {

    /**
     * Attempt to preload review information to void delay when calling [launchReview]
     */
    fun warm()

    /**
     * Launch the App review UI. It is not guaranteed that the UI will be launch as this is
     * at the discretion of the App review implementation
     */
    fun launchReview(activity: Activity)
}


class NoAppReviewManager : AppReviewManager {
    override fun warm() {
        // Do nothing
    }

    override fun launchReview(activity: Activity) {
        // Do nothing
    }
}

@Module
@DisableInstallInCheck
object NoAppReviewModule {

    @Provides
    fun providesAppReviewManager(): AppReviewManager = NoAppReviewManager()

}
