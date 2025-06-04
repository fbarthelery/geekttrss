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
package com.geekorum.ttrss.articles_browsing

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.FeedWithFavIcon
import com.geekorum.ttrss.ui.components.isExpanded
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Stable
class FeedNavigationMenuState(
    private val coroutineScope: CoroutineScope,
    private val navController: NavHostController,
    val railState: WideNavigationRailState,
    val virtualFeeds: Flow<List<Feed>>,
    val feeds: Flow<List<FeedWithFavIcon>>,
) {

    val isMenuExpanded: Boolean
        get() = railState.targetValue.isExpanded

    fun toggleMenu() = coroutineScope.launch {
        railState.toggle()
    }

    val isMagazineSelected: Boolean
        get() = false //TODO

    val isSettingsSelected: Boolean
        get() = false //TODO


    fun closeMenu(doNext: () -> Unit = {}) {
        coroutineScope.launch {
            railState.collapse()
        }
        doNext()
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberFeedNavigationMenuState(
    feeds: Flow<List<FeedWithFavIcon>>,
    virtualFeeds: Flow<List<Feed>>,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
    railState: WideNavigationRailState = rememberWideNavigationRailState(),
): FeedNavigationMenuState {
    return remember(coroutineScope, navController, railState, virtualFeeds, feeds) {
        FeedNavigationMenuState(
            coroutineScope = coroutineScope,
            navController = navController,
            railState = railState,
            virtualFeeds = virtualFeeds,
            feeds = feeds,
        )
    }
}