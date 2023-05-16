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

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geekorum.ttrss.ui.AppTheme3


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsListBar(
    tags: Set<String>,
    selectedTag: String?,
    selectedTagChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color)
) {
    Surface(modifier = modifier, color = color, contentColor = contentColor) {
        val scrollState = rememberScrollState()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(48.dp)
                .horizontalScroll(scrollState)
        ) {
            Spacer(Modifier.size(8.dp))
                tags.forEach { tag ->
                    FilterChip(selected = tag == selectedTag,
                        onClick = {  selectedTagChange(tag.takeUnless { tag == selectedTag }) },
                        label = { Text(tag) })
                }
            Spacer(Modifier.size(8.dp))
        }
    }
}


@Preview
@Composable
private fun PreviewTagsListBar() {
    AppTheme3 {
        var selected: String? by remember { mutableStateOf("phone") }
        TagsListBar(setOf("tv", "games", "phone", "android"),
            selectedTag = selected,
            selectedTagChange = { selected = it },
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}
