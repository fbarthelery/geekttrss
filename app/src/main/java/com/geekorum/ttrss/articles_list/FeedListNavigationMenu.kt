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

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.rememberAsyncImagePainter
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.FeedWithFavIcon
import com.geekorum.ttrss.ui.AppTheme3


@Composable
fun FeedListNavigationMenu(
    user: String,
    server: String,
    feedSection: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    fab: (@Composable () -> Unit)? = null,
    manageFeedsSection: @Composable () -> Unit,
    onSettingsClicked: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier
            .verticalScroll(scrollState)
            .withVerticalScrollBar(scrollState)
    ) {
        AccountHeader(user, server)

        if (fab != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .padding(vertical = 12.dp),
                propagateMinConstraints = true){
                fab()
            }
        }

        feedSection()

        NavigationDivider()
        manageFeedsSection()
        NavigationDivider()
        NavigationItem(
            stringResource(R.string.activity_settings_title),
            selected = false,
            onClick = onSettingsClicked,
            icon = {
                Icon(AppTheme3.Icons.Settings, contentDescription = null)
            },
        )
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
    }
}

private fun Modifier.withVerticalScrollBar(
    scrollState: ScrollState,
) : Modifier  = composed {
    val scrollBarAlpha by animateFloatAsState(targetValue = if (scrollState.isScrollInProgress)
        0.5f else 0f, animationSpec = tween(500), label = "scrollBarAlpha"
    )
    withVerticalScrollBar(scrollState, MaterialTheme.colorScheme.onSurface, scrollBarAlpha)
}

private fun Modifier.withVerticalScrollBar(
    scrollState: ScrollState,
    color: Color,
    alpha: Float = 1f
) = drawWithContent {
    drawContent()
    val scrollBarHeight = 56.dp.toPx()
    val scrollBarWidth = 4.dp.toPx()
    val scrollBarY = lerp(0f, size.height - scrollBarHeight, (scrollState.value) / scrollState.maxValue.toFloat())
    val scrollBarX = size.width - scrollBarWidth
    drawRoundRect(
        color = color,
        topLeft = Offset(x = scrollBarX, y = scrollBarY),
        size = Size(width = scrollBarWidth, height = scrollBarHeight),
        cornerRadius = CornerRadius(4.dp.toPx()),
        alpha = alpha,
    )
}

