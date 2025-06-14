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
package com.geekorum.ttrss.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geekorum.ttrss.ui.AppTheme3
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@Composable
fun SwipeableItem(
    state: SwipeToDismissBoxState,
    backgroundContent: @Composable RowScope.(dismissDirection: SwipeToDismissBoxValue, layoutWidthPx: Int, progress: Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    var layoutWidthPx by remember { mutableIntStateOf(1) }
    SwipeToDismissBox(state,
        modifier = modifier.onSizeChanged {
            layoutWidthPx = it.width
        },
        backgroundContent = {
            // We can't use state progress we need as it only change when crossing half of an anchor
            // we want anchoredDraggableState.progress(settle, start/end) but anchoredDraggableState
            // is internal so we can't use it
            // we can calculate the layout progress by using requiredOffset()/layoutWidth so we need to obtain layout width
            val progress = when(state.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd,
                SwipeToDismissBoxValue.EndToStart-> state.requireOffset() / layoutWidthPx
                SwipeToDismissBoxValue.Settled -> 0f
            }.absoluteValue
            backgroundContent(state.dismissDirection, layoutWidthPx, progress)
        },
        content = content
    )

}



@Composable
fun SwipeableItemBackgroundContainer(
    progress: Float,
    color: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        color = color,
        shape = shape,
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(progress),
        content = content
    )
}


fun alignInSwipeableItemBackgroundContainer(dismissDirection: SwipeToDismissBoxValue, alignment: Alignment.Horizontal) =
    Alignment.Horizontal { size, space, layoutDirection ->
        // negative space : how many pixels of the composable are hidden
        when {
            space <= 0 && dismissDirection == SwipeToDismissBoxValue.StartToEnd -> 0
            space <= 0 && dismissDirection == SwipeToDismissBoxValue.EndToStart -> space
            else -> {
                alignment.align(size, space, layoutDirection)
            }
        }
}


@Preview
@Composable
private fun PreviewSwipeableItem() {
    AppTheme3 {
        Scaffold {
            val state = rememberSwipeToDismissBoxState()
            // revert so we can test many times
            if (state.currentValue != SwipeToDismissBoxValue.Settled) {
                LaunchedEffect(Unit ) {
                    delay(2000)
                    state.reset()
                }
            }

            SwipeableItem(
                modifier = Modifier.padding(it).fillMaxHeight().wrapContentHeight(),
                state = state,
                backgroundContent = { dismissDirection, layoutWidth, progress ->
                    val containerHorizontalAlignment =
                        if (dismissDirection == SwipeToDismissBoxValue.EndToStart) Alignment.End else Alignment.Start

                    SwipeableItemBackgroundContainer(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.extraLarge,
                        progress = progress,
                        modifier = Modifier.fillMaxWidth()
                            .wrapContentWidth(containerHorizontalAlignment)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxHeight()
                                .wrapContentWidth(unbounded = true,
                                    align = alignInSwipeableItemBackgroundContainer(dismissDirection, Alignment.CenterHorizontally)
                                )
                        ) {
                            // use inner padding to avoid to deal with it during alignment
                            Spacer(Modifier.width(24.dp))
                            if (dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                                Icon(Icons.Default.Archive, null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            Text("Mark as read")
                            if (dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                Icon(Icons.Default.Archive, null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            Spacer(Modifier.width(24.dp))
                        }
                    }
                }
            ) {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Column {
                        Text("Item")
                    }
                }
            }
        }
    }
}