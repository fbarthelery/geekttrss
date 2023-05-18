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
package com.geekorum.ttrss.article_details

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme3


@Composable
fun AnimatedArticleBottomAppBar(
    isVisible: MutableTransitionState<Boolean>,
    isUnread: Boolean,
    isStarred: Boolean,
    modifier: Modifier = Modifier,
    floatingActionButton: @Composable (() -> Unit)? = null,
    onToggleUnreadClick: () -> Unit,
    onStarredChange: (Boolean) -> Unit,
    onShareClick: () -> Unit,
) {
    Box(modifier.fillMaxWidth()) {
        AnimatedVisibility(isVisible,
            enter = slideInVertically(tween(225, easing = LinearOutSlowInEasing)) { it },
            exit = slideOutVertically(tween(175, easing = FastOutLinearInEasing)) { it },
        ) {
            ArticleBottomAppBar(
                isUnread = isUnread,
                isStarred = isStarred,
                floatingActionButton = if (isVisible.targetState && isVisible.isIdle)
                    floatingActionButton else null,
                onToggleUnreadClick = onToggleUnreadClick,
                onStarredChange = onStarredChange,
                onShareClick = onShareClick
            )
        }

        if (!isVisible.targetState || !isVisible.isIdle) {
            Box(
                Modifier
                    .padding(end = 16.dp, bottom = 12.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .align(Alignment.BottomEnd)
            ) {
                floatingActionButton?.invoke()
            }
        }
    }
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun ArticleBottomAppBar(
    isUnread: Boolean,
    isStarred: Boolean,
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
            IconToggleButton(isStarred, onCheckedChange = onStarredChange) {
                val image =
                    AnimatedImageVector.animatedVectorResource(id = R.drawable.avd_ic_star_filled)
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
        },
        floatingActionButton = floatingActionButton
    )
}

@Preview
@Composable
fun PreviewAnimatedArticleBottomAppBar() {
    AppTheme3 {
        Surface(Modifier.fillMaxWidth().height(150.dp)) {
            Box {
                val isVisible = remember { MutableTransitionState(true) }
                AnimatedArticleBottomAppBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    isVisible = isVisible,
                    isUnread = true,
                    isStarred = false,
                    onToggleUnreadClick = { isVisible.targetState = false },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                isVisible.targetState = true
                            }) {
                            Icon(
                                Icons.Default.OpenInBrowser,
                                contentDescription = null
                            )
                        }
                    },
                    onStarredChange = {},
                    onShareClick = {}
                )
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
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun PreviewM3ArticleTopAppBar() {
    AppTheme3 {
        ArticleTopAppBar(onNavigateUpClick = {})
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
                Icon(Icons.Default.ArrowBack, contentDescription = null)
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
