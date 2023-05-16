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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.geekorum.ttrss.R
import com.geekorum.ttrss.article_details.ArticleDetailActivity
import com.geekorum.ttrss.articles_list.magazine.MagazineScreen
import com.geekorum.ttrss.articles_list.search.ArticlesSearchScreen
import com.geekorum.ttrss.settings.SettingsActivity

object NavRoutes {
    const val Magazine = "magazine"
    const val ArticlesList = "feeds/{feed_id}?feed_name={feed_name}"
    const val ArticlesListByTag = "tags/{tag}"
    const val Search = "search"

    fun getLabelForRoute(context: Context, route: String?) = when(route) {
        Magazine -> context.getString(R.string.title_magazine)
        Search -> context.getString(R.string.title_article_search)
        ArticlesList -> "{feed_name}"
        ArticlesListByTag -> "#{tag}"
        else -> null
    }

    fun isTopLevelDestination(route: String?) = when (route) {
        Magazine,
        ArticlesList,
        ArticlesListByTag -> true

        else -> false
    }
}

class ArticlesListScreenArgs(
    val feedId: Long,
    val feedName: String?
) {
    constructor(arguments: Bundle) : this(
        feedId = arguments.getLong("feed_id"),
        feedName = arguments.getString("feed_name"))
}

@Composable
fun ArticlesListNavHost(
    activityViewModel: ActivityViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    NavHost(navController = navController, startDestination = "magazine") {
        composable(NavRoutes.Magazine) {
            MagazineScreen(activityViewModel = activityViewModel, contentPadding = contentPadding)
        }
        composable(NavRoutes.ArticlesList, arguments = listOf(
            navArgument("feed_id") {
                type = NavType.LongType
                defaultValue = -4L
            },
            navArgument("feed_name") {
                defaultValue = "All Articles"
            }
        )) {
            ArticlesListScreen(activityViewModel = activityViewModel, contentPadding = contentPadding)
        }

        composable(NavRoutes.ArticlesListByTag) {
            ArticlesListByTagScreen(activityViewModel = activityViewModel, contentPadding = contentPadding)
        }

        composable(NavRoutes.Search) {
            ArticlesSearchScreen(activityViewModel = activityViewModel)
        }
    }
}

 fun NavController.navigateToFeed(feedId: Long = -4L, feedTitle: String? = null) {
     // we change navigation stack but don't restore state
     val route = buildString {
         append("feeds/$feedId")
         if (feedTitle != null) {
             append("?feed_name=$feedTitle")
         }
     }
     navigate(route) {
         popUpTo(graph.findStartDestination().route!!) {
             saveState = true
         }
         launchSingleTop = true
     }
 }

fun NavController.navigateToTag(tag: String) {
    navigate("tags/$tag") {
        popUpTo(NavRoutes.ArticlesListByTag) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

fun NavController.navigateToMagazine() {
    popBackStack(NavRoutes.Magazine, inclusive = false)
}

fun NavController.navigateToSettings() {
    val intent = Intent(context, SettingsActivity::class.java)
    context.startActivity(intent)
}


fun NavController.navigateToArticle(articleId: Long) {
    val intent = Intent(context, ArticleDetailActivity::class.java).apply {
        data = context.getString(R.string.article_details_data_pattern)
            .replace("{article_id}", articleId.toString())
            .toUri()
    }
    context.startActivity(intent)
}


fun NavController.navigateToSearch() {
    navigate(NavRoutes.Search)
}

fun NavController.navigateToManageFeeds() {
    val intent = Intent().apply {
        component = ComponentName(context, "com.geekorum.ttrss.manage_feeds.ManageFeedsActivity")
    }
    context.startActivity(intent)
}
