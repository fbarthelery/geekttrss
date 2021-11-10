/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2021 by Frederic-Charles Barthelery.
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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geekorum.ttrss.ui.AppTheme


@Composable
fun TagsListBar(
    tags: Set<String>,
    selectedTag: String?,
    selectedTagChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier) {
        val scrollState = rememberScrollState()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(48.dp)
                .horizontalScroll(scrollState)
        ) {
            Spacer(Modifier.size(8.dp))
            AppTheme(colors = MaterialTheme.colors.copy(primary = MaterialTheme.colors.secondaryVariant)) {
                tags.forEach { tag ->
                    Chip(text = tag, selected = tag == selectedTag,
                        onClick = {
                            selectedTagChange(tag.takeUnless { tag == selectedTag })
                        }
                    )
                }
            }
            Spacer(Modifier.size(8.dp))
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Chip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected)
        MaterialTheme.colors.secondaryVariant
    else MaterialTheme.colors.onSurface
    val backgroundColorAlpha = if (selected) {
        0.24f
    } else 0.10f
    Card(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        shape = MaterialTheme.shapes.small.copy(topStart = CornerSize(50), bottomEnd = CornerSize(50)),
        border = BorderStroke(1.dp, backgroundColor.copy(alpha = backgroundColorAlpha)),
        elevation = 2.dp
    ) {
        Box(Modifier
            .background(backgroundColor.copy(alpha = backgroundColorAlpha))
        ) {
            Row(
                modifier = Modifier.fillMaxHeight()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val textColor = if (selected) {
                    MaterialTheme.colors.primary
                } else MaterialTheme.colors.onSurface.copy(alpha = 0.87f)
                Text(text = text,
                    style = MaterialTheme.typography.body2,
                    color = textColor
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewTagsListBar() {
    AppTheme {
        var selected: String? by remember { mutableStateOf("phone") }
        TagsListBar(setOf("tv", "games", "phone", "android"),
            selectedTag = selected,
            selectedTagChange = { selected = it },
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}
