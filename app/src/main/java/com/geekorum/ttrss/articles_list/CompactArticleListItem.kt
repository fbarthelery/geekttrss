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
package com.geekorum.ttrss.articles_list

import android.graphics.drawable.Drawable
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Panorama
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme3
import com.geekorum.ttrss.ui.components.MultiLineHeadlineListItem
import com.geekorum.ttrss.ui.components.OpenInBrowserIcon
import com.geekorum.ttrss.ui.components.SwipeableItem
import com.geekorum.ttrss.ui.components.forwardingPainter
import com.materialkolor.ktx.harmonizeWithPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableCompactArticleListItem(
    title: String,
    flavorImageUrl: String,
    feedNameOrAuthor: String,
    feedIconUrl: String?,
    browserApplicationIcon: Drawable?,
    isUnread: Boolean,
    isStarred: Boolean,
    onItemClick: () -> Unit,
    onOpenInBrowserClick: () -> Unit,
    onStarChanged: (Boolean) -> Unit,
    onShareClick: () -> Unit,
    onToggleUnreadClick: () -> Unit,
    onSwiped: () -> Unit,
    modifier: Modifier = Modifier,
    swipeToDismissState: SwipeToDismissBoxState = rememberSwipeToDismissBoxState(),
    behindCardContent: @Composable RowScope.(dismissDirection: SwipeToDismissBoxValue, layoutWidthPx: Int, progress: Float) -> Unit = { _, _, _ -> }
) {
    var isInit by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // state is restored because it uses rememberSaveable
        // so reset it if needed
        swipeToDismissState.snapTo(SwipeToDismissBoxValue.Settled)
        isInit = true
    }
    LaunchedEffect(isInit, swipeToDismissState.settledValue) {
        if (isInit && swipeToDismissState.settledValue != SwipeToDismissBoxValue.Settled) {
            onSwiped()
        }
    }

    // we want the dismiss box to take full width so the card is completely
    // out of screen when dismissed
    SwipeableItem(
        state = swipeToDismissState,
        modifier = modifier
            .fillMaxWidth()
            .dismissProgressHapticFeedback(swipeToDismissState),
        backgroundContent = behindCardContent,
        content = {
            CompactArticleListItem(
                title = title,
                flavorImageUrl = flavorImageUrl,
                feedNameOrAuthor = feedNameOrAuthor,
                feedIconUrl = feedIconUrl,
                browserApplicationIcon = browserApplicationIcon,
                isUnread = isUnread,
                isStarred = isStarred,
                onItemClick = onItemClick,
                onOpenInBrowserClick = onOpenInBrowserClick,
                onStarChanged = onStarChanged,
                onShareClick = onShareClick,
                onToggleUnreadClick = onToggleUnreadClick,
                // center the card
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth()
            )
        }
    )
}


