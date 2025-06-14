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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme3
import com.geekorum.ttrss.ui.components.OpenInBrowserIcon
import com.geekorum.ttrss.ui.components.SwipeableItem
import com.materialkolor.ktx.harmonizeWithPrimary
import kotlin.math.roundToInt


private val SwipePositionalThreshold: (totalDistance: Float) -> Float
    @Composable get() = with(LocalDensity.current) { { 128.dp.toPx() } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableArticleCard(
    title: String,
    flavorImageUrl: String,
    excerpt: String,
    feedNameOrAuthor: String,
    feedIconUrl: String?,
    browserApplicationIcon: Drawable?,
    isUnread: Boolean,
    isStarred: Boolean,
    onCardClick: () -> Unit,
    onOpenInBrowserClick: () -> Unit,
    onStarChanged: (Boolean) -> Unit,
    onShareClick: () -> Unit,
    onToggleUnreadClick: () -> Unit,
    onSwiped: () -> Unit,
    modifier: Modifier = Modifier,
    behindCardContent: @Composable RowScope.(dismissDirection: SwipeToDismissBoxValue, layoutWidthPx: Int, progress: Float) -> Unit = { _, _, _ -> }
) {
    val dismissState = rememberSwipeToDismissBoxState(positionalThreshold = SwipePositionalThreshold)
    var isInit by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // state is restored because it user rememberSaveable
        // so reset it if needed
        dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        isInit = true
    }
    LaunchedEffect(isInit, dismissState.currentValue) {
        if (isInit && dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            onSwiped()
        }
    }

    // we want the dismiss box to take full width so the card is completely
    // out of screen when dismissed
    SwipeableItem(
        state = dismissState,
        modifier = modifier
            .fillMaxWidth()
            .dismissProgressHapticFeedback(dismissState),
        backgroundContent = behindCardContent,
        content = {
            ArticleCard(
                title = title,
                flavorImageUrl = flavorImageUrl,
                excerpt = excerpt,
                feedNameOrAuthor = feedNameOrAuthor,
                feedIconUrl = feedIconUrl,
                browserApplicationIcon = browserApplicationIcon,
                isUnread = isUnread,
                isStarred = isStarred,
                onCardClick = onCardClick,
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
fun Modifier.dismissProgressHapticFeedback(dismissState: SwipeToDismissBoxState, skipInitial: SwipeToDismissBoxValue = SwipeToDismissBoxValue.Settled): Modifier {
    val hapticFeedback = LocalHapticFeedback.current
    var hasDoneFeedback by remember { mutableStateOf(false) }
    LaunchedEffect(dismissState.targetValue) {
        if (hasDoneFeedback || dismissState.targetValue != skipInitial) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
            hasDoneFeedback = true
        }
    }
    return this
}

@Composable
fun ArticleCard(
    title: String,
    flavorImageUrl: String,
    excerpt: String,
    feedNameOrAuthor: String,
    feedIconUrl: String?,
    browserApplicationIcon: Drawable?,
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
            if (excerpt.isNotBlank()) {
                ArticleExcerpt(excerpt)
            }
            CardToolbar(
                feedNameOrAuthor, feedIconUrl,
                browserApplicationIcon = browserApplicationIcon,
                isStarred = isStarred,
                onOpenInBrowserClick = onOpenInBrowserClick,
                onStarChanged = onStarChanged,
                onShareClick = onShareClick,
                onToggleUnreadClick = onToggleUnreadClick,
            )
        }
    }
}


@Composable
private fun ArticleExcerpt(excerpt: String) {
    Text(excerpt,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
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

            val imageReq = ImageRequest.Builder(LocalContext.current)
                .data(finalImageUrl)
                .listener(onError = { _, _ -> hasImage = false})
                .build()
            AsyncImage(model = imageReq,
                contentDescription = null,
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
            )
        }

        val imageScrimColor = if (hasImage) colorResource(id = R.color.image_scrim)
        else Color.Transparent
        Box(
            Modifier
                .fillMaxWidth()
                .background(imageScrimColor)
        ) {
            Text(title,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp, bottom = 16.dp),
                style = MaterialTheme.typography.headlineSmall,
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
    browserApplicationIcon: Drawable?,
    onOpenInBrowserClick: () -> Unit,
    onStarChanged: (Boolean) -> Unit,
    onShareClick: () -> Unit,
    onToggleUnreadClick: () -> Unit,
) {
    Row {
        val feedIconPainter = rememberAsyncImagePainter(model = feedIconUrl,
            placeholder = painterResource(R.drawable.ic_rss_feed_orange),
            fallback = painterResource(R.drawable.ic_rss_feed_orange),
            error = painterResource(R.drawable.ic_rss_feed_orange),
        )
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
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        IconButton(onClick = onOpenInBrowserClick) {
            OpenInBrowserIcon(
                browserApplicationIcon = browserApplicationIcon,
                contentDescription = stringResource(R.string.open_article_in_browser)
            )
        }

        IconToggleButton(checked = isStarred, onCheckedChange = onStarChanged,
            colors = IconButtonDefaults.iconToggleButtonColors(
                checkedContentColor = MaterialTheme.colorScheme.harmonizeWithPrimary(AppTheme3.Colors.MaterialGreenA700)
            )
        ) {
            val image = AnimatedImageVector.animatedVectorResource(id = R.drawable.avd_ic_star_filled)
            Icon(
                painter = rememberAnimatedVectorPainter(image, atEnd = isStarred),
                contentDescription = null,
            )
        }
        IconButton(onClick = onShareClick) {
            Icon(AppTheme3.Icons.Share,
                contentDescription = null)
        }

        Box {
            var showMorePopup by remember { mutableStateOf(false) }
            IconButton(onClick = { showMorePopup = true }) {
                Icon(AppTheme3.Icons.MoreVert,
                    contentDescription = null)
            }
            DropdownMenu(expanded = showMorePopup, onDismissRequest = { showMorePopup = false }) {
                DropdownMenuItem(
                    onClick = {
                        onToggleUnreadClick()
                        showMorePopup = false
                    },
                    text = {
                        Text(stringResource(R.string.context_selection_toggle_unread))
                    })
            }
        }
    }
}

@Preview
@Composable
private fun PreviewArticleCard() {
    AppTheme3 {
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
                browserApplicationIcon = null,
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
                browserApplicationIcon = null,
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