@Composable
fun AccountHeader(login: String, server: String) {
    Surface(contentColor = Color.White) {
        Box(contentAlignment = Alignment.BottomStart) {
            Image(painter = painterResource(id = R.drawable.drawer_header_dark),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(
                Modifier
                    .padding(horizontal = NavigationItemPadding, vertical = 16.dp)) {
                Text(login, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(server, modifier = Modifier.padding(top = 5.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}


@Composable
fun FeedSection(
    feeds: List<FeedWithFavIcon>,
    isMagazineSelected: Boolean,
    selectedFeed: Feed?,
    onMagazineSelected: () -> Unit,
    onFeedSelected: (Feed) -> Unit,
    onMarkFeedAsReadClick: (Feed) -> Unit,
) {
    SectionLabel(stringResource(R.string.title_feeds_menu))
    NavigationItem(stringResource(R.string.title_magazine),
        icon = { Icon(painterResource(R.drawable.ic_newspaper_24), contentDescription = null) },
        selected = isMagazineSelected,
        selectedForAction = false,
        onLongClick = null,
        onClick = onMagazineSelected)
    for (feedWithFavIcon in feeds) {
        Box {
            var displayDropdownMenu by remember { mutableStateOf(false) }
            val feed = feedWithFavIcon.feed
            NavigationItem(
                feed.displayTitle.takeIf { it.isNotBlank() } ?: feed.title,
                selected = feed == selectedFeed,
                selectedForAction = displayDropdownMenu,
                onClick = {
                    onFeedSelected(feed)
                },
                onLongClick = {
                    displayDropdownMenu = true
                },
                icon = {
                    val iconVector = when {
                        feed.isArchivedFeed -> AppTheme3.Icons.Inventory2
                        feed.isStarredFeed -> AppTheme3.Icons.Star
                        feed.isPublishedFeed -> AppTheme3.Icons.CheckBox
                        feed.isFreshFeed -> AppTheme3.Icons.LocalCafe
                        feed.isAllArticlesFeed -> AppTheme3.Icons.FolderOpen
                        else -> null
                    }
                    if (iconVector != null) {
                        Icon(iconVector, contentDescription = null)
                    } else {
                        val feedIconPainter = rememberAsyncImagePainter(
                            model = feedWithFavIcon.favIcon?.url,
                            placeholder = painterResource(R.drawable.ic_rss_feed_orange),
                            fallback = painterResource(R.drawable.ic_rss_feed_orange),
                            error = painterResource(R.drawable.ic_rss_feed_orange),
                        )
                        Image(
                            painter = feedIconPainter,
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                badge = {
                    if (feed.unreadCount > 0) {
                        Text(feed.unreadCount.toString())
                    }
                }
            )

 	DropdownMenu(expanded = displayDropdownMenu,
                onDismissRequest = { displayDropdownMenu = false },
                offset = DpOffset(x = 16.dp, y = (-8).dp)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.menu_item_mark_feed_as_read))
                    },
                    onClick = {
                        onMarkFeedAsReadClick(feed)
                        displayDropdownMenu = false
                    }
                )
            }
        }
    }
}


@Composable
private fun NavigationDivider() {
    Divider(modifier = Modifier
        .padding(horizontal = NavigationItemPadding))
}

@Composable
fun NavigationItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedForAction: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
) {
    val colors = if (selectedForAction)
        NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    else NavigationDrawerItemDefaults.colors()
    NavigationDrawerItem(
        label = {
            Text(label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        },
        selected = selected || selectedForAction,
        colors = colors,
        onClick = onClick,
        icon = icon,
        badge = badge,
        modifier = modifier
            .interceptLongClick(onLongClick)
            .padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

private fun Modifier.interceptLongClick(
    onLongClick: (() -> Unit)?,
): Modifier {
    return if (onLongClick != null) {
        pointerInput(onLongClick) {
            awaitEachGesture {
                val longPressTimeout = viewConfiguration.longPressTimeoutMillis
                awaitFirstDown(pass = PointerEventPass.Initial)
                try {
                    withTimeout(longPressTimeout) {
                        waitForUpOrCancellation(PointerEventPass.Initial)
                    }
                } catch (e: PointerEventTimeoutCancellationException) {
                    // we got the long press
                    onLongClick()

                    // consume the children's click handling
                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                    event.changes.forEach { it.consume() }
                }
            }
        }.semantics(mergeDescendants = true) {
            this.onLongClick(action = {
                onLongClick()
                true
            })
        }
    } else Modifier
}


@Composable
private fun SectionLabel(title: String) {
    Box(modifier = Modifier
        .height(NavigationItemHeight)
        .fillMaxWidth()
        .padding(horizontal = NavigationItemPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun PreviewFeedListNavigationMenu() {
    AppTheme3 {
        Surface(color = Color.Black.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            ModalDrawerSheet {
                val feeds = listOf(
                    FeedWithFavIcon(
                        feed = Feed(
                            id = Feed.FEED_ID_ALL_ARTICLES,
                            title = "All articles",
                            unreadCount = 290,
                        ),
                        favIcon = null
                    ),
                    FeedWithFavIcon(
                        feed = Feed(
                            id = Feed.FEED_ID_FRESH,
                            title = "Fresh articles",
                        ),
                        favIcon = null
                    ),
                    FeedWithFavIcon(
                        feed = Feed(
                            id = Feed.FEED_ID_STARRED,
                            title = "Starred articles",
                        ),
                        favIcon = null
                    ),
                    FeedWithFavIcon(
                        feed = Feed(
                            id = 2,
                            title = "Frandroid",
                            unreadCount = 42,
                        ),
                        favIcon = null
                    ),
                    FeedWithFavIcon(
                        feed = Feed(
                            id = 3,
                            title = "Gentoo universe",
                            unreadCount = 10,
                        ),
                        favIcon = null
                    ),
                    FeedWithFavIcon(
                        feed = Feed(
                            id = 4,
                            title = "LinuxFr",
                            unreadCount = 8,
                        ),
                        favIcon = null
                    ),
                )
                FeedListNavigationMenu(
                    user = "test",
                    server = "example.org",
                    fab = {
                        ExtendedFloatingActionButton(
                            onClick = {},
                            icon = {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                            },
                            text = {
                                Text("Refresh")
                            })
                    },
                    feedSection = {
                        var selectedFeed by remember {
                            mutableStateOf<Feed?>(null)
                        }
                        var isMagazineSelected by remember {
                            mutableStateOf(true)
                        }
                        FeedSection(
                            feeds,
                            selectedFeed = selectedFeed,
                            onFeedSelected = {
                                selectedFeed = it
                                isMagazineSelected = false
                            },
                            isMagazineSelected = isMagazineSelected,
                            onMagazineSelected = {
                                selectedFeed = null
                                isMagazineSelected = true
                            },
                            onMarkFeedAsReadClick = { }
                        )
                    },
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
                    onSettingsClicked = {},
                )
            }
        }
    }
}

private val NavigationItemHeight = 56.dp
private val NavigationItemPadding = 28.dp
