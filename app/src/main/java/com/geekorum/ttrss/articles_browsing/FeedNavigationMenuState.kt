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
package com.geekorum.ttrss.articles_browsing

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
    val railState: WideNavigationRailState,
    val virtualFeeds: Flow<List<Feed>>,
    val feeds: Flow<List<FeedWithFavIcon>>,
    val selectedItem: SelectedItem,
) {

    val isMenuExpanded: Boolean
        get() = railState.targetValue.isExpanded

    fun toggleMenu() = coroutineScope.launch {
        railState.toggle()
    }

    val isMagazineSelected: Boolean = selectedItem == MagazineSelectedItem

    val isSettingsSelected: Boolean = selectedItem == SettingsSelectedItem

    fun isFeedSelected(feed: Feed): Boolean {
        return  feed.id == (selectedItem as? FeedSelectedItem)?.feedId
    }

    fun closeMenu(doNext: () -> Unit = {}) {
        coroutineScope.launch {
            railState.collapse()
        }
        doNext()
    }

    sealed class SelectedItem
    data object MagazineSelectedItem: SelectedItem()
    data object SettingsSelectedItem: SelectedItem()
    data class FeedSelectedItem(val feedId: Long): SelectedItem()
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberFeedNavigationMenuState(
    feeds: Flow<List<FeedWithFavIcon>>,
    virtualFeeds: Flow<List<Feed>>,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    railState: WideNavigationRailState = rememberWideNavigationRailState(),
    selectedItem: FeedNavigationMenuState.SelectedItem
): FeedNavigationMenuState {
    return remember(coroutineScope, railState, virtualFeeds, feeds, selectedItem) {
        FeedNavigationMenuState(
            coroutineScope = coroutineScope,
            railState = railState,
            virtualFeeds = virtualFeeds,
            feeds = feeds,
            selectedItem = selectedItem,
        )
    }
}