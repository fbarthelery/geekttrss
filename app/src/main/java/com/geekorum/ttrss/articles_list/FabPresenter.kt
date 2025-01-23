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
package com.geekorum.ttrss.articles_list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import com.geekorum.ttrss.R

/**
 * Controls the behavior of the FloatingActionButton
 */
internal class FabPresenter(
    private val navController: NavController
){

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun Content(isScrollingUpOrRest: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
        //TODO deal with module install
        val navBackStackEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(null)
        val fabVisibleInDestination = when {
            navBackStackEntry?.destination?.hasRoute<NavRoutes.Search>() == true -> false
            navBackStackEntry == null -> false
            else -> true
        }
        val fabVisible = fabVisibleInDestination && isScrollingUpOrRest

        FloatingActionButton(
            modifier = modifier.animateFloatingActionButton(visible = fabVisible, alignment = Alignment.BottomEnd),
            onClick = onClick
        ) {
            Icon(Icons.Default.Refresh, stringResource(R.string.btn_refresh))
        }
    }
}