@Composable
fun CompactArticleListItem(
    title: String,
    flavorImageUrl: String,
    feedNameOrAuthor: String,
    feedIconUrl: String?,
    browserApplicationIcon: Drawable?,
    isUnread: Boolean,
    isStarred: Boolean,
    onItemClick: () -> Unit,
    onOpenInBrowserClick: () -> Unit,
    onStarChanged: (Boolean) -> Unit,
    onShareClick: () -> Unit,
    onToggleUnreadClick: () -> Unit,
    modifier: Modifier = Modifier,
    displayImage: Boolean = true,
) {
    MultiLineHeadlineListItem(
        modifier = modifier.clickable(onClick = onItemClick),
        leadingContent = {
            if (!displayImage)
                return@MultiLineHeadlineListItem
            val finalImageUrl = flavorImageUrl.takeUnless { it.isEmpty() }
            val colorFilter = if (!isUnread) {
                val colorMatrix = ColorMatrix().apply { setToSaturation(0f) }
                ColorFilter.colorMatrix(colorMatrix)
            } else null

            val imageReq = ImageRequest.Builder(LocalContext.current)
                .data(finalImageUrl)
                .build()
            val contentColor = LocalContentColor.current
            val fallback = forwardingPainter(
                painter = rememberVectorPainter(image = Icons.Outlined.Panorama),
                colorFilter = if (contentColor == Color.Unspecified) null else ColorFilter.tint(contentColor)
            )
            AsyncImage(
                model = imageReq,
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.FillHeight,
                colorFilter = colorFilter,
                fallback = fallback,
                placeholder = fallback,
                error = fallback,
                modifier = Modifier
                    .heightIn(max = 64.dp)
                    .aspectRatio(4 / 3f)
                    .clip(MaterialTheme.shapes.medium),
            )
        },
        supportingContent = {
            Row(Modifier.padding(top = 8.dp)) {
                val feedIconPainter = rememberAsyncImagePainter(
                    model = feedIconUrl,
                    placeholder = painterResource(R.drawable.ic_rss_feed_orange),
                    fallback = painterResource(R.drawable.ic_rss_feed_orange),
                    error = painterResource(R.drawable.ic_rss_feed_orange),
                )
                Icon(
                    painter = feedIconPainter,
                    tint = Color.Unspecified,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(16.dp)
                        .align(Alignment.CenterVertically)
                )

                Text(
                    feedNameOrAuthor,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        trailingContent = {
            Box {
                var showMorePopup by remember { mutableStateOf(false) }
                IconButton(
                    modifier = Modifier.align(Alignment.TopCenter),
                    onClick = { showMorePopup = true }
                ) {
                    Icon(
                        AppTheme3.Icons.MoreVert,
                        contentDescription = null
                    )
                }
                FullMenuDropdown(
                    showMorePopup,
                    browserApplicationIcon = browserApplicationIcon,
                    isArticleStarred = isStarred,
                    setExpanded = { showMorePopup = it },
                    setArticleStarred = onStarChanged,
                    onOpenInBrowserClick = onOpenInBrowserClick,
                    onToggleUnreadClick = onToggleUnreadClick,
                    onShareClick = onShareClick,
                )
            }
        },
        headlineContent = {
            Text(
                title,
                fontWeight = FontWeight.Bold.takeIf { isUnread },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}


@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
private fun FullMenuDropdown(
    expanded: Boolean,
    browserApplicationIcon: Drawable?,
    isArticleStarred: Boolean,
    setExpanded: (Boolean) -> Unit,
    setArticleStarred: (Boolean) -> Unit,
    onOpenInBrowserClick: () -> Unit,
    onToggleUnreadClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { setExpanded(false) }) {
        DropdownMenuItem(
            leadingIcon = {
                OpenInBrowserIcon(
                    browserApplicationIcon = browserApplicationIcon,
                    contentDescription = stringResource(R.string.open_article_in_browser)
                )
            },
            onClick = {
                onOpenInBrowserClick()
                setExpanded(false)
            },
            text = {
                Text(stringResource(R.string.open_article_in_browser))
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                val iconColor = if (isArticleStarred) {
                    MaterialTheme.colorScheme.harmonizeWithPrimary(AppTheme3.Colors.MaterialGreenA700)
                } else LocalContentColor.current
                val image = AnimatedImageVector.animatedVectorResource(id = R.drawable.avd_ic_star_filled)
                Icon(
                    painter = rememberAnimatedVectorPainter(image, atEnd = isArticleStarred),
                    contentDescription = null,
                    tint = iconColor
                )
            },
            onClick = {
                setArticleStarred(!isArticleStarred)
                setExpanded(false)
            },
            text = {
                Text(stringResource(R.string.star_article))
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(imageVector = AppTheme3.Icons.Share, contentDescription = null)
            },
            onClick = {
                onShareClick()
                setExpanded(false)
            },
            text = {
                Text(stringResource(R.string.share_article))
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(imageVector = AppTheme3.Icons.Archive, contentDescription = null)
            },
            onClick = {
                onToggleUnreadClick()
                setExpanded(false)
            },
            text = {
                Text(stringResource(R.string.context_selection_toggle_unread))
            }
        )
    }
}

@Preview
@Composable
private fun PreviewCompactArticleListItem() {
    AppTheme3 {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            var card1Starred by remember { mutableStateOf(false) }
            CompactArticleListItem(
                title = "Title".repeat(10),
                flavorImageUrl = "https://images.frandroid.com/wp-content/uploads/2021/11/capture-decran-2021-11-03-a-145358-1200x585.jpg",
                feedNameOrAuthor = "Geekttrss",
                feedIconUrl = null,
                browserApplicationIcon = null,
                isUnread = true,
                isStarred = card1Starred,
                onOpenInBrowserClick = {},
                onToggleUnreadClick = {},
                onShareClick = {},
                onStarChanged = { card1Starred = it },
                onItemClick = {},
            )

            var card2Starred by remember { mutableStateOf(true) }
            CompactArticleListItem(
                title = "Title".repeat(5),
                flavorImageUrl = "",
                feedNameOrAuthor = "Geekttrss",
                feedIconUrl = "",
                browserApplicationIcon = null,
                isUnread = false,
                isStarred = card2Starred,
                onOpenInBrowserClick = {},
                onToggleUnreadClick = {},
                onShareClick = {},
                onStarChanged = { card2Starred = it },
                onItemClick = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PreviewSwipeableCompactArticleListItem() {
    AppTheme3 {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            var card1Starred by remember { mutableStateOf(false) }
            val swipeToDismissState = rememberSwipeToDismissBoxState()
            val coroutineScopre = rememberCoroutineScope()
            SwipeableCompactArticleListItem(
                title = "Title".repeat(10),
                flavorImageUrl = "https://images.frandroid.com/wp-content/uploads/2021/11/capture-decran-2021-11-03-a-145358-1200x585.jpg",
                feedNameOrAuthor = "Geekttrss",
                feedIconUrl = null,
                browserApplicationIcon = null,
                isUnread = true,
                isStarred = card1Starred,
                onOpenInBrowserClick = {},
                onToggleUnreadClick = {},
                onShareClick = {},
                onStarChanged = { card1Starred = it },
                onItemClick = {},
                onSwiped = {
                    coroutineScopre.launch {
                        delay(2000)
                        swipeToDismissState.reset()
                    }
                },
                behindCardContent = { dismissDirection, layoutWidthPx, progress ->
                    Box(Modifier.fillMaxSize().background(Color.Blue))
                },
                swipeToDismissState = swipeToDismissState
            )

        }
    }
}