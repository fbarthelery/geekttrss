/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.articles_browsing

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldAdaptStrategies
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.layout.calculateThreePaneScaffoldValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.get
import androidx.navigation3.scene.Scene
import com.geekorum.ttrss.articles_browsing.BrowsingSceneDecoratorStrategy.Companion.BrowsingRoleKey
import com.geekorum.ttrss.data.Feed
import kotlinx.coroutines.flow.map

val LocalBrowsingSceneScope = compositionLocalOf<BrowsingSceneScope?> { null }


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal class BrowsingSceneDecorator<T: Any>(
    private val listDetailsScene: Scene<T>,
    // use to calculate PaneScaffoldValue,
    private val paneScaffoldDirective: PaneScaffoldDirective,
    private val adaptStrategies: ThreePaneScaffoldAdaptStrategies,
    private val navigateToFeed: (Feed) -> Unit,
    private val navigateToMagazine: () -> Unit,
    private val navigateToSettings: () -> Unit,
) : Scene<T> {
//    TODO check if/how we want an unique browsing scene
    // maybe one per account
//    override val key: Any = "only one browsing" //listDetailsScene.key
    override val key: Any = listDetailsScene.key


    override val entries: List<NavEntry<T>> = listDetailsScene.entries
    override val previousEntries: List<NavEntry<T>> = listDetailsScene.previousEntries

    override val content: @Composable (() -> Unit) = {
        val browsingVm = hiltViewModel<BrowsingSceneDecoratorViewModel>()
        val feeds = remember(browsingVm) {
            browsingVm.feeds.map { list -> list.filterNot { Feed.isVirtualFeed(it.feed.id) } }
        }
        val virtualFeeds = remember(browsingVm) {
            browsingVm.feeds.map { list ->
                list.filter { Feed.isVirtualFeed(it.feed.id) }
                .map { it.feed } }
        }

        val browsingEntry = listDetailsScene.entries.last {
            it.metadata[BrowsingRoleKey] is BrowsingSceneDecoratorStrategy.BrowsingMetadata
        }
        val browsingMetadata = browsingEntry.metadata[BrowsingRoleKey] as BrowsingSceneDecoratorStrategy.BrowsingMetadata

        val railState = rememberWideNavigationRailState()
        val feedsMenuState = rememberFeedNavigationMenuState(
            feeds = feeds,
            virtualFeeds = virtualFeeds,
            railState = railState,
            selectedItem = browsingMetadata.feedSelectedItem
        )

        val browsingSceneScope = remember(feedsMenuState) {
            DefaultBrowsingSceneScope(
                feedNavigationMenuState = feedsMenuState,
                paneScaffoldDirective = paneScaffoldDirective,
                adaptStrategies = adaptStrategies
            )
        }

        ArticleBrowsingScaffold(
            feedsMenuState = feedsMenuState,
            onFeedClick = navigateToFeed,
            onSettingsClick = navigateToSettings,
            onMagazineClick = navigateToMagazine,
            content = {
                CompositionLocalProvider(
                    LocalBrowsingSceneScope provides browsingSceneScope
                ) {
                    listDetailsScene.content()
                }
            }
        )
    }
}


interface BrowsingSceneScope {
    fun toggleMenu()

    val paneExpandedCount: Int

    val isSinglePane: Boolean
        get() = paneExpandedCount == 1
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private class DefaultBrowsingSceneScope(
    private val feedNavigationMenuState: FeedNavigationMenuState,
    private val paneScaffoldDirective: PaneScaffoldDirective,
    private val adaptStrategies: ThreePaneScaffoldAdaptStrategies,
) : BrowsingSceneScope {

    override fun toggleMenu() {
        feedNavigationMenuState.toggleMenu()
    }

    private val scaffoldValue by lazy {
        calculateThreePaneScaffoldValue(
            maxHorizontalPartitions = paneScaffoldDirective.maxHorizontalPartitions,
            maxVerticalPartitions = paneScaffoldDirective.maxVerticalPartitions,
            adaptStrategies = adaptStrategies,
            destinationHistory = emptyList()
        )
    }
    override val paneExpandedCount: Int
        get() = scaffoldValue.expandedCount


    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    private val ThreePaneScaffoldValue.expandedCount: Int
        get() {
            var count = 0
            if (primary == PaneAdaptedValue.Expanded) {
                count++
            }
            if (secondary == PaneAdaptedValue.Expanded) {
                count++
            }
            if (tertiary == PaneAdaptedValue.Expanded) {
                count++
            }
            return count
        }

}

