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

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.geekorum.ttrss.R

/**
 * Controls the behavior of the FloatingActionButton
 */
internal class FabPresenter(
    private val navController: NavController
){

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun Content(isScrollingUpOrRest: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
        //TODO deal with module install
        val navBackStackEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(null)
        val fabVisibleInDestination = when {
            navBackStackEntry?.destination?.route == NavRoutes.Search -> false
            navBackStackEntry == null -> false
            else -> true
        }

        val fabVisible = fabVisibleInDestination && isScrollingUpOrRest
        val targetState = if (fabVisible) {
            val feedId = if (navBackStackEntry?.destination?.route == NavRoutes.ArticlesList) {
                navBackStackEntry?.arguments?.getLong("feed_id")
            } else null
            DestinationWithFeedId(
                destination = navBackStackEntry?.destination,
                feedId = feedId
            )
        } else null
        // as M3 scaffold remove the fab if size is 0 we need to have a size
        // so that even when fab is not visible it is still added in composition
        AnimatedContent(
            modifier = modifier.size(56.dp),
            targetState = targetState,
            transitionSpec = {
                scaleIn(tween(300, delayMillis = 100)) togetherWith
                        scaleOut(tween(300)) using
                        SizeTransform(clip = false) { _, _ ->
                            // delay size transform to make scaleOut happens in place
                            tween(300, delayMillis = if (targetState == null) 300 else 0)
                        }
            },
            contentAlignment = Alignment.Center,
            label = "AnimatedFabVisibility",
        ) { state ->
            if (state != null) {
                FloatingActionButton(
                    onClick = onClick
                ) {
                    Icon(Icons.Default.Refresh, stringResource(R.string.btn_refresh))
                }
            }
        }
    }

    private data class DestinationWithFeedId(
        val destination: NavDestination?,
        val feedId: Long?
    )
}
