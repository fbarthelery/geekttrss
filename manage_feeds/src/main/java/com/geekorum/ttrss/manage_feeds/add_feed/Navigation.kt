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
package com.geekorum.ttrss.manage_feeds.add_feed

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geekorum.ttrss.manage_feeds.R
import kotlinx.serialization.Serializable
import kotlin.math.sign

@Serializable
internal object EnterFeedUrlDestination

@Serializable
internal object SelectFeedDestination

@Serializable
internal data class DisplayErrorDestination(val errorMsgId: Int = R.string.fragment_display_error_no_feeds_found)

@Composable
fun SubscribeToFeedNavHost(
    modifier: Modifier = Modifier,
    viewModel: SubscribeToFeedViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
    finishActivity: () -> Unit,
) {
    val slideOffsetPx = with(LocalDensity.current) {
        30.dp.roundToPx()
    }
    NavHost(modifier = modifier, navController = navController,
        startDestination = EnterFeedUrlDestination,
        contentAlignment = Alignment.TopStart,
        enterTransition = { sharedAxisXEnterTransition(SlideDirection.Start, slideOffsetPx) },
        exitTransition = { sharedAxisXExitTransition(SlideDirection.Start, slideOffsetPx) },
        popEnterTransition = { sharedAxisXEnterTransition(SlideDirection.End, slideOffsetPx) },
        popExitTransition = { sharedAxisXExitTransition(SlideDirection.End, slideOffsetPx) },
    ) {
        composable<EnterFeedUrlDestination>(
            enterTransition = {
                if (initialState.destination.hasRoute<DisplayErrorDestination>()) {
                    fadeIn(animationSpec = tween(700))
                } else null
            },
            exitTransition = {
                if (targetState.destination.hasRoute<DisplayErrorDestination>()) {
                    fadeOut(animationSpec = tween(700))
                } else null
            }
        ) {
            EnterFeedUrlScreen(viewModel,
                navigateToDisplayError = {
                    navController.navigateToDisplayError(it)
                },
                navigateToShowAvailableFeeds = {
                    navController.navigateToSelectFeed()
                },
                finishActivity = finishActivity)
        }
        composable<SelectFeedDestination> {
            SelectFeedScreen(viewModel)
        }
        composable<DisplayErrorDestination>(
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) },
        ) {
            val errorMsgId = it.arguments!!.getInt("errorMsgId")
            DisplayErrorScreen(errorMsgId = errorMsgId)
        }
    }
}

/*
 try to replicate material motion spec for shared axis X
 https://material.io/design/motion/the-motion-system.html#shared-axis
 */
private fun <S> AnimatedContentTransitionScope<S>.sharedAxisXEnterTransition(
    towards: SlideDirection,
    slideOffsetPx: Int
) = slideIntoContainer(
    towards = towards,
    initialOffset = {
        // keep sign of full slide offset
        it.sign * slideOffsetPx
    },
    animationSpec = tween(AnimationConstants.DefaultDurationMillis)) +
        fadeIn(animationSpec = keyframes {
            delayMillis = 90
            durationMillis = AnimationConstants.DefaultDurationMillis - 90
            0f at 0 using CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        })

private fun <S> AnimatedContentTransitionScope<S>.sharedAxisXExitTransition(
    towards: SlideDirection,
    slideOffsetPx: Int
) = slideOutOfContainer(
    towards = towards,
    targetOffset = { it.sign * slideOffsetPx },
    animationSpec = tween(AnimationConstants.DefaultDurationMillis)) +
            fadeOut(animationSpec = keyframes {
                durationMillis = 90
                0f at 0 using CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
            })

private fun NavController.navigateToDisplayError(errorMsgId: Int) {
    val route = DisplayErrorDestination(errorMsgId)
    navigate(route)
}

private fun NavController.navigateToSelectFeed() {
    navigate(SelectFeedDestination)
}
