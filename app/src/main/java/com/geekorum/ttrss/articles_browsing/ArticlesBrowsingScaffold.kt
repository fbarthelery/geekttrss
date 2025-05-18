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

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.ui.AppTheme3
import com.geekorum.ttrss.ui.components.FeedNavigationRail
import com.geekorum.ttrss.ui.components.MagazineWideNavigationRailItem
import com.geekorum.ttrss.ui.components.ModalFeedNavigationRail
import com.geekorum.ttrss.ui.components.SettingsWideNavigationRailItem
import com.geekorum.ttrss.ui.components.VirtualFeedWideNavigationRailItem
import com.geekorum.ttrss.ui.components.isExpanded
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArticleBrowsingScaffold(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    railState: WideNavigationRailState = rememberWideNavigationRailState(),
    feedsNavigationMenu: @Composable FeedNavigationMenuScope.() -> Unit,
    content: @Composable () -> Unit
) {
    val compact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact || windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
    val medium = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
    val expanded = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded

    val feedNavigationMenuScope = remember(railState) { FeedNavigationMenuScopeImpl(railState) }
    feedsNavigationMenu.invoke(feedNavigationMenuScope)

    when {
        // check compact first because we want it based on height
        compact -> {
            CompactArticleBrowsingScaffold(
                feedNavigationMenuScopeImpl = feedNavigationMenuScope,
                content = content
            )
        }

        expanded -> {
            ExpandedArticleBrowsingScaffold(modifier = modifier,
                feedNavigationMenuScopeImpl = feedNavigationMenuScope,
                content = content
            )
        }

        medium -> {
            MediumArticleBrowsingScaffold(modifier = modifier,
                feedNavigationMenuScopeImpl = feedNavigationMenuScope,
                content = content
            )
        }
    }
}

@Composable
private fun DrawerStatusBarProtection(modifier: Modifier = Modifier) {
    Spacer(
        modifier
            .windowInsetsTopHeight(WindowInsets.systemBars)
            .width(DrawerDefaults.MaximumDrawerWidth)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.48f))
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CompactArticleBrowsingScaffold(
    feedNavigationMenuScopeImpl: FeedNavigationMenuScopeImpl,
    content: @Composable () -> Unit
) {
    ModalFeedNavigationRail(
        state = feedNavigationMenuScopeImpl.railState,
        hideOnCollapse = true,
        quickFeedAccess = feedNavigationMenuScopeImpl.quickFeed ?: {},
        feedSection = feedNavigationMenuScopeImpl.feedsSection ?: {},
        settingsSection = feedNavigationMenuScopeImpl.settingsSection ?: {},
        header = feedNavigationMenuScopeImpl.header
    )
    content()
}



@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediumArticleBrowsingScaffold(
    feedNavigationMenuScopeImpl: FeedNavigationMenuScopeImpl,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(modifier) {
        ModalFeedNavigationRail(
            state = feedNavigationMenuScopeImpl.railState,
            quickFeedAccess = feedNavigationMenuScopeImpl.quickFeed ?: {},
            feedSection = feedNavigationMenuScopeImpl.feedsSection ?: {},
            settingsSection = feedNavigationMenuScopeImpl.settingsSection ?: {},
            header = feedNavigationMenuScopeImpl.header
        )
        content()
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpandedArticleBrowsingScaffold(
    feedNavigationMenuScopeImpl: FeedNavigationMenuScopeImpl,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(modifier) {
        FeedNavigationRail(
            state = feedNavigationMenuScopeImpl.railState,
            quickFeedAccess = feedNavigationMenuScopeImpl.quickFeed ?: {},
            feedSection = feedNavigationMenuScopeImpl.feedsSection ?: {},
            settingsSection = feedNavigationMenuScopeImpl.settingsSection ?: {},
            header = feedNavigationMenuScopeImpl.header
        )
        content()
    }
}

@Stable
interface FeedNavigationMenuScope {

    fun header(content: @Composable () -> Unit)

    fun quickFeedAccess(content: @Composable () -> Unit)
    fun feedsSection(content: @Composable () -> Unit)
    fun settingsSection(content: @Composable () -> Unit)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private class FeedNavigationMenuScopeImpl(
    val railState: WideNavigationRailState
) : FeedNavigationMenuScope {
    var quickFeed by mutableStateOf<(@Composable () -> Unit)?>(null)
        private set
    var feedsSection by mutableStateOf<(@Composable () -> Unit)?>(null)
        private set
    var settingsSection by mutableStateOf<(@Composable () -> Unit)?>(null)
        private set
    var header by mutableStateOf<(@Composable () -> Unit)?>(null)

    override fun header(content: @Composable (() -> Unit)) {
        header = content
    }

    override fun quickFeedAccess(content: @Composable (() -> Unit)) {
        quickFeed = content
    }

    override fun feedsSection(content: @Composable (() -> Unit)) {
        feedsSection = content
    }

    override fun settingsSection(content: @Composable (() -> Unit)) {
        settingsSection = content
    }

}


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@PreviewScreenSizes
@Composable
private fun PreviewArticleBrowsingScaffold() {
    BoxWithConstraints {
        val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
        AppTheme3 {
            val coroutineScope = rememberCoroutineScope()
            val railState = rememberWideNavigationRailState()
            ArticleBrowsingScaffold(
                railState = railState,
                windowSizeClass = windowSizeClass,
                feedsNavigationMenu = {
                    header {
                        IconButton(
                            modifier = Modifier.padding(start = 24.dp),
                            onClick = {
                            coroutineScope.launch {
                                railState.toggle()
                            }
                        }) {
                            Icon(Icons.Default.Menu, null)
                        }
                    }
                    quickFeedAccess {
                        MagazineWideNavigationRailItem(
                            selected = false,
                            onClick = {}
                        )
                        val feed = Feed(
                            id = Feed.FEED_ID_ALL_ARTICLES,
                            title = "All articles",
                            unreadCount = 1290,
                        )
                        VirtualFeedWideNavigationRailItem(feed,
                            selected = false,
                            onClick = {},
                            railExpanded = railState.targetValue.isExpanded
                        )
                    }
                    settingsSection {
                        SettingsWideNavigationRailItem(
                            selected = false,
                            onClick = {},
                            railExpanded = railState.targetValue.isExpanded
                        )
                    }
                },
                content = {
                    Button(onClick = {
                        coroutineScope.launch {
                            railState.toggle()
                        }
                    }) {
                        Text("Toggle")
                    }
                }
            )
        }
    }
}
