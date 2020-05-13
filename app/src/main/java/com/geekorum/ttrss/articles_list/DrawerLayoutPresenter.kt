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
package com.geekorum.ttrss.articles_list

import android.view.View
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.addCallback
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.dynamicfeatures.DynamicGraphNavigator
import com.geekorum.ttrss.R

/**
 * Controls the behavior of the [DrawerLayout]
 */
internal class DrawerLayoutPresenter(
    private val drawerLayout: DrawerLayout,
    private val navController: NavController,
    private val onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner
) {

    init {
        setup()
    }

    private fun setup() {
        navController.addOnDestinationChangedListener { controller, destination, _ ->
            drawerLayout.closeDrawers()
            val progressDestinationId = (controller.graph as? DynamicGraphNavigator.DynamicNavGraph)?.progressDestination ?: 0
            val lockMode = when (destination.id) {
                R.id.articlesSearchFragment,
                progressDestinationId -> DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                else -> DrawerLayout.LOCK_MODE_UNLOCKED
            }
            drawerLayout.setDrawerLockMode(lockMode)
        }

        val backPressedCallback = onBackPressedDispatcherOwner.onBackPressedDispatcher.addCallback(onBackPressedDispatcherOwner) {
            drawerLayout.closeDrawers()
        }.apply {
            isEnabled = drawerLayout.isOpen
        }

        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerClosed(drawerView: View) {
                backPressedCallback.isEnabled = false
            }

            override fun onDrawerOpened(drawerView: View) {
                backPressedCallback.isEnabled = true
            }
        })
    }
}
