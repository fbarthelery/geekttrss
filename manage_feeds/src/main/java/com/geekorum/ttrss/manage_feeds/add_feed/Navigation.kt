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

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.geekorum.ttrss.manage_feeds.R

val ROUTE_ENTER_FEED_URL = "enter_feed_url"
val ROUTE_SELECT_FEED = "select_feed"
val ROUTE_DISPLAY_ERROR = "display_error?errorMsgId={errorMsgId}"

@Composable
fun SubscribeToFeedNavHost(
    viewModel: SubscribeToFeedViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
    finishActivity: () -> Unit,
) {
    NavHost(navController = navController, startDestination = ROUTE_ENTER_FEED_URL) {
        composable(ROUTE_ENTER_FEED_URL) {
            // TODO animation with navigation 1.7.x
            EnterFeedUrlScreen(viewModel,
                navigateToDisplayError = {
                    navController.navigateToDisplayError(it)
                },
                navigateToShowAvailableFeeds = {
                    navController.navigateToSelectFeed()
                },
                finishActivity = finishActivity)
        }
        composable(ROUTE_SELECT_FEED) {
            SelectFeedScreen(viewModel)
        }
        composable(ROUTE_DISPLAY_ERROR, arguments = listOf(
            navArgument("errorMsgId") {
                type = NavType.IntType
                defaultValue = R.string.fragment_display_error_no_feeds_found
            }
        )) {
            val errorMsgId = it.arguments!!.getInt("errorMsgId")
            DisplayErrorScreen(errorMsgId = errorMsgId)
        }
    }
}

private fun NavController.navigateToDisplayError(errorMsgId: Int?) {
    val route = buildString {
        append("display_error")
        if (errorMsgId != null) {
            append("?errorMsgId=${errorMsgId}")
        }
    }
    navigate(route)
}

private fun NavController.navigateToSelectFeed() {
    navigate(ROUTE_SELECT_FEED)
}