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
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.geekorum.ttrss.R
import com.geekorum.ttrss.article_details.ArticleDetailActivity
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
        data = "content://com.geekorum.ttrss.free.providers.articles/${articleId}".toUri()
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
