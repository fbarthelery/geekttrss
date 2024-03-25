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

import android.app.Activity
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.FloatingWindow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Feed.Companion.FEED_ID_ALL_ARTICLES


/**
 * Controls the behavior of the AppBar
 */
internal class AppBarPresenter(
    private val activity: Activity,
    private val tagsViewModel: TagsViewModel,
    private val activityViewModel: ActivityViewModel,
    private val navController: NavController,
) {

    /**
     * Are we in a transition to/from ArticleSearchBar
     */
    var isSearchTransitioning: Boolean = false
        private set

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun ToolbarContent(modifier: Modifier = Modifier, scrollBehavior: TopAppBarScrollBehavior? = null, onNavigationMenuClick: () -> Unit) {
        val windowSizeClass = calculateWindowSizeClass(activity)
        val hasFixedDrawer = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

        val currentDestination by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(
            navController.currentBackStackEntry
        )

        val isSearchOpen = currentDestination != null && currentDestination?.destination?.route == NavRoutes.Search
        val isNotOnSearchDestination = currentDestination != null && currentDestination?.destination?.route != NavRoutes.Search
        if (isNotOnSearchDestination) {
            LaunchedEffect(Unit) {
                activityViewModel.setSearchQuery("")
            }
        }

        val searchScreenTransition = updateTransition(targetState = isSearchOpen, label = "show search")
        SideEffect {
            isSearchTransitioning = searchScreenTransition.currentState != searchScreenTransition.targetState
        }

        searchScreenTransition.AnimatedContent(
            transitionSpec = {
                //TODO tweak transition
                fadeIn(animationSpec = tween(220 , delayMillis = 90 ))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            },
            modifier = modifier
        ) { showSearch ->
            if (showSearch) {
                var active by rememberSaveable { mutableStateOf(true) }
                var query by rememberSaveable { mutableStateOf("") }
                var hasSearched by rememberSaveable { mutableStateOf(false) }
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) {
                    if (active) {
                        focusRequester.requestFocus()
                    }
                }

                val suggestions by activityViewModel.articlesSearchHistory.collectAsStateWithLifecycle()
                ArticlesSearchBar(
                    active = active,
                    onActiveChange = {
                        if (!it && !hasSearched) navController.popBackStack()
                        active = it
                    },
                    query = query,
                    onQueryChange = {
                        query = it
                    },
                    suggestions = suggestions,
                    onSearch = {
                        activityViewModel.setSearchQuery(it)
                        activityViewModel.recordSearchQueryInHistory(it)
                        hasSearched = true
                    },
                    onUpClick = { navController.popBackStack() },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth()
                        .wrapContentWidth()
                        .semantics { traversalIndex = -1f })

            } else {
                TopAppBar(
                    hasFixedDrawer = hasFixedDrawer,
                    currentDestination = currentDestination,
                    onNavigationMenuClick = onNavigationMenuClick,
                    onSearchClick = { navController.navigateToSearch() },
                    scrollBehavior = scrollBehavior
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TopAppBar(
        hasFixedDrawer: Boolean,
        currentDestination: NavBackStackEntry?,
        onNavigationMenuClick: () -> Unit,
        onSearchClick: () -> Unit,
        modifier: Modifier = Modifier,
        scrollBehavior: TopAppBarScrollBehavior? = null
    ) {
        val colors = TopAppBarDefaults.toolbarAppBarColors()

        // Sets the app bar's height offset to collapse the entire bar's height when content is
        // scrolled
        val heightOffsetLimit =
            // -64dp without TagsList + 48.dp with list
            // TODO change base on tags
            with(LocalDensity.current) { ((-64).dp).toPx() }
        SideEffect {
            if (scrollBehavior?.state?.heightOffsetLimit != heightOffsetLimit) {
                scrollBehavior?.state?.heightOffsetLimit = heightOffsetLimit
            }
        }

        // Obtain the container color from the TopAppBarColors using the `overlapFraction`. This
        // ensures that the colors will adjust whether the app bar behavior is pinned or scrolled.
        // This may potentially animate or interpolate a transition between the container-color and the
        // container's scrolled-color according to the app bar's scroll state.
        val colorTransitionFraction = scrollBehavior?.state?.overlappedFraction ?: 0f
        val fraction = if (colorTransitionFraction > 0.01f) 1f else 0f
        val appBarContainerColor by animateColorAsState(
            targetValue = colors.containerColor(fraction),
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "ToolbarContainerColor"
        )

        Surface(color = appBarContainerColor) {
            Column(modifier) {
                Toolbar(
                    hasFixedDrawer,
                    currentDestination,
                    onNavigationMenuClick,
                    onSearchClick,
                    Modifier.zIndex(1f)
                )
                AnimatedTagsList(currentDestination)
            }
        }
    }

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
                fadeIn() + slideInVertically() togetherWith
                        fadeOut() + slideOutVertically() using SizeTransform(clip = false)
            },
            label = "TagBarVisibility"
        ) { visible ->
            if (visible) {
                TagsListBar(
                    color = Color.Transparent,
                    tags = tagsSet,
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Toolbar(
        hasFixedDrawer: Boolean,
        currentDestination: NavBackStackEntry?,
        onNavigationMenuClick: () -> Unit,
        onSearchClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        var hasSortMenu by remember {
            mutableStateOf(destinationHasSortButton(currentDestination?.destination))
        }
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

        val context = LocalContext.current
        var toolbarTitle by remember { mutableStateOf("") }
        LaunchedEffect(currentDestination, context) {
            if (currentDestination?.destination !is FloatingWindow) {
                if (currentDestination?.destination != null && currentDestination.destination.label == null) {
                    setDestinationLabelPerRoute(context, currentDestination.destination)
                }
                // change title only if there is a new one otherwise keep previous
                currentDestination?.destination?.fillInLabel(
                    context,
                    currentDestination.arguments
                )?.let {
                    toolbarTitle =  it
                }

                // change sortMenu if not going to search
                if (currentDestination?.destination?.route != NavRoutes.Search) {
                    hasSortMenu = destinationHasSortButton(currentDestination?.destination)
                }
            }
        }

        ArticlesListAppBar(
            // transparent container colors, the surface colors will be handled by parent
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
            title = {
                AppBarTitleText(toolbarTitle)
            },
            onSearchClick = onSearchClick,
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


/**
 * Copy of [TopAppBarColors] to allow using [TopAppBarColors.containerColor]
 */
@ExperimentalMaterial3Api
@Stable
private class ToolbarAppBarColors internal constructor(
    private val containerColor: Color,
    private val scrolledContainerColor: Color,
    internal val navigationIconContentColor: Color,
    internal val titleContentColor: Color,
    internal val actionIconContentColor: Color,
) {

    /**
     * Represents the container color used for the top app bar.
     *
     * A [colorTransitionFraction] provides a percentage value that can be used to generate a color.
     * Usually, an app bar implementation will pass in a [colorTransitionFraction] read from
     * the [TopAppBarState.collapsedFraction] or the [TopAppBarState.overlappedFraction].
     *
     * @param colorTransitionFraction a `0.0` to `1.0` value that represents a color transition
     * percentage
     */
    @Composable
    internal fun containerColor(colorTransitionFraction: Float): Color {
        return lerp(
            containerColor,
            scrolledContainerColor,
            FastOutLinearInEasing.transform(colorTransitionFraction)
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ToolbarAppBarColors) return false

        if (containerColor != other.containerColor) return false
        if (scrolledContainerColor != other.scrolledContainerColor) return false
        if (navigationIconContentColor != other.navigationIconContentColor) return false
        if (titleContentColor != other.titleContentColor) return false
        if (actionIconContentColor != other.actionIconContentColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + scrolledContainerColor.hashCode()
        result = 31 * result + navigationIconContentColor.hashCode()
        result = 31 * result + titleContentColor.hashCode()
        result = 31 * result + actionIconContentColor.hashCode()

        return result
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBarDefaults.toolbarAppBarColors(
    containerColor: Color = MaterialTheme.colorScheme.surface,
    scrolledContainerColor: Color = MaterialTheme.colorScheme.applyTonalElevation(
        backgroundColor = containerColor,
        elevation = 3.dp
    ),
    navigationIconContentColor: Color = MaterialTheme.colorScheme.onSurface,
    titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
    actionIconContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
): ToolbarAppBarColors =
    ToolbarAppBarColors(
        containerColor,
        scrolledContainerColor,
        navigationIconContentColor,
        titleContentColor,
        actionIconContentColor
    )

/**
 * Returns the new background [Color] to use, representing the original background [color] with an
 * overlay corresponding to [elevation] applied. The overlay will only be applied to
 * [ColorScheme.surface].
 */
internal fun ColorScheme.applyTonalElevation(backgroundColor: Color, elevation: Dp): Color {
    return if (backgroundColor == surface) {
        surfaceColorAtElevation(elevation)
    } else {
        backgroundColor
    }
}
