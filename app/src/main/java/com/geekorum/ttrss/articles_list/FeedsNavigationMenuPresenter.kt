/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2021 by Frederic-Charles Barthelery.
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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import com.geekorum.geekdroid.views.doOnApplyWindowInsets
import com.geekorum.ttrss.ArticlesListDirections
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.ui.AppTheme
import com.google.accompanist.insets.ProvideWindowInsets


/**
 * Display the feeds in a NavigationView menu.
 */
class FeedsNavigationMenuPresenter(
    private val composeView: ComposeView,
    private val navController: NavController,
    private val feedsViewModel: FeedsViewModel,
    private val accountViewModel: TtrssAccountViewModel,
    private val activityViewModel: ActivityViewModel
) {

    private var currentFeedId: Long? by mutableStateOf(null)
    private var isMagazineFeed: Boolean by mutableStateOf(true)

    init {
        setDestinationListener()
        setComposeContent()
    }

    private fun setComposeContent() {
        // pass window insets to compose
        composeView.fitsSystemWindows = true
        composeView.doOnApplyWindowInsets { view, windowInsetsCompat, relativePadding ->
            windowInsetsCompat
        }

        composeView.setContent {
            ProvideWindowInsets {
                AppTheme {
                    Surface(Modifier.fillMaxSize()) {
                        val account by accountViewModel.selectedAccount.observeAsState()
                        val server by accountViewModel.selectedAccountHost.observeAsState()
                        FeedListNavigationMenu(
                            user = account?.name ?: "",
                            server = server?: "",
                            feedSection = {
                                val feeds by feedsViewModel.feeds.collectAsState(emptyList())
                                FeedSection(
                                    feeds,
                                    selectedFeed = feeds.find { it.id == currentFeedId },
                                    isMagazineSelected = isMagazineFeed,
                                    onFeedSelected = {
                                        navigateToFeed(it)
                                    },
                                    onMagazineSelected = {
                                        navigateToMagazine()
                                    }
                                )
                            },
                            onManageFeedsClicked = {
                                val directions = ArticlesListDirections.actionManageFeeds()
                                navController.navigate(directions)
                            },
                            onSettingsClicked = {
                                val directions = ArticlesListDirections.actionShowSettings()
                                navController.navigate(directions)
                            },
                        )
                    }
                }
            }
        }
    }

    private fun navigateToMagazine() {
        navController.navigate(ArticlesListDirections.actionShowMagazine())
    }

    private fun navigateToFeed(feed: Feed) {
        activityViewModel.setSelectedFeed(feed)
        navController.navigate(ArticlesListDirections.actionShowFeed(feed.id, feed.title))
    }

    private fun setDestinationListener() {
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            when(destination.id) {
                R.id.articlesListFragment -> {
                    currentFeedId = arguments!!.getLong("feed_id")
                    isMagazineFeed = false
                }
                R.id.magazineFragment -> {
                    currentFeedId = null
                    isMagazineFeed = true
                }
            }
        }
    }
}
