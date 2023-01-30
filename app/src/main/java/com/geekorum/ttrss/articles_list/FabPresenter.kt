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
package com.geekorum.ttrss.articles_list

import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.NavController
import com.geekorum.geekdroid.views.behaviors.ScrollAwareFABBehavior
import com.geekorum.ttrss.R
import com.geekorum.ttrss.on_demand_modules.OnDemandModuleNavHostProgressDestinationProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Controls the behavior of the FloatingActionButton
 */
internal class FabPresenter(
    private val fab: FloatingActionButton,
    private val onDemandModuleNavHostProgressDestinationProvider: OnDemandModuleNavHostProgressDestinationProvider,
    private val navController: NavController
){

    private val fabBehavior: ScrollAwareFABBehavior? =
        (fab.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? ScrollAwareFABBehavior

    init {
        setup()
    }

    private fun setup() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val progressDestinationId = onDemandModuleNavHostProgressDestinationProvider.progressDestinationId
            when (destination.id) {
                progressDestinationId,
                R.id.articlesSearchFragment -> {
                    setFabBehavior(null)
                    fab.hide()
                }
                else -> {
                    setFabBehavior(fabBehavior)
                    fab.show()
                }
            }
        }
    }

    private fun setFabBehavior(behavior: ScrollAwareFABBehavior?) {
        (fab.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior = behavior
    }

}
