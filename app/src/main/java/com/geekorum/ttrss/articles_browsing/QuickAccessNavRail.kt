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
package com.geekorum.ttrss.articles_browsing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme3

data class QuickNavItem(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

//TODO shorten labels
val QuickNavItems = listOf(
    QuickNavItem(R.string.title_magazine,
        Icons.Filled.Newspaper, Icons.Outlined.Newspaper),
    QuickNavItem(R.string.label_all_articles_feeds_title,
        Icons.Filled.FolderOpen, Icons.Outlined.FolderOpen),
    QuickNavItem(R.string.label_fresh_feeds_title,
        Icons.Filled.LocalCafe, Icons.Outlined.LocalCafe),
    QuickNavItem(R.string.label_starred_feeds_title,
        Icons.Filled.Star, Icons.Outlined.StarOutline),
    QuickNavItem(R.string.activity_settings_title,
        Icons.Filled.Settings, Icons.Outlined.Settings),
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuickAccessNavRail(
    selectedItem: Int,
    onNavItemClick: (Int) -> Unit,
    onMenuButtonClick: () -> Unit,
    onRefreshFabClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    WideNavigationRail(
        modifier = modifier,
        arrangement = Arrangement.Center,
        header = {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onMenuButtonClick
                ) {
                    Icon(Icons.Filled.Menu, null)
                }

                MediumFloatingActionButton(
                    onClick = onRefreshFabClick,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Icon(Icons.Default.Refresh, null,
                        modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize))
                }
            }
        }
    ) {
        QuickNavItems.forEachIndexed { index, item ->
            WideNavigationRailItem(
                icon = {
                    Icon(
                        if (selectedItem == index) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.labelRes)
                    )
                },
                label = { Text(stringResource(item.labelRes)) },
                selected = selectedItem == index,
                onClick = { onNavItemClick(index) }
            )
        }
    }
}

@Preview
@Composable
private fun PreviewQuickAccessNavRail() {
    AppTheme3 {
        var selectedItem by remember { mutableIntStateOf(0) }
        QuickAccessNavRail(
            selectedItem = selectedItem,
            onNavItemClick = { selectedItem = it },
            onMenuButtonClick = {},
            onRefreshFabClick = {}
        )
    }
}

