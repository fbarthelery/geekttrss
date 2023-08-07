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
package com.geekorum.ttrss.manage_feeds.add_feed

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.geekorum.ttrss.manage_feeds.BaseSessionActivity
import com.geekorum.ttrss.manage_feeds.R
import com.geekorum.ttrss.ui.AppTheme3


class SubscribeToFeedActivity : BaseSessionActivity() {

    private val viewModel: SubscribeToFeedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AppTheme3 {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val destination = currentBackStackEntry?.destination

                SubscribeToFeedScaffold(
                    bottomBar = {
                        val cancelTxtId = when (destination?.route) {
                            ROUTE_ENTER_FEED_URL -> R.string.activity_subscribe_feed_btn_cancel
                            else -> R.string.activity_subscribe_feed_btn_back
                        }
                        val nextTxtId = when (destination?.route) {
                            ROUTE_DISPLAY_ERROR -> R.string.activity_subscribe_feed_btn_close
                            else -> R.string.activity_subscribe_feed_btn_subscribe
                        }
                        BottomButtonBar(
                            nextTxtId = nextTxtId,
                            cancelTxtId = cancelTxtId, onCancelClick = {
                                if (!navController.popBackStack()) {
                                    finish()
                                }
                            },
                            onNextClick = {
                                when (destination?.route) {
                                    ROUTE_ENTER_FEED_URL -> {
                                        viewModel.submitUrl(viewModel.urlTyped)
                                    }
                                    ROUTE_SELECT_FEED -> {
                                        if (viewModel.selectedFeed != null) {
                                            viewModel.subscribeToFeed(viewModel.selectedFeed!!.toFeedInformation())
                                            finish()
                                        }
                                    }
                                    ROUTE_DISPLAY_ERROR -> finish()
                                    else -> Unit
                                }
                            })
                    }) {
                    SubscribeToFeedNavHost(
                        modifier = Modifier.padding(it),
                        viewModel = viewModel,
                        navController = navController,
                        finishActivity = {
                            finish()
                        }
                    )
                    SideEffect {
                        if (destination?.route == ROUTE_ENTER_FEED_URL) {
                            viewModel.resetAvailableFeeds()
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun BottomButtonBar(
    @StringRes cancelTxtId: Int,
    @StringRes nextTxtId: Int,
    onCancelClick: () -> Unit, onNextClick: () -> Unit) {
    Row(Modifier.padding(8.dp)) {
        TextButton(onClick = onCancelClick) {
            Text(stringResource(cancelTxtId))
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = onNextClick) {
            Text(stringResource(nextTxtId))
        }
    }
}



@Composable
private fun SubscribeToFeedScaffold(
    bottomBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            LargeTitleBar()
        },
        bottomBar = {
            Box(Modifier.navigationBarsPadding()) {
                bottomBar()
            }
        },
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LargeTitleBar() {
    LargeTopAppBar(
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ),
        title = {
        Text(
            stringResource(R.string.activity_add_feed_title),
            modifier = Modifier
                .padding(start = 32.dp)
                .paddingFromBaseline(bottom = 28.dp),
        ) })
}

@Preview
@Composable
private fun PreviewSubscribeToFeedScaffold() {
    AppTheme3 {
        SubscribeToFeedScaffold(bottomBar = {
            BottomButtonBar(
                cancelTxtId = R.string.activity_subscribe_feed_btn_cancel,
                nextTxtId = R.string.activity_subscribe_feed_btn_subscribe,
                onCancelClick = {}, onNextClick = {}
            )
        }) {}
    }
}