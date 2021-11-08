/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2020 by Frederic-Charles Barthelery.
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

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableArticleCard(
    title: String,
    flavorImageUrl: String,
    excerpt: String,
    feedNameOrAuthor: String,
    feedIconUrl: String?,
    isUnread: Boolean,
    isStarred: Boolean,
    onCardClick: () -> Unit,
    onOpenInBrowserClick: () -> Unit,
    onStarChanged: (Boolean) -> Unit,
    onShareClick: () -> Unit,
    onToggleUnreadClick: () -> Unit,
    onSwiped: () -> Unit,
    modifier: Modifier = Modifier,
    behindCardContent: @Composable (DismissDirection?) -> Unit = { }
) {
    val dismissState = rememberDismissState()
    var isInit by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // state is restored because it user rememberSaveable
        // so reset it if needed
        dismissState.snapTo(DismissValue.Default)
        isInit = true
    }
    LaunchedEffect(isInit, dismissState.currentValue) {
        if (isInit && dismissState.currentValue != DismissValue.Default) {
            onSwiped()
        }
    }

    SwipeToDismiss(
        state = dismissState,
        modifier = modifier,
        background = {
            behindCardContent(dismissState.dismissDirection)
        },
        dismissContent = {
            ArticleCard(
                title = title,
                flavorImageUrl = flavorImageUrl,
                excerpt = excerpt,
                feedNameOrAuthor = feedNameOrAuthor,
                feedIconUrl = feedIconUrl,
                isUnread = isUnread,
                isStarred = isStarred,
                onCardClick = onCardClick,
                onOpenInBrowserClick = onOpenInBrowserClick,
                onStarChanged = onStarChanged,
                onShareClick = onShareClick,
                onToggleUnreadClick = onToggleUnreadClick,
            )
        }
    )
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ArticleCard(
    title: String,
    flavorImageUrl: String,
    excerpt: String,
    feedNameOrAuthor: String,
    feedIconUrl: String?,
    isUnread: Boolean,
    isStarred: Boolean,
    onCardClick: () -> Unit,
    onOpenInBrowserClick: () -> Unit,
    onStarChanged: (Boolean) -> Unit,
    onShareClick: () -> Unit,
    onToggleUnreadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onCardClick,
        modifier = modifier.widthIn(max = 384.dp)
    ) {
        Column {
            TitleWithImage(title, flavorImageUrl, isUnread)
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                if (excerpt.isNotBlank()) {
                    ArticleExcerpt(excerpt)
                }
                CardToolbar(
                    feedNameOrAuthor, feedIconUrl,
                    isStarred = isStarred,
                    onOpenInBrowserClick = onOpenInBrowserClick,
                    onStarChanged = onStarChanged,
                    onShareClick = onShareClick,
                    onToggleUnreadClick = onToggleUnreadClick,
                )
            }
        }
    }
}


