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
package com.geekorum.ttrss.articles_list

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme3
import com.geekorum.ttrss.ui.components.rememberWindowInsetsController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ArticlesListScaffold(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    undoUnreadSnackbarHostState: UndoUnreadSnackbarHostState = remember { UndoUnreadSnackbarHostState() },
    topBar: @Composable () -> Unit,
    navigationMenu: @Composable (ColumnScope.() -> Unit),
    drawerGesturesEnabled: Boolean = true,
    floatingActionButton: @Composable () -> Unit = {},
    bannerContent: @Composable (PaddingValues) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    ArticleListScaffold(
        modifier = modifier,
        drawerState = drawerState,
        snackbarHostState = snackbarHostState,
        undoUnreadSnackbarHostState = undoUnreadSnackbarHostState,
        hasPermanentDrawer = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded,
        drawerGesturesEnabled = drawerGesturesEnabled,
        topBar = topBar,
        navigationMenu = navigationMenu,
        floatingActionButton = floatingActionButton,
        bannerContent = bannerContent,
        content = content,
    )
}

@Composable
private fun ArticleListScaffold(
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    undoUnreadSnackbarHostState: UndoUnreadSnackbarHostState = remember { UndoUnreadSnackbarHostState() },
    topBar: @Composable () -> Unit,
    navigationMenu: @Composable (ColumnScope.() -> Unit),
    hasPermanentDrawer: Boolean = false,
    drawerGesturesEnabled: Boolean = true,
    floatingActionButton: @Composable () -> Unit = {},
    bannerContent: @Composable (PaddingValues) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val contentScaffold = remember {
        movableContentOf {
            ArticlesListContentScaffold(
                snackbarHostState = snackbarHostState,
                topBar = topBar,
                bannerContent = bannerContent,
                floatingActionButton = floatingActionButton,
                undoUnreadSnackbarHostState = undoUnreadSnackbarHostState,
                content = content
            )
        }
    }

    if (hasPermanentDrawer) {
        PermanentNavigationDrawer(
            modifier = modifier,
            drawerContent = {
                PermanentDrawerSheet(
                    windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Start),
                    modifier = modifier,
                    content = navigationMenu
                )
            }
        ) {
            contentScaffold()
        }
    } else  {
        val windowInsetsController = rememberWindowInsetsController()
        val systemInDarkTheme = isSystemInDarkTheme()
        LaunchedEffect(systemInDarkTheme, drawerState.isOpen) {
            windowInsetsController.isAppearanceLightStatusBars = !drawerState.isOpen && !systemInDarkTheme
        }

        ModalNavigationDrawer(
            modifier = modifier,
            drawerState = drawerState,
            gesturesEnabled = drawerGesturesEnabled,
            drawerContent = {
                ModalDrawerSheet(
                    windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Start),
                    modifier = modifier,
                    content = navigationMenu
                )
            }
        ){
            contentScaffold()
            // need to be called after content so if there is a navhost in content
            // this is registered latter
            // this doesn't seem to work anymore with movableContentOf or m3
            val coroutineScope = rememberCoroutineScope()
            if (drawerState.isOpen) {
                BackHandler {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                }
            }
        }
    }
}


@Composable
private fun ArticlesListContentScaffold(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    undoUnreadSnackbarHostState: UndoUnreadSnackbarHostState,
    topBar: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    bannerContent: @Composable (PaddingValues) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val bannerHeightState = remember { mutableStateOf<Float?>(null) }

    //TODO reimplement our own Scaffold and ScaffoldLayout
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        floatingActionButton = {
            val paddingBottom  = with(LocalDensity.current) {
                (bannerHeightState.value ?: 0f).toDp()
            }
            Box(Modifier.padding(bottom = paddingBottom)) {
                floatingActionButton()
            }
        },
        snackbarHost = {
            SnackbarHostWithCustomSnackBar(snackbarHostState, undoUnreadSnackbarHostState)
        },
        content = { contentPadding ->
            ContentWithBottomBanner(
                bannerHeightState = bannerHeightState,
                contentPadding = contentPadding,
                bannerContent = bannerContent,
                content = content
            )

        }
    )
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun SnackbarHostWithCustomSnackBar(
    snackBarHostState: SnackbarHostState,
    undoUnreadSnackbarHostState: UndoUnreadSnackbarHostState,
) {
    Box {
        SnackbarHost(snackBarHostState) {
            Snackbar(it)
        }

        val currentUndoMessage = undoUnreadSnackbarHostState.currentSnackbarMessage
        val showCustomSnackbar = (currentUndoMessage?.nbArticles ?: 0) > 0
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
            if (currentUndoMessage != null) {
                UndoUnreadSnackBar(undoUnreadSnackbarMessage = currentUndoMessage)
            }
        }
    }
}

