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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.geekorum.ttrss.Features
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.on_demand_modules.InstallModuleViewModel
import com.geekorum.ttrss.on_demand_modules.InstallSession
import com.geekorum.ttrss.ui.AppTheme3


private const val CODE_REQUEST_USER_CONFIRMATION = 1

/**
 * Display the feeds in a NavigationView menu.
 */
class FeedsNavigationMenuPresenter(
    private val navController: NavController,
    private val feedsViewModel: FeedsViewModel,
    private val accountViewModel: TtrssAccountViewModel,
    private val activityViewModel: ActivityViewModel,
    private val installModuleViewModel: InstallModuleViewModel,
    private val activity: Activity,
) {

    @Composable
    fun Content(hasFab: Boolean, onNavigation: () -> Unit, modifier: Modifier = Modifier) {
        val navBackStackEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(null)
        val currentFeedId = run {
            if (navBackStackEntry?.destination?.route == NavRoutes.ArticlesList) {
                navBackStackEntry?.arguments?.getLong("feed_id")
            } else if (navBackStackEntry?.destination?.route == NavRoutes.ArticlesListByTag) {
                val listEntry = navController.getBackStackEntry(NavRoutes.ArticlesList)
                listEntry.arguments?.getLong("feed_id")
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
                    selectedFeed = feeds.find { it.feed.id == currentFeedId }?.feed,
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
            manageFeedsSection = {
                ManageFeedSection(
                    viewModel = installModuleViewModel,
                    activity = activity,
                    navigateToManageFeed = {
                        check(installModuleViewModel.isModuleInstalled(Features.MANAGE_FEEDS)) {
                            { "${Features.MANAGE_FEEDS} is not installed" }
                        }
                        navController.navigateToManageFeeds()
                        onNavigation()
                    },
		     onMarkFeedAsReadClick = {
                       feedsViewModel.markFeedAsRead(it)
                    })
            },
            onSettingsClicked = {
                navController.navigateToSettings()
                onNavigation()
            },
            fab = fab
        )
    }

    private fun navigateToFeed(feed: Feed) {
        activityViewModel.setSelectedFeed(feed)
        navController.navigateToFeed(feed.id, feed.title)
    }

}


@Composable
private fun ManageFeedSection(
    viewModel: InstallModuleViewModel = hiltViewModel(),
    activity: Activity,
    navigateToManageFeed: () -> Unit
) {
    val installState by viewModel.sessionState.collectAsStateWithLifecycle()
    val installProgress by viewModel.progress.collectAsStateWithLifecycle()
    val installationIsInProgress = installState.status != InstallSession.State.Status.PENDING

    LaunchedEffect(installState) {
        when (installState.status) {
            InstallSession.State.Status.REQUIRES_USER_CONFIRMATION ->
                viewModel.startUserConfirmationDialog(activity, CODE_REQUEST_USER_CONFIRMATION)
            else -> Unit
        }
    }

    ManageFeedSection(
        installInProgress = installationIsInProgress,
        installationMessage = stringResource(installProgress.message),
        indeterminateProgress = installProgress.progressIndeterminate,
        progress = installProgress.progress,
        progressMax = installProgress.max,
        onItemClick = {
            if (viewModel.isModuleInstalled(Features.MANAGE_FEEDS)) {
                navigateToManageFeed()
                viewModel.resetInstallState()
            } else if (installState.status == InstallSession.State.Status.FAILED || installState.status == InstallSession.State.Status.CANCELED) {
                viewModel.resetInstallState()
            } else {
                viewModel.startInstallModules(Features.MANAGE_FEEDS)
            }
        }
    )
}

@Composable
private fun ManageFeedSection(
    installInProgress: Boolean,
    installationMessage: String,
    indeterminateProgress: Boolean,
    progress: Int,
    progressMax: Int,
    onItemClick: () -> Unit,
) {
    Surface(onClick = onItemClick, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)) {
        Column(Modifier.animateContentSize()) {
            ManageFeedItem()

            if (installInProgress) {
                Column(Modifier.padding(start = 56.dp)) {
                    Text(installationMessage, style = MaterialTheme.typography.bodySmall)
                    if (indeterminateProgress) {
                        LinearProgressIndicator(
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(
                                top = 8.dp,
                                bottom = 16.dp
                            )
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = progress / progressMax.toFloat(),
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManageFeedItem(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(56.dp)
    ) {
        NavigationItemLayout(
            icon = {
                Icon(AppTheme3.Icons.Tune, contentDescription = null)
            },
            label = {
                Text(
                    stringResource(R.string.title_manage_feeds),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
        )
    }
}


@Composable
private fun NavigationItemLayout(
    label: @Composable () -> Unit,
    icon: @Composable (() -> Unit)? = null,
) {
    Row(
        Modifier.padding(start = 16.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            val iconColor = MaterialTheme.colorScheme.onSurfaceVariant
            CompositionLocalProvider(LocalContentColor provides iconColor, content = icon)
            Spacer(Modifier.width(12.dp))
        }
        Box(Modifier.weight(1f)) {
            val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            CompositionLocalProvider(LocalContentColor provides labelColor, content = label)
        }
    }
}



@Preview
@Composable
fun PreviewManageFeedSection() {
    AppTheme3 {
        ManageFeedSection(installInProgress = false,
            installationMessage = "",
            indeterminateProgress = false,
            progress = 0,
            progressMax = 1,
            onItemClick = {})
    }
}

@Preview
@Composable
fun PreviewManageFeedSectionInstallInProgress() {
    AppTheme3 {
        ManageFeedSection(installInProgress = true,
            installationMessage = "Installing",
            indeterminateProgress = false,
            progress = 33,
            progressMax = 100,
            onItemClick = {})
    }
}
