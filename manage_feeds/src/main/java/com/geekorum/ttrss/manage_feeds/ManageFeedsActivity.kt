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
package com.geekorum.ttrss.manage_feeds

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.geekorum.ttrss.manage_feeds.add_feed.SubscribeToFeedActivity
import com.geekorum.ttrss.ui.AppTheme3

class ManageFeedsActivity : BaseSessionActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme3 {
                ManageFeedNavHost(navigateToSubscribeToFeed = {
                    startSubscribeToFeed()
                })
            }
        }
    }

    private fun startSubscribeToFeed() {
        val intent = Intent(this, SubscribeToFeedActivity::class.java)
        startActivity(intent)
    }

}


@Composable
fun ManageFeedNavHost(
    navigateToSubscribeToFeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "feeds_list",
        modifier = modifier
    ) {
        composable("feeds_list") {
            ManageFeedsListScreen(
                navigateToSubscribeToFeed = navigateToSubscribeToFeed,
                navigateToEditFeed = {
                    navController.navigateToEditFeed(it)
                })
        }

        composable("edit_feed/{feedId}",
            arguments = listOf(
                navArgument("feedId") {
                    type = NavType.LongType
                }
            )
        ) {
            EditFeedScreen(navigateBack = {
                navController.popBackStack()
            })
        }
    }
}

private fun NavController.navigateToEditFeed(feedId: Long) = navigate("edit_feed/$feedId")