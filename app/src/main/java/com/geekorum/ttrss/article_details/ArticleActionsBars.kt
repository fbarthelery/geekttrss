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

import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme3
import com.geekorum.ttrss.ui.components.OpenInBrowserIcon
import com.geekorum.ttrss.ui.components.rememberWindowInsetsController
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
    // sync 3 buttons navbar colors
    val lightNavBar = containerColor.luminance() > 0.5
    val windowInsetsController = rememberWindowInsetsController()
    LaunchedEffect(lightNavBar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            windowInsetsController.isAppearanceLightNavigationBars = lightNavBar
        }
    }
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
    val scrolledContainerColor = if (isUnread)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceContainer

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
        scrolledContainerColor = scrolledContainerColor,
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

@OptIn(ExperimentalAnimationGraphicsApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun ArticleHorizontalFloatingToolbar(
    isUnread: Boolean,
    isStarred: Boolean,
    browserApplicationIcon: Drawable?,
    modifier: Modifier = Modifier,
    scrollBehavior: FloatingToolbarScrollBehavior? = null,
    onToggleUnreadClick: () -> Unit,
    onStarredChange: (Boolean) -> Unit,
    onShareClick: () -> Unit,
    onOpenInBrowserClick: () -> Unit,
) {
    val colors = if (isUnread) {
        FloatingToolbarDefaults.vibrantFloatingToolbarColors()
    } else
        FloatingToolbarDefaults.vibrantFloatingToolbarColors(
            toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            toolbarContentColor = MaterialTheme.colorScheme.onSurface,
//            fabContainerColor = MaterialTheme.colorScheme.surfaceContainer,
//            fabContentColor = MaterialTheme.colorScheme.onSurface
        )

    HorizontalFloatingToolbar(
        modifier = modifier,
        expanded = true,
        floatingActionButton = {
            FloatingToolbarDefaults.VibrantFloatingActionButton(
                containerColor = colors.fabContainerColor,
                contentColor = colors.fabContentColor,
                onClick = onOpenInBrowserClick
            ) {
                OpenInBrowserIcon(browserApplicationIcon, contentDescription = stringResource(R.string.open_article_in_browser))
            }
        },
        colors = colors,
        scrollBehavior = scrollBehavior,
    ) {
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
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Preview(showSystemUi = true, device = "spec:parent=pixel_5,navigation=buttons")
@Composable
fun PreviewArticleHorizontalFloatingToolbar() {
    AppTheme3 {
        val toolbarScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
            FloatingToolbarExitDirection.Bottom)
        Scaffold(
            modifier = Modifier.nestedScroll(toolbarScrollBehavior),
            floatingActionButtonPosition = FabPosition.Center,
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars)
        ) {
            Box(Modifier.padding(it)) {
                val scrollState = rememberScrollState()
                Column(Modifier.fillMaxWidth().verticalScroll(scrollState)) {
                    repeat(199) {
                        Text("text $it")
                    }
                }

                var isUnread by remember { mutableStateOf(true) }
                var isStarred by remember { mutableStateOf(false) }
                ArticleHorizontalFloatingToolbar(
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .offset(y = -FloatingToolbarDefaults.ScreenOffset)
                    ,
                    isUnread = isUnread,
                    isStarred = isStarred,
                    browserApplicationIcon = null,
                    onToggleUnreadClick = { isUnread = !isUnread },
                    onStarredChange = { isStarred = it},
                    onShareClick = {},
                    onOpenInBrowserClick = {},
                    scrollBehavior = toolbarScrollBehavior
                )
            }
        }
    }
}


@OptIn(ExperimentalAnimationGraphicsApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun ArticleVerticalFloatingToolbar(
    browserApplicationIcon: Drawable?,
    isUnread: Boolean,
    isStarred: Boolean,
    onToggleUnreadClick: () -> Unit,
    onStarredChange: (Boolean) -> Unit,
    onShareClick: () -> Unit,
    onOpenInBrowserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isUnread)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface
    val contentColor = if (isUnread)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurface

    val colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(
        toolbarContainerColor = containerColor,
        toolbarContentColor = contentColor
    )
    VerticalFloatingToolbar(
        expanded = true,
//        shape = MaterialTheme.shapes.small,
        modifier = modifier,
        colors = colors,
        floatingActionButton = {
            FloatingToolbarDefaults.VibrantFloatingActionButton(onClick = onOpenInBrowserClick) {
                OpenInBrowserIcon(browserApplicationIcon, contentDescription = stringResource(R.string.open_article_in_browser))
            }
        },
    ) {
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
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewArticleTopActionsBar() {
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
private fun PreviewArticleVerticalFloatingToolbar() {
    AppTheme3 {
        var isUnread by remember { mutableStateOf(true) }
        Box(Modifier.height(400.dp)
            .width(100.dp)
            .background(MaterialTheme.colorScheme.surface)
        ) {
            ArticleVerticalFloatingToolbar(
                browserApplicationIcon = null,
                isUnread = isUnread,
                isStarred = false,
                onToggleUnreadClick = { isUnread = !isUnread },
                onStarredChange = { },
                onShareClick = { },
                onOpenInBrowserClick = {},
                modifier = Modifier.align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            )
        }
    }
}
