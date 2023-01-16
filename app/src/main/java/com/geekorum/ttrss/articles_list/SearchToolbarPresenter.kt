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

import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.dynamicfeatures.DynamicGraphNavigator
import com.geekorum.ttrss.R

/**
 * Presenter for the search toolbar widget
 */
internal class SearchToolbarPresenter(
    private val searchItem: MenuItem,
    private val navController: NavController,
    private val activityViewModel: ActivityViewModel
) {

    init {
        setup()
    }

    private fun setup() {
        navController.addOnDestinationChangedListener { controller, destination, _ ->
            if (destination.id != R.id.articlesSearchFragment && searchItem.isActionViewExpanded) {
                searchItem.collapseActionView()
            }
            val progressDestinationId = (controller.graph as? DynamicGraphNavigator.DynamicNavGraph)?.progressDestination ?: 0
            val isOnProgress = destination.id == progressDestinationId
            searchItem.isVisible = !isOnProgress
            searchItem.isEnabled = !isOnProgress
        }

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                navController.navigate(ArticlesListFragmentDirections.actionSearchArticle())
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                if (navController.currentDestination?.id == R.id.articlesSearchFragment)
                    navController.popBackStack()
                return true
            }
        })

        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                activityViewModel.setSearchQuery(query)
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                activityViewModel.setSearchQuery(query)
                return true
            }
        })
    }
}
