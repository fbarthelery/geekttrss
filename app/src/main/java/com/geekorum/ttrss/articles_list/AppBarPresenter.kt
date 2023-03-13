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
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.FloatingWindow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Feed.Companion.FEED_ID_ALL_ARTICLES
import com.geekorum.ttrss.on_demand_modules.OnDemandModuleNavHostProgressDestinationProvider
import com.geekorum.ttrss.ui.AppTheme
import com.google.android.material.appbar.AppBarLayout


/**
 * Controls the behavior of the AppBar
 */
internal class AppBarPresenter(
    private val activity: Activity,
    private val appBarLayout: AppBarLayout,
    private val appBarConfiguration: AppBarConfiguration,
    private val toolbar: ComposeView,
    private val tagsViewModel: TagsViewModel,
    private val activityViewModel: ActivityViewModel,
    private val onDemandModuleNavHostProgressDestinationProvider: OnDemandModuleNavHostProgressDestinationProvider,
    private val navController: NavController,
){

    init {
        setup()
        setupToolbar()
    }

    private fun setup() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val progressDestinationId = onDemandModuleNavHostProgressDestinationProvider.progressDestinationId

            when (destination.id) {
                R.id.articlesListFragment,
                R.id.articlesListByTagFragment,
                R.id.articlesSearchFragment -> appBarLayout.setExpanded(true)

                progressDestinationId -> {
                    appBarLayout.setExpanded(true)
                }
            }
        }
    }

    private fun setupToolbar() {
        toolbar.setContent {
            ToolbarContent()
        }
    }


    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Composable
    fun ToolbarContent() {
        val windowSizeClass = calculateWindowSizeClass(activity)
        val hasFixedDrawer = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

        AppTheme {
            val currentDestination by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(
                null
            )

            Column {
                Toolbar(hasFixedDrawer, currentDestination, Modifier.zIndex(1f))
                AnimatedTagsList(currentDestination)
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun AnimatedTagsList(currentDestination: NavBackStackEntry?) {
        val currentTag = if (currentDestination?.destination?.id == R.id.articlesListByTagFragment) {
            currentDestination.arguments?.getString("tag")
        } else {
            null
        }

        val showTagsBar = run {
            when (currentDestination?.destination?.id) {
                R.id.articlesListFragment,
                R.id.articlesListByTagFragment -> {
                    val feedId = currentDestination.arguments?.let { ArticlesListFragmentArgs.fromBundle(it) }?.feedId ?: FEED_ID_ALL_ARTICLES
                    feedId == FEED_ID_ALL_ARTICLES
                }
                else -> false
            }
        }
        val tags by tagsViewModel.tags.observeAsState()
        val tagsSet = tags?.toSet() ?: emptySet()
        AnimatedContent(targetState = showTagsBar && tagsSet.isNotEmpty(),
            transitionSpec = {
                fadeIn() + slideInVertically() with
                        fadeOut() + slideOutVertically() using SizeTransform(clip = false)
            }
        ) { visible ->
            if (visible) {
                TagsListBar(tags = tagsSet,
                    selectedTag = currentTag,
                    selectedTagChange = { tag ->
                        if (tag == null) {
                            if (navController.currentDestination?.id == R.id.articlesListByTagFragment) {
                                navController.popBackStack()
                            }
                        } else {
                            val showTag = ArticlesListFragmentDirections.actionShowTag(tag)
                            navController.navigate(showTag)
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
        modifier: Modifier = Modifier,
    ) {
        val progressDestinationId =
            onDemandModuleNavHostProgressDestinationProvider.progressDestinationId

        val hasSearchButton = currentDestination?.destination?.id != progressDestinationId
        val hasSortMenu = when (currentDestination?.destination?.id) {
            R.id.articlesListFragment,
            R.id.articlesListByTagFragment -> true
            else -> false
        }
        val sortOrder by activityViewModel.sortOrder.collectAsStateWithLifecycle()

        val navigationIcon: @Composable () -> Unit = {
            val hasDrawerMenu = currentDestination?.destination?.let { appBarConfiguration.isTopLevelDestination(it) } ?: true
            if (hasDrawerMenu) {
                IconButton(onClick = { appBarConfiguration.openableLayout?.open() }) {
                    Icon(Icons.Default.Menu, contentDescription = "open menu")
                }
            }
        }

        val appbarState = rememberArticlesListAppBarState(
            onSearchTextChange = {
                activityViewModel.setSearchQuery(it)
            },
            onSearchOpenChange = { searchOpen ->
                if (searchOpen) {
                    navController.navigate(ArticlesListFragmentDirections.actionSearchArticle())
                } else {
                    if (navController.currentDestination?.id == R.id.articlesSearchFragment) {
                        navController.popBackStack()
                    }
                }
            }
        )

        if (currentDestination != null && currentDestination.destination.id != R.id.articlesSearchFragment) {
            LaunchedEffect(currentDestination) {
                appbarState.closeSearch()
            }
        }

        val context = LocalContext.current
        var toolbarTitle by remember { mutableStateOf("") }
        LaunchedEffect(currentDestination, context) {
            if (currentDestination?.destination !is FloatingWindow) {
                toolbarTitle = currentDestination?.destination?.fillInLabel(context,
                    currentDestination.arguments
                ) ?: ""
            }
            if (currentDestination?.destination?.id == progressDestinationId) {
                toolbarTitle = context.resources.getString(R.string.lbl_install_feature_title)
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
            displaySearchButton = hasSearchButton,
            navigationIcon = if (!hasFixedDrawer) navigationIcon else null,
            modifier = modifier
        )
    }

}
