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
package com.geekorum.ttrss.article_details

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme3
import com.materialkolor.ktx.harmonizeWithPrimary
import kotlin.math.roundToInt


@OptIn(ExperimentalAnimationGraphicsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ArticleBottomAppBar(
    isUnread: Boolean,
    isStarred: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: BottomAppBarScrollBehavior? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    onToggleUnreadClick: () -> Unit,
    onStarredChange: (Boolean) -> Unit,
    onShareClick: () -> Unit,
) {
    val containerColor = if (isUnread)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surface
    BottomAppBar(
        containerColor = containerColor,
        actions = {
            IconButton(onClick = onToggleUnreadClick) {
                Icon(Icons.Default.Archive, contentDescription = null)
            }
            IconToggleButton(isStarred, onCheckedChange = onStarredChange,
                colors = IconButtonDefaults.iconToggleButtonColors(
                    checkedContentColor = MaterialTheme.colorScheme.harmonizeWithPrimary(AppTheme3.Colors.MaterialGreenA700)
                )
            ) {
                val image =
                    AnimatedImageVector.animatedVectorResource(id = R.drawable.avd_ic_star_filled)
                Icon(
                    painter = rememberAnimatedVectorPainter(image, atEnd = isStarred),
                    contentDescription = null,
                )
            }
            IconButton(onClick = onShareClick) {
                Icon(Icons.Default.Share, contentDescription = null)
            }
        },
        floatingActionButton = floatingActionButton,
        // don't pass the scrollBehavior as it will only collapse the bottomAppBar content and not the insets padding.
        // instead we reimplement the collapsing using Modifier.layout
        // Passing the scrollBehavior allows to drag the bottomBar, but it's not necessary for us.
        // scrollBehavior = scrollBehavior,
        modifier = modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            // Sets the app bar's height offset to collapse the entire bar's height when content
            // is scrolled.
            scrollBehavior?.state?.heightOffsetLimit = -placeable.height.toFloat()

            val height = placeable.height + (scrollBehavior?.state?.heightOffset ?: 0f)
            layout(placeable.width, height.roundToInt()) {
                placeable.place(0, 0)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewArticleBottomAppBar() {
    AppTheme3 {
        val bottomAppBarScrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(bottomAppBarScrollBehavior.nestedScrollConnection),
            bottomBar = {
                var isUnread by remember { mutableStateOf(true) }
                var isStarred by remember { mutableStateOf(false) }
                ArticleBottomAppBar(
                    isUnread = isUnread,
                    isStarred = isStarred,
                    onToggleUnreadClick = { isUnread = !isUnread },
                    onStarredChange = { isStarred = it},
                    onShareClick = {},
                    scrollBehavior = bottomAppBarScrollBehavior
                )
            },
            floatingActionButtonPosition = FabPosition.EndOverlay,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {}) {
                    Icon(
                        Icons.Default.OpenInBrowser,
                        contentDescription = null
                    )
                }
            }
        ) {
            val scrollState = rememberScrollState()
            Column(Modifier.padding(it).fillMaxWidth().verticalScroll(scrollState)) {
                repeat(199) {
                    Text("text $it")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavigateUpClick: () -> Unit,
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {},
        navigationIcon = {
            IconButton(onClick = onNavigateUpClick) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun PreviewArticleTopAppBar() {
    AppTheme3 {
        ArticleTopAppBar(onNavigateUpClick = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleFloatingTopAppBar(
    onNavigateUpClick: () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
    // Sets the app bar's height offset to collapse the entire bar's height when content is
    // scrolled.
    val heightOffsetLimit =
        with(LocalDensity.current) { ((-64).dp).toPx() }
    SideEffect {
        if (scrollBehavior?.state?.heightOffsetLimit != heightOffsetLimit) {
            scrollBehavior?.state?.heightOffsetLimit = heightOffsetLimit
        }
    }
    val colorTransitionFraction = scrollBehavior?.state?.overlappedFraction ?: 0f
    val iconButtonColors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = lerp(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.secondaryContainer,
            FastOutLinearInEasing.transform(colorTransitionFraction)
        )
    )
    val statusBarColor = lerp(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceContainer,
        FastOutLinearInEasing.transform(colorTransitionFraction)
    )

    Column(modifier) {
        Box(modifier = Modifier
            .windowInsetsTopHeight(windowInsets)
            .fillMaxWidth()
            .background(color = statusBarColor))

        FilledTonalIconButton(onClick = onNavigateUpClick, colors = iconButtonColors,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewArticleFloatingTopAppBar() {
    AppTheme3 {
        ArticleFloatingTopAppBar(onNavigateUpClick = {})
    }
}

@OptIn(ExperimentalAnimationGraphicsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ArticleTopActionsBar(
    title: @Composable () -> Unit,
    isUnread: Boolean,
    isStarred: Boolean,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavigateUpClick: () -> Unit,
    onToggleUnreadClick: () -> Unit,
    onStarredChange: (Boolean) -> Unit,
    onShareClick: () -> Unit,
) {
    val containerColor = if (isUnread)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surface
    val contentColor = if (isUnread)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurface

    val actionContentColor = if (isUnread)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = containerColor,
        navigationIconContentColor = contentColor,
        titleContentColor = contentColor,
        actionIconContentColor = actionContentColor
    )
    TopAppBar(
        title = title,
        colors = colors,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = onNavigateUpClick) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = onToggleUnreadClick) {
                Icon(Icons.Default.Archive, contentDescription = null)
            }
            IconToggleButton(isStarred, onCheckedChange = onStarredChange) {
                val image = AnimatedImageVector.animatedVectorResource(id = R.drawable.avd_ic_star_filled)
                val starColor = if (isStarred) {
                    Color.Unspecified
                } else {
                    LocalContentColor.current
                }
                Icon(
                    painter = rememberAnimatedVectorPainter(image, atEnd = isStarred),
                    contentDescription = null,
                    tint = starColor,
                )
            }
            IconButton(onClick = onShareClick) {
                Icon(Icons.Default.Share, contentDescription = null)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewM3ArticleTopActionsBar() {
    AppTheme3 {
        ArticleTopActionsBar(
            title = { Text("Article title") },
            isUnread = true,
            isStarred = false,
            onNavigateUpClick = { },
            onToggleUnreadClick = { },
            onStarredChange = { },
            onShareClick = { }
        )
    }
}