@Composable
private fun ArticleExcerpt(excerpt: String) {
    // TODO line spacing 2.sp is missing
    Text(excerpt,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        style = MaterialTheme.typography.body1,
        maxLines = 5,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun TitleWithImage(
    title: String,
    imageUrl: String,
    isUnread: Boolean,
) {
    Box {
        val finalImageUrl = imageUrl.takeUnless { it.isEmpty() }
        var hasImage by remember { mutableStateOf(finalImageUrl != null) }
        if (hasImage) {
            val colorFilter = if (!isUnread) {
                val colorMatrix = ColorMatrix().apply { setToSaturation(0f) }
                ColorFilter.colorMatrix(colorMatrix)
            } else null

            Image(
                painter = rememberImagePainter(data = finalImageUrl) {
                    listener(onError = { _, _ ->
                        hasImage = false
                    })
                },
                alignment = Alignment.TopCenter,
                contentScale = ContentScale.FillWidth,
                colorFilter = colorFilter,
                modifier = Modifier
                    .fillMaxWidth()
                    .layout { measurable, constraints ->
                        val maxHeight = (constraints.maxWidth * (9f / 16))
                            .roundToInt()
                            .coerceIn(constraints.minHeight, constraints.maxHeight)
                        val placeable = measurable.measure(constraints.copy(maxHeight = maxHeight))
                        layout(placeable.width, placeable.height) {
                            placeable.placeRelative(0, 0)
                        }
                    }
                    .padding(bottom = 16.dp),
                contentDescription = null,
            )
        }

        val imageScrimColor = if (hasImage) colorResource(id = R.color.image_scrim)
        else Color.Transparent
        Box(Modifier
            .fillMaxWidth()
            .background(imageScrimColor)
        ) {
            Text(title,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp, bottom = 16.dp),
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold.takeIf { isUnread },
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
@OptIn(ExperimentalAnimationGraphicsApi::class)
private fun CardToolbar(
    feedNameOrAuthor: String,
    feedIconUrl: String?,
    isStarred: Boolean,
    onOpenInBrowserClick: () -> Unit,
    onStarChanged: (Boolean) -> Unit,
    onShareClick: () -> Unit,
    onToggleUnreadClick: () -> Unit,
) {
    Row {
        val feedIconPainter = rememberImagePainter(data = feedIconUrl) {
            placeholder(R.drawable.ic_rss_feed_orange)
            fallback(R.drawable.ic_rss_feed_orange)
            error(R.drawable.ic_rss_feed_orange)
        }
        Icon(painter = feedIconPainter,
            tint = Color.Unspecified,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 16.dp, end = 8.dp)
                .size(16.dp)
                .align(Alignment.CenterVertically)
        )

        Text(
            feedNameOrAuthor,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .align(Alignment.CenterVertically)
                .weight(1f),
            style = MaterialTheme.typography.caption,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        IconButton(onClick = onOpenInBrowserClick) {
            Icon(AppTheme.Icons.OpenInBrowser,
                contentDescription = stringResource(R.string.open_article_in_browser))
        }

        IconToggleButton(checked = isStarred, onCheckedChange = onStarChanged) {
            val image = animatedVectorResource(id = R.drawable.avd_ic_star_filled)
            Icon(
                painter = image.painterFor(atEnd = isStarred),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        }
        IconButton(onClick = onShareClick) {
            Icon(AppTheme.Icons.Share,
                contentDescription = null)
        }

        Box {
            var showMorePopup by remember { mutableStateOf(false) }
            IconButton(onClick = { showMorePopup = true }) {
                Icon(AppTheme.Icons.MoreVert,
                    contentDescription = null)
            }
            DropdownMenu(expanded = showMorePopup, onDismissRequest = { showMorePopup = false }) {
                DropdownMenuItem(onClick = {
                    onToggleUnreadClick()
                    showMorePopup = false
                }) {
                    Text(stringResource(R.string.context_selection_toggle_unread))
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewArticleCard() {
    AppTheme {
        Column(Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            var card1Starred by remember { mutableStateOf(false) }
            ArticleCard(
                title = "Title".repeat(10),
                flavorImageUrl = "https://images.frandroid.com/wp-content/uploads/2021/11/capture-decran-2021-11-03-a-145358-1200x585.jpg",
                excerpt = "Excerpt of article".repeat(50),
                feedNameOrAuthor = "Geekttrss",
                feedIconUrl = null,
                isUnread = true,
                isStarred = card1Starred,
                onOpenInBrowserClick = {},
                onToggleUnreadClick = {},
                onShareClick = {},
                onStarChanged = { card1Starred = it },
                onCardClick = {},
            )

            var card2Starred by remember { mutableStateOf(true) }
            ArticleCard(
                title = "Title".repeat(5),
                flavorImageUrl = "",
                excerpt = "Excerpt of article".repeat(50),
                feedNameOrAuthor = "Geekttrss",
                feedIconUrl = "",
                isUnread = false,
                isStarred = card2Starred,
                onOpenInBrowserClick = {},
                onToggleUnreadClick = {},
                onShareClick = {},
                onStarChanged = { card2Starred = it },
                onCardClick = {},
            )
        }
    }
}
