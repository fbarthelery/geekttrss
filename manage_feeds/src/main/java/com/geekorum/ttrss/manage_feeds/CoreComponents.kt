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
package com.geekorum.ttrss.manage_feeds

import androidx.lifecycle.ViewModelProvider
import com.geekorum.ttrss.applicationComponent
import com.geekorum.ttrss.session.SessionActivity

open class BaseSessionActivity : SessionActivity() {
    private lateinit var activityComponent: ActivityComponent

    override fun inject() {
        val manageFeedComponent = DaggerManageFeedComponent.builder()
            .manageFeedsDependencies(applicationComponent)
            .build()
        activityComponent = manageFeedComponent
            .createActivityComponent()
            .newComponent(this)
        activityComponent.inject(this)
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return activityComponent.hiltViewModelFactoryFactory.fromActivity(this,
            super.getDefaultViewModelProviderFactory())
    }
}
