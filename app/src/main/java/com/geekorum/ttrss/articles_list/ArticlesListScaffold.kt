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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.*
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme
import kotlinx.coroutines.launch

@Composable
fun ArticlesListScaffold(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    topBar: @Composable () -> Unit,
    navigationMenu: @Composable (ColumnScope.() -> Unit),
    drawerGesturesEnabled: Boolean = true,
    floatingActionButton: @Composable () -> Unit = {},
    bannerContent: @Composable (PaddingValues) -> Unit = {},
    undoUnreadSnackBar: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    ArticleListScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        hasFixedDrawer = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded,
        drawerGesturesEnabled = drawerGesturesEnabled,
        topBar = topBar,
        navigationMenu = navigationMenu,
        floatingActionButton = floatingActionButton,
        bannerContent = bannerContent,
        undoUnreadSnackBar = undoUnreadSnackBar,
        content = content,
    )
}

@Composable
private fun ArticleListScaffold(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    topBar: @Composable () -> Unit,
    navigationMenu: @Composable (ColumnScope.() -> Unit),
    hasFixedDrawer: Boolean = false,
    drawerGesturesEnabled: Boolean = true,
    floatingActionButton: @Composable () -> Unit = {},
    bannerContent: @Composable (PaddingValues) -> Unit,
    undoUnreadSnackBar: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val drawerContent: (@Composable ColumnScope.() -> Unit)? =
        if (hasFixedDrawer) null else {
        {
            navigationMenu()
        }
    }

    Surface {
        Row {
            if (hasFixedDrawer) {
                Column(
                    Modifier
                        .width(360.dp)
                        .fillMaxHeight()) {
                    navigationMenu()
                }
            }

            val bannerHeightState = remember { mutableStateOf<Float?>(null) }

            //TODO reimplement our own Scaffold and ScaffoldLayout
            Scaffold(
                modifier = modifier,
                scaffoldState = scaffoldState,
                topBar = topBar,
                floatingActionButton = {
                    val paddingBottom  = with(LocalDensity.current) {
                        (bannerHeightState.value ?: 0f).toDp()
                    }
                    Box(Modifier.padding(bottom = paddingBottom)) {
                        floatingActionButton()
                    }
                },
                drawerContent = drawerContent,
                drawerShape = MaterialTheme.shapes.large.copy(topStart = ZeroCornerSize, bottomStart = ZeroCornerSize),
                drawerGesturesEnabled = drawerGesturesEnabled,
                snackbarHost = { snackbarHostState ->
                    SnackbarHostWithCustomSnackBar(snackbarHostState, undoUnreadSnackBar)
                },
                content = { contentPadding ->
                    ContentWithBottomBanner(
                        bannerHeightState = bannerHeightState,
                        contentPadding = contentPadding,
                        bannerContent = bannerContent,
                        content = content
                    )

                    // need to be called after content so if there is a navhost in content
                    // this is registered latter
                    val coroutineScope = rememberCoroutineScope()
                    BackHandler(enabled = scaffoldState.drawerState.isOpen) {
                        coroutineScope.launch {
                            scaffoldState.drawerState.close()
                        }
                    }
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun SnackbarHostWithCustomSnackBar(
    snackBarHostState: SnackbarHostState,
    customSnackBar: @Composable (() -> Unit)?
) {
    Box {
        SnackbarHost(snackBarHostState) {
            Snackbar(it)
        }
        val showCustomSnackbar = customSnackBar != null
        AnimatedVisibility(
            visible = showCustomSnackbar,
            enter = fadeIn(tween(150, easing = LinearEasing)) + scaleIn(
                tween(
                    150,
                    easing = FastOutSlowInEasing
                ), initialScale = 0.8f
            ),
            exit = fadeOut(tween(75, easing = LinearEasing)) + scaleOut(
                tween(
                    75,
                    easing = FastOutSlowInEasing
                ), targetScale = 0.8f
            )
        ) {
            customSnackBar?.invoke()
        }
    }
}


@Composable
private fun ContentWithBottomBanner(
    bannerHeightState: MutableState<Float?>,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    bannerContent: @Composable (PaddingValues) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        val ltr = LocalLayoutDirection.current
        val bannerHeightDp = with(LocalDensity.current) {
            bannerHeightState.value?.toDp() ?: 0.dp
        }
        val actualContentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(ltr),
            end = contentPadding.calculateEndPadding(ltr),
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + bannerHeightDp
        )
        content(actualContentPadding)

        Box(
            Modifier
                .align(Alignment.BottomStart)
                .onGloballyPositioned {
                    bannerHeightState.value = it.size.height.toFloat()
                }
        ) {
            val bannerContentPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(ltr),
                end = contentPadding.calculateEndPadding(ltr),
                top = 0.dp,
                bottom = contentPadding.calculateBottomPadding()
            )
            bannerContent(bannerContentPadding)
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
fun PreviewArticlesListScaffoldPhone() {
    BoxWithConstraints {
        val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight) )
        AppTheme {
            val scaffoldState = rememberScaffoldState()
            val coroutineScope = rememberCoroutineScope()
            var showBanner by remember { mutableStateOf(false) }

            ArticlesListScaffold(
                windowSizeClass = windowSizeClass,
                scaffoldState = scaffoldState,
                topBar = {
                    TopAppBar(
                        title = { Text("Magazine") },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        scaffoldState.drawerState.open()
                                    }
                                }) {
                                Icon(Icons.Default.Menu, contentDescription = null)
                            }
                        }
                    )
                },
                navigationMenu = {
                    FeedListNavigationMenu(
                        user = "user",
                        server = "server",
                        feedSection = {},
                        manageFeedsSection = {
                            NavigationItem(
                                stringResource(R.string.title_manage_feeds),
                                selected = false,
                                onClick = {},
                                icon = {
                                    Icon(AppTheme.Icons.Tune, contentDescription = null)
                                },
                            )
                        },
                        onSettingsClicked = { })
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        showBanner = !showBanner
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = "refresh"
                        )
                    }
                },
                bannerContent = {
                    SampleBanner(showBanner)
                },
                undoUnreadSnackBar = {
                    SampleUndoSnackbar()
                },
                content = { contentPadding ->
                    val items = List(100) { "Item $it" }
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = contentPadding ) {
                        items(items) {
                            Text(it)
                        }
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun PreviewArticlesListScaffoldTablet() {
    BoxWithConstraints {
        val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight) )
        AppTheme {
            val scaffoldState = rememberScaffoldState()
            val coroutineScope = rememberCoroutineScope()
            var showBanner by remember { mutableStateOf(false) }

            ArticlesListScaffold(
                windowSizeClass = windowSizeClass,
                scaffoldState = scaffoldState,
                topBar = {
                    TopAppBar(
                        title = { Text("Magazine") },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        scaffoldState.drawerState.open()
                                    }
                                }) {
                                Icon(Icons.Default.Menu, contentDescription = null)
                            }
                        },
                    )
                },
                navigationMenu = {
                    FeedListNavigationMenu(
                        user = "user",
                        server = "server",
                        fab = {
                            ExtendedFloatingActionButton(
                                text = { Text(stringResource(R.string.btn_refresh)) },
                                icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                                onClick = {}
                            )
                        },
                        feedSection = { },
                        manageFeedsSection = {
                            NavigationItem(
                                stringResource(R.string.title_manage_feeds),
                                selected = false,
                                onClick = {},
                                icon = {
                                    Icon(AppTheme.Icons.Tune, contentDescription = null)
                                },
                            )
                        },
                        onSettingsClicked = {})
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        showBanner = !showBanner
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = "refresh"
                        )
                    }
                },
                bannerContent = {
                    SampleBanner(showBanner = showBanner)
                },
                content = { contentPadding ->
                    val items = List(100) { "Item $it" }
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = contentPadding ) {
                        items(items) {
                            Text(it)
                        }
                    }
                },
            )
        }
    }
}


@Composable
private fun SampleBanner(showBanner: Boolean) {
    AnimatedVisibility(
        showBanner,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colors.secondaryVariant
            ) {
                Text(
                    "Hello banner",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SampleUndoSnackbar() {
    Snackbar(modifier = Modifier.padding(12.dp),
        action = {
            Text("Undo")
        }) {
        Text("4 articles marked as read")
    }
}


