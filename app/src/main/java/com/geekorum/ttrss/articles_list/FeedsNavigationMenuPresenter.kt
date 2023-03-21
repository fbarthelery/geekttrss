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

import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.on_demand_modules.OnDemandModuleManager
import com.geekorum.ttrss.ui.AppTheme


/**
 * Display the feeds in a NavigationView menu.
 */
class FeedsNavigationMenuPresenter(
    private val navController: NavController,
    private val feedsViewModel: FeedsViewModel,
    private val accountViewModel: TtrssAccountViewModel,
    private val activityViewModel: ActivityViewModel,
    private val onDemandModuleManager: OnDemandModuleManager,
) {

    @Composable
    fun Content(hasFab: Boolean, onNavigation: () -> Unit, modifier: Modifier = Modifier) {
        AppTheme {
            Surface(modifier) {
                val navBackStackEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(null)
                val currentFeedId = run {
                    if (navBackStackEntry?.destination?.route == NavRoutes.ArticlesList) {
                        navBackStackEntry?.arguments?.getLong("feed_id")
                    } else {
                        null
                    }
                }
                val isMagazineFeed = navBackStackEntry?.destination?.route == NavRoutes.Magazine

                val account by accountViewModel.selectedAccount.observeAsState()
                val server by accountViewModel.selectedAccountHost.observeAsState()

                val fab: (@Composable () -> Unit)? = if (!hasFab) null else {
                    @Composable {
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.btn_refresh)) },
                            icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                            onClick = {}
                        )
                    }
                }

                FeedListNavigationMenu(
                    user = account?.name ?: "",
                    server = server ?: "",
                    feedSection = {
                        val feeds by feedsViewModel.feeds.collectAsStateWithLifecycle()
                        FeedSection(
                            feeds,
                            selectedFeed = feeds.find { it.id == currentFeedId },
                            isMagazineSelected = isMagazineFeed,
                            onFeedSelected = {
                                navigateToFeed(it)
                                onNavigation()
                            },
                            onMagazineSelected = {
                                navController.navigateToMagazine()
                                onNavigation()
                            }
                        )
                    },
                    onManageFeedsClicked = {
                        navController.navigateToManageFeeds(onDemandModuleManager)
                        onNavigation()
                    },
                    onSettingsClicked = {
                        navController.navigateToSettings()
                        onNavigation()
                    },
                    fab = fab
                )
            }
        }
    }

    private fun navigateToFeed(feed: Feed) {
        activityViewModel.setSelectedFeed(feed)
        navController.navigateToFeed(feed.id, feed.title)
    }

}
