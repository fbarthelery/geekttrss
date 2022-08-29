/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.ui.AppTheme


@Composable
fun FeedListNavigationMenu(
    user: String,
    server: String,
    feedSection: @Composable ColumnScope.() -> Unit,
    onManageFeedsClicked: () -> Unit,
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

            feedSection()

            NavigationDivider()
            NavigationItem(
                stringResource(R.string.title_manage_feeds),
                selected = false,
                onClick = onManageFeedsClicked,
                icon = {
                    Icon(AppTheme.Icons.Tune, contentDescription = null)
                },
            )
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
        0.5f else 0f, animationSpec = tween(500))
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
    feeds: List<Feed>,
    isMagazineSelected: Boolean,
    selectedFeed: Feed?,
    onMagazineSelected: () -> Unit,
    onFeedSelected: (Feed) -> Unit
) {
    SectionLabel(stringResource(R.string.title_feeds_menu))
    NavigationItem(stringResource(R.string.title_magazine),
        icon = { Icon(painterResource(R.drawable.ic_newspaper_24), contentDescription = null) },
        selected = isMagazineSelected,
        onClick = onMagazineSelected)
    for (feed in feeds) {
        NavigationItem(
            feed.displayTitle.takeIf { it.isNotBlank() } ?: feed.title,
            selected = feed == selectedFeed,
            onClick = {
                onFeedSelected(feed)
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
                    Icon(painterResource(id = R.drawable.ic_rss_box), contentDescription = null)
                }
            },
            badge = {
                if (feed.unreadCount > 0) {
                    Text(feed.unreadCount.toString())
                }
            }
        )
    }
}


@Composable
private fun NavigationDivider() {
    Divider(modifier = Modifier
        .padding(horizontal = NavigationItemPadding))
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NavigationItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = Modifier
            .height(NavigationItemHeight)
            .padding(horizontal = ActiveIndicatorPadding),
        shape = if (selected)
            RoundedCornerShape(28.dp)
        else RectangleShape,
        color = if (selected) {
            MaterialTheme.colors.secondary.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colors.surface
        },
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier
                .padding(start = ActiveIndicatorContentPadding, end = 12.dp)
                .size(24.dp)){
                if (icon != null) {
                    icon()
                }
            }

            Text(label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.button,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (badge != null) {
                Box(Modifier.padding(start = 12.dp, end = 24.dp)) {
                    CompositionLocalProvider(LocalContentAlpha provides 0.4f) {
                        badge()
                    }
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
                    Feed(
                        id = Feed.FEED_ID_ALL_ARTICLES,
                        title = "All articles",
                        unreadCount = 290,
                    ),
                    Feed(
                        id = Feed.FEED_ID_FRESH,
                        title = "Fresh articles",
                    ),
                    Feed(
                        id = Feed.FEED_ID_STARRED,
                        title = "Starred articles",
                    ),
                    Feed(
                        id = 2,
                        title = "Frandroid",
                        unreadCount = 42,
                    ),
                    Feed(
                        id = 3,
                        title = "Gentoo universe",
                        unreadCount = 10,
                    ),
                    Feed(
                        id = 4,
                        title = "LinuxFr",
                        unreadCount = 8,
                    )
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
                            }
                        )
                    },
                    onManageFeedsClicked = {},
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
