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
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import com.geekorum.ttrss.data.Feed


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class BrowsingSceneDecoratorStrategy<T: Any>(
    val listDetailSceneStrategy: ListDetailSceneStrategy<T>,
    private val navigateToFeed: (Feed) -> Unit,
    private val navigateToSettings: () -> Unit,
    private val navigateToMagazine: () -> Unit,
) : SceneDecoratorStrategy<T> {


    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> {
        if (BrowsingRoleKey in scene.metadata) {
            return BrowsingSceneDecorator(
                listDetailsScene = scene,
                paneScaffoldDirective = listDetailSceneStrategy.directive,
                adaptStrategies = listDetailSceneStrategy.adaptStrategies,
                navigateToFeed = navigateToFeed,
                navigateToMagazine = navigateToMagazine,
                navigateToSettings = navigateToSettings
            )
        }
        return scene
    }

    internal sealed interface PaneMetadata {
        val sceneKey: Any
    }

    internal class BrowsingMetadata(
        override val sceneKey: Any,
        val feedSelectedItem: FeedNavigationMenuState.SelectedItem,
        val articlePlaceHolder: @Composable () -> Unit,
    ): PaneMetadata

    internal class DetailMetadata(override val sceneKey: Any) : PaneMetadata

    companion object {
        internal object BrowsingRoleKey : NavMetadataKey<PaneMetadata>

        fun browsingPane(
            feedSelectedItem: FeedNavigationMenuState.SelectedItem,
            sceneKey: Any = Unit,
            articlePlaceHolder: @Composable () -> Unit = {},
        ): Map<String, Any> = metadata { put(BrowsingRoleKey, BrowsingMetadata(sceneKey, feedSelectedItem, articlePlaceHolder)) } +
                ListDetailSceneStrategy.listPane(sceneKey, {
                    articlePlaceHolder()
                })

        fun articlePane(sceneKey: Any = Unit): Map<String, Any> =
            metadata { put(BrowsingRoleKey, DetailMetadata(sceneKey)) } +
                    ListDetailSceneStrategy.detailPane(sceneKey)
    }
}


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun <T: Any> rememberBrowsingSceneDecoratorStrategy(
    navigateToFeed: (Feed) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToMagazine: () -> Unit,
): BrowsingSceneDecoratorStrategy<T> {
    val listDetailsSceneStrategy = rememberListDetailSceneStrategy<T>(
        shouldHandleSinglePaneLayout = true,
    )
    return remember(navigateToFeed, navigateToSettings, navigateToMagazine) {
        BrowsingSceneDecoratorStrategy(
            listDetailSceneStrategy = listDetailsSceneStrategy,
            navigateToFeed = navigateToFeed,
            navigateToSettings = navigateToSettings,
            navigateToMagazine = navigateToMagazine
        )
    }
}