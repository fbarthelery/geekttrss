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
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.ui.AppTheme3
import com.geekorum.ttrss.ui.components.FeedNavigationRail
import com.geekorum.ttrss.ui.components.FeedWideNavigationRailItem
import com.geekorum.ttrss.ui.components.MagazineWideNavigationRailItem
import com.geekorum.ttrss.ui.components.ModalFeedNavigationRail
import com.geekorum.ttrss.ui.components.NavRailMenuButton
import com.geekorum.ttrss.ui.components.SectionHeader
import com.geekorum.ttrss.ui.components.SettingsWideNavigationRailItem
import com.geekorum.ttrss.ui.components.VirtualFeedWideNavigationRailItem
import com.geekorum.ttrss.ui.components.isExpanded
import kotlinx.coroutines.launch


@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun ArticleBrowsingScaffold(
    windowSizeClass: WindowSizeClass,
    feedsMenuState: FeedNavigationMenuState,
    onMagazineClick: () -> Unit,
    onFeedClick: (Feed) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ArticleBrowsingScaffold(windowSizeClass,
        modifier = modifier,
        railState = feedsMenuState.railState,
        railHeader = { isInDrawer ->
            Column {
                if (!isInDrawer) {
                    NavRailMenuButton(
                        isMenuOpen = feedsMenuState.isMenuExpanded,
                        onClick = {
                            feedsMenuState.toggleMenu()
                        },
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }
            }
        },
        railFeedSection = {
            val feeds by feedsMenuState.feeds.collectAsStateWithLifecycle(emptyList())
            SectionHeader(stringResource(R.string.title_feeds_menu))
            feeds.forEach {
                FeedWideNavigationRailItem(
                    it,
                    selected = feedsMenuState.isFeedSelected(it.feed),
                    onClick = {
                        feedsMenuState.closeMenu { onFeedClick(it.feed) }
                    },
                    railExpanded = feedsMenuState.isMenuExpanded
                )
            }
        },
        railSettingsSection = {
            SettingsWideNavigationRailItem(
                selected = feedsMenuState.isSettingsSelected,
                onClick = {
                    feedsMenuState.closeMenu {
                        onSettingsClick()
                    }
                },
                railExpanded = feedsMenuState.isMenuExpanded
            )
        },
        railQuickFeeds = {
            MagazineWideNavigationRailItem(
                selected = feedsMenuState.isMagazineSelected,
                onClick = onMagazineClick,
                railExpanded = feedsMenuState.isMenuExpanded
            )
            val virtualFeeds by feedsMenuState.virtualFeeds.collectAsStateWithLifecycle(
                emptyList()
            )
            virtualFeeds.forEach {
                VirtualFeedWideNavigationRailItem(
                    it,
                    selected = feedsMenuState.isFeedSelected(it),
                    onClick = {
                        feedsMenuState.closeMenu { onFeedClick(it) }
                    },
                    railExpanded = feedsMenuState.isMenuExpanded
                )
            }

            val feeds by feedsMenuState.feeds.collectAsStateWithLifecycle(emptyList())
            val selectedFeed = feeds.firstOrNull { feedsMenuState.isFeedSelected(it.feed) }
            if (selectedFeed != null && !feedsMenuState.isMenuExpanded) {
                FeedWideNavigationRailItem(
                    selectedFeed,
                    selected = feedsMenuState.isFeedSelected(selectedFeed.feed),
                    onClick = {
                        feedsMenuState.closeMenu { onFeedClick(selectedFeed.feed) }
                    },
                    railExpanded = feedsMenuState.isMenuExpanded
                )
            }
        },
        content = content
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalComposeRuntimeApi::class)
@Composable
fun ArticleBrowsingScaffold(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    railState: WideNavigationRailState = rememberWideNavigationRailState(),
    railHeader: @Composable ((isInDrawer: Boolean) -> Unit)?,
    railQuickFeeds: @Composable (() -> Unit),
    railFeedSection: @Composable (() -> Unit),
    railSettingsSection: @Composable (() -> Unit),
    content: @Composable () -> Unit
) {
    val compact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact || windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
    val medium = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
    val expanded = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded

    when {
        // check compact first because we want it based on height
        compact -> {
            CompactArticleBrowsingScaffold(
                modifier = modifier,
                railState = railState,
                railHeader = railHeader,
                railQuickFeeds = railQuickFeeds,
                railFeedSection = railFeedSection,
                railSettingsSection = railSettingsSection,
                content = content
            )
        }

        expanded -> {
            ExpandedArticleBrowsingScaffold(
                modifier = modifier,
                railState = railState,
                railHeader = railHeader,
                railQuickFeeds = railQuickFeeds,
                railFeedSection = railFeedSection,
                railSettingsSection = railSettingsSection,
                content = content,
            )
        }

        medium -> {
            MediumArticleBrowsingScaffold(
                modifier = modifier,
                railState = railState,
                railHeader = railHeader,
                railQuickFeeds = railQuickFeeds,
                railFeedSection = railFeedSection,
                railSettingsSection = railSettingsSection,
                content = content,
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
    railState: WideNavigationRailState,
    railHeader: @Composable ((isDrawer: Boolean) -> Unit)?,
    railQuickFeeds: @Composable (() -> Unit),
    railFeedSection: @Composable (() -> Unit),
    railSettingsSection: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ModalFeedNavigationRail(
        modifier = modifier,
        state = railState,
        hideOnCollapse = true,
        quickFeedAccess = railQuickFeeds,
        feedSection = railFeedSection,
        settingsSection = railSettingsSection,
        header = railHeader?.let { { it.invoke(true) } }
    )
    content()
}



@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediumArticleBrowsingScaffold(
    railState: WideNavigationRailState,
    railHeader: @Composable ((isDrawer: Boolean) -> Unit)?,
    railQuickFeeds: @Composable (() -> Unit),
    railFeedSection: @Composable (() -> Unit),
    railSettingsSection: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(modifier) {
        ModalFeedNavigationRail(
            state = railState,
            quickFeedAccess = railQuickFeeds,
            feedSection = railFeedSection,
            settingsSection = railSettingsSection,
            header = railHeader?.let { { it.invoke(false) } }
        )
        content()
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpandedArticleBrowsingScaffold(
    railState: WideNavigationRailState,
    railHeader: @Composable ((isDrawer: Boolean) -> Unit)?,
    railQuickFeeds: @Composable (() -> Unit),
    railFeedSection: @Composable (() -> Unit),
    railSettingsSection: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(modifier) {
        FeedNavigationRail(
            state = railState,
            quickFeedAccess = railQuickFeeds,
            feedSection = railFeedSection,
            settingsSection = railSettingsSection,
            header = railHeader?.let { { it.invoke(false) } }
        )
        content()
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
                railHeader = {
                    IconButton(
                        modifier = Modifier.padding(start = 24.dp),
                        onClick = {
                            coroutineScope.launch {
                                railState.toggle()
                            }
                        }) {
                        Icon(Icons.Default.Menu, null)
                    }
                },
                railFeedSection = {},
                railSettingsSection = {
                    SettingsWideNavigationRailItem(
                        selected = false,
                        onClick = {},
                        railExpanded = railState.targetValue.isExpanded
                    )
                },
                railQuickFeeds = {
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
