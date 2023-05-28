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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import coil.compose.rememberAsyncImagePainter
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.FeedWithFavIcon
import com.geekorum.ttrss.ui.AppTheme


@Composable
fun FeedListNavigationMenu(
    user: String,
    server: String,
    feedSection: @Composable ColumnScope.() -> Unit,
    fab: (@Composable () -> Unit)? = null,
    manageFeedsSection: @Composable () -> Unit,
    onSettingsClicked: () -> Unit,
) {
    BoxWithNavigationMenuTypography {
        val scrollState = rememberScrollState()
        Column(Modifier.verticalScroll(scrollState)
            .withVerticalScrollBar(scrollState)
        ) {
            AppTheme(colors = AppTheme.DarkColors) {
                AccountHeader(user, server)
            }

            if (fab != null) {
                Box(Modifier
                    .fillMaxWidth()
                    .padding(ActiveIndicatorContentPadding),
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
                    Icon(AppTheme.Icons.Settings, contentDescription = null)
                },
            )
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
        }
    }
}

@Composable
private inline fun BoxWithNavigationMenuTypography(
    crossinline content: @Composable BoxScope.() -> Unit
) {
    val navTypography = MaterialTheme.typography.copy(
        button = MaterialTheme.typography.button.copy(
            letterSpacing = 0.4.sp,
            fontWeight = FontWeight.SemiBold
        )
    )
    MaterialTheme(typography = navTypography) {
        Box(content = content)
    }
}

private fun Modifier.withVerticalScrollBar(
    scrollState: ScrollState,
) : Modifier  = composed {
    val scrollBarAlpha by animateFloatAsState(targetValue = if (scrollState.isScrollInProgress)
        0.5f else 0f, animationSpec = tween(500), label = "scrollBarAlpha"
    )
    withVerticalScrollBar(scrollState, MaterialTheme.colors.onSurface, scrollBarAlpha)
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
    Surface {
        Box(contentAlignment = Alignment.BottomStart) {
            Image(painter = painterResource(id = R.drawable.drawer_header_dark),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(Modifier
                .height(56.dp)
                .padding(horizontal = NavigationItemPadding)) {
                Text(login, style = MaterialTheme.typography.body2, fontWeight = FontWeight.Bold)
                Text(server, modifier = Modifier.padding(top = 5.dp), style = MaterialTheme.typography.body2)
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
            val feed = feedWithFavIcon.feed
            var displayDropdownMenu by remember { mutableStateOf(false) }
            NavigationItem(
                feed.displayTitle.takeIf { it.isNotBlank() } ?: feed.title,
                selected = feed == selectedFeed,
                selectedForAction = displayDropdownMenu,
                onClick = {
                    onFeedSelected(feed)
                },
                onLongClick = {
                    if (feed.isAllArticlesFeed || !Feed.isVirtualFeed(feed.id)) {
                        displayDropdownMenu = true
                    }
                },
                icon = {
                    val iconVector = when {
                        feed.isArchivedFeed -> AppTheme.Icons.Inventory2
                        feed.isStarredFeed -> AppTheme.Icons.Star
                        feed.isPublishedFeed -> AppTheme.Icons.CheckBox
                        feed.isFreshFeed -> AppTheme.Icons.LocalCafe
                        feed.isAllArticlesFeed -> AppTheme.Icons.FolderOpen
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
                DropdownMenuItem(onClick = {
                    onMarkFeedAsReadClick(feed)
                    displayDropdownMenu = false
                }) {
                    Text(stringResource(R.string.menu_item_mark_feed_as_read))
                }
            }
        }
    }
}


@Composable
private fun NavigationDivider() {
    Divider(modifier = Modifier
        .padding(horizontal = NavigationItemPadding))
}

@OptIn(ExperimentalFoundationApi::class)
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
    Surface(
        modifier = modifier
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
            .height(NavigationItemHeight)
            .padding(horizontal = ActiveIndicatorPadding),
        shape = if (selected || selectedForAction)
            RoundedCornerShape(28.dp)
        else RectangleShape,
        color = when {
            selectedForAction -> MaterialTheme.colors.primary
            selected -> MaterialTheme.colors.secondary.copy(alpha = 0.4f)
            else -> MaterialTheme.colors.surface
        },
    ) {
        NavigationItemLayout(
            icon = icon,
            label = {
                Text(
                    label,
                    style = MaterialTheme.typography.button,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            badge = badge
        )
    }
}

@Composable
fun NavigationItemLayout(
    label: @Composable () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    badge: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .padding(start = ActiveIndicatorContentPadding, end = 12.dp)
                .size(24.dp)
        ) {
            if (icon != null) {
                icon()
            }
        }

        Box(Modifier.weight(1f)) {
            label()
        }

        if (badge != null) {
            Box(Modifier.padding(start = 12.dp, end = 24.dp)) {
                CompositionLocalProvider(LocalContentAlpha provides 0.4f) {
                    badge()
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Box(modifier = Modifier
        .height(NavigationItemHeight)
        .fillMaxWidth()
        .padding(start = NavigationItemPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(title,
            style = MaterialTheme.typography.button,
            color = MaterialTheme.colors.onSurface
        )
    }
}


@Preview
@Composable
fun PreviewFeedListNavigationMenu() {
    AppTheme {
        Surface(color = Color.Black.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(Modifier.requiredWidth(360.dp), elevation = 8.dp) {
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
                                Icon(AppTheme.Icons.Tune, contentDescription = null)
                            },
                        )
                    },
                    onSettingsClicked = {},
                )
            }
        }
    }
}

private val NavigationItemHeight = 48.dp // 56.dp
private val ActiveIndicatorPadding = 12.dp
private val ActiveIndicatorContentPadding = 16.dp
private val NavigationItemPadding = ActiveIndicatorPadding + ActiveIndicatorContentPadding
