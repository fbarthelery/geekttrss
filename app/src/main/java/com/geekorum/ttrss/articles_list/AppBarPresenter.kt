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

import android.app.Activity
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.FloatingWindow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Feed.Companion.FEED_ID_ALL_ARTICLES
import com.geekorum.ttrss.ui.AppTheme


/**
 * Controls the behavior of the AppBar
 */
internal class AppBarPresenter(
    private val activity: Activity,
    private val tagsViewModel: TagsViewModel,
    private val activityViewModel: ActivityViewModel,
    private val navController: NavController,
) {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Composable
    fun ToolbarContent(modifier: Modifier = Modifier, onNavigationMenuClick: () -> Unit) {
        val windowSizeClass = calculateWindowSizeClass(activity)
        val hasFixedDrawer = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

        AppTheme {
            val currentDestination by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(
                null
            )

            Column(modifier) {
                Toolbar(
                    hasFixedDrawer,
                    currentDestination,
                    onNavigationMenuClick,
                    Modifier.zIndex(1f)
                )
                AnimatedTagsList(currentDestination)
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun AnimatedTagsList(currentDestination: NavBackStackEntry?) {
        val currentTag = if (currentDestination?.destination?.route == NavRoutes.ArticlesListByTag) {
            currentDestination.arguments?.getString("tag")
        } else {
            null
        }

        val showTagsBar = run {
            when (currentDestination?.destination?.route) {
                NavRoutes.ArticlesList -> {
                    val feedId = currentDestination.arguments?.let { ArticlesListScreenArgs(it) }?.feedId
                        ?: FEED_ID_ALL_ARTICLES
                    feedId == FEED_ID_ALL_ARTICLES
                }
                NavRoutes.ArticlesListByTag -> true

                else -> false
            }
        }
        val tags by tagsViewModel.tags.observeAsState()
        val tagsSet = tags?.toSet() ?: emptySet()
        AnimatedContent(targetState = showTagsBar && tagsSet.isNotEmpty(),
            transitionSpec = {
                fadeIn() + slideInVertically() with
                        fadeOut() + slideOutVertically() using SizeTransform(clip = false)
            },
            label = "TagBarVisibility"
        ) { visible ->
            if (visible) {
                TagsListBar(tags = tagsSet,
                    selectedTag = currentTag,
                    selectedTagChange = { tag ->
                        if (tag == null) {
                            if (navController.currentDestination?.route == NavRoutes.ArticlesListByTag) {
                                navController.popBackStack()
                            }
                        } else {
                            navController.navigateToTag(tag)
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun Toolbar(
        hasFixedDrawer: Boolean,
        currentDestination: NavBackStackEntry?,
        onNavigationMenuClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val hasSortMenu = destinationHasSortButton(currentDestination?.destination)
        val sortOrder by activityViewModel.sortOrder.collectAsStateWithLifecycle()

        val navigationIcon: @Composable () -> Unit = {
            val hasDrawerMenu = currentDestination?.destination?.let {
                isTopLevelDestinationRoute(it)
            } ?: true
            if (hasDrawerMenu) {
                IconButton(onClick = onNavigationMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.drawer_open))
                }
            }
        }

        val appbarState = rememberArticlesListAppBarState(
            onSearchTextChange = {
                activityViewModel.setSearchQuery(it)
            },
            onSearchOpenChange = { searchOpen ->
                if (searchOpen) {
                    navController.navigateToSearch()
                } else {
                    if (navController.currentDestination?.route == NavRoutes.Search) {
                        navController.popBackStack()
                    }
                }
            }
        )

        if (currentDestination != null && currentDestination.destination.route != NavRoutes.Search) {
            LaunchedEffect(currentDestination) {
                appbarState.closeSearch()
            }
        }

        val context = LocalContext.current
        var toolbarTitle by remember { mutableStateOf("") }
        LaunchedEffect(currentDestination, context) {
            if (currentDestination?.destination !is FloatingWindow) {
                if (currentDestination?.destination != null && currentDestination.destination.label == null) {
                    setDestinationLabelPerRoute(context, currentDestination.destination)
                }
                toolbarTitle = currentDestination?.destination?.fillInLabel(
                    context,
                    currentDestination.arguments
                ) ?: ""
            }
        }

        ArticlesListAppBar(
            appBarState = appbarState,
            title = {
                AppBarTitleText(toolbarTitle)
            },
            sortOrder = sortOrder,
            onSortOrderChange = {
                val mostRecentFirst = when (it) {
                    SortOrder.MOST_RECENT_FIRST -> true
                    SortOrder.OLDEST_FIRST -> false
                }
                activityViewModel.setSortByMostRecentFirst(mostRecentFirst)
            },
            displaySortMenuButton = hasSortMenu,
            displaySearchButton = true,
            navigationIcon = if (!hasFixedDrawer) navigationIcon else null,
            modifier = modifier
        )
    }

    private fun setDestinationLabelPerRoute(context: Context, destination: NavDestination) {
        destination.label = NavRoutes.getLabelForRoute(context, destination.route)
    }

    private fun isTopLevelDestinationRoute(destination: NavDestination): Boolean =
        NavRoutes.isTopLevelDestination(destination.route)

    private fun destinationHasSortButton(destination: NavDestination?) = when (destination?.route) {
        NavRoutes.ArticlesList,
        NavRoutes.ArticlesListByTag -> true

        else -> false
    }
}
