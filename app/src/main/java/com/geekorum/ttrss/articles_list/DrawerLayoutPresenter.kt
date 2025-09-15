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
package com.geekorum.ttrss.articles_list

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute

/**
 * Controls the behavior of the NavigationDrawer
 */
@Stable
internal class DrawerLayoutPresenter {

    var drawerGesturesEnabled by mutableStateOf(true)
        internal set
}

@Composable
internal fun rememberDrawerLayoutPresenter(navController: NavController): DrawerLayoutPresenter {
    val presenter = remember {
        DrawerLayoutPresenter()
    }

    val navBackStackEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(null)
    LaunchedEffect(navBackStackEntry) {
        presenter.drawerGesturesEnabled = when {
            navBackStackEntry?.destination?.hasRoute<NavRoutes.Search>() == true -> false
            else -> true
        }
    }

    return presenter
}