@Stable
class UndoUnreadSnackbarHostState {
    var currentSnackbarMessage by mutableStateOf<UndoReadSnackbarMessage?>(null)
}

data class UndoReadSnackbarMessage(
    val nbArticles: Int,
    val onAction: () -> Unit,
    val onDismiss: () -> Unit
)

@Composable
private fun UndoUnreadSnackBar(undoUnreadSnackbarMessage: UndoReadSnackbarMessage) {
    Snackbar(modifier = Modifier.padding(12.dp),
        action = @Composable {
            TextButton(
                colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current),
                onClick = { undoUnreadSnackbarMessage.onAction() },
                content = { Text(stringResource(R.string.undo_set_articles_read_btn)) }
            )
        }) {
        val nbArticles = undoUnreadSnackbarMessage.nbArticles
        Text(
            pluralStringResource(
                R.plurals.undo_set_articles_read_text,
                nbArticles,
                nbArticles
            )
        )
    }
    LaunchedEffect(undoUnreadSnackbarMessage.nbArticles) {
        delay(2_750)
        undoUnreadSnackbarMessage.onDismiss()
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewArticlesListScaffoldPhone() {
    BoxWithConstraints {
        val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight) )
        AppTheme3 {
            val snackbarHostState = remember { SnackbarHostState() }
            val undoUnreadSnackbarHostState = remember { UndoUnreadSnackbarHostState() }
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val coroutineScope = rememberCoroutineScope()
            var showBanner by remember { mutableStateOf(false) }

            ArticlesListScaffold(
                windowSizeClass = windowSizeClass,
                snackbarHostState = snackbarHostState,
                undoUnreadSnackbarHostState = undoUnreadSnackbarHostState,
                drawerState = drawerState,
                topBar = {
                    TopAppBar(
                        title = { Text("Magazine") },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.open()
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
                                    Icon(AppTheme3.Icons.Tune, contentDescription = null)
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
                            AppTheme3.Icons.Refresh,
                            contentDescription = "refresh"
                        )
                    }
                },
                bannerContent = {
                    SampleBanner(showBanner)
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun PreviewArticlesListScaffoldTablet() {
    BoxWithConstraints {
        val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight) )
        AppTheme3 {
            val snackbarHostState = remember { SnackbarHostState() }
            val drawerState = rememberDrawerState(DrawerValue.Closed)

            var showBanner by remember { mutableStateOf(false) }

            ArticlesListScaffold(
                windowSizeClass = windowSizeClass,
                drawerState = drawerState,
                snackbarHostState = snackbarHostState,
                topBar = {
                    TopAppBar(
                        title = { Text("Magazine") },
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
                                    Icon(AppTheme3.Icons.Tune, contentDescription = null)
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
                            AppTheme3.Icons.Refresh,
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
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    "Hello banner",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}

@Composable
@Preview
private fun PreviewUndoUnreadSnackbar() {
    AppTheme3 {
        Column {
            val msg = UndoReadSnackbarMessage(3, onAction = {}, onDismiss = {})
            UndoUnreadSnackBar(undoUnreadSnackbarMessage = msg)
            Spacer(modifier = Modifier.height(32.dp))
            Snackbar(modifier = Modifier.padding(12.dp),
                action = {
                    Text("Undo")
                }) {
                Text("With a normal snackbar")
            }

        }
    }
}
