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
package com.geekorum.ttrss.manage_feeds.add_feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geekorum.ttrss.manage_feeds.R
import com.geekorum.ttrss.ui.AppTheme3

@Composable
fun SelectFeedScreen(viewModel: SubscribeToFeedViewModel = viewModel()) {
    val feeds by viewModel.feedsFound.observeAsState()
    var selectedFeed by remember { mutableStateOf(feeds?.firstOrNull()) }
    SelectFeedScreen(
        feeds = feeds ?: emptyList(),
        selectedFeed = selectedFeed,
        onFeedSelected = {
            selectedFeed = it
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectFeedScreen(
    feeds: List<FeedsFinder.FeedResult>,
    selectedFeed: FeedsFinder.FeedResult? = feeds.firstOrNull(),
    onFeedSelected: (FeedsFinder.FeedResult) -> Unit,
) {
    Column(Modifier.padding(16.dp)) {
        Text(pluralStringResource(id = R.plurals.fragment_select_feed_label, count = feeds.size, feeds.size))

        if (feeds.size > 1) {
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                modifier = Modifier.padding(top = 32.dp),
                expanded = expanded,
                onExpandedChange = {
                    expanded = it
                }) {
                TextField(
                    readOnly = true,
                    textStyle = MaterialTheme.typography.titleMedium,
                    value = selectedFeed?.title ?: "",
                    singleLine = true,
                    onValueChange = { },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded
                        )
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Use DropDownMenu instead of exposed, to workaround bug
                // https://issuetracker.google.com/issues/205589613
                // ExposedDropdownMenu(
                DropdownMenu(
                    modifier = Modifier.exposedDropdownSize(),
                    expanded = expanded, onDismissRequest = { expanded = false }) {
                    for (feed in feeds) {
                        DropdownMenuItem(
                            onClick = {
                                onFeedSelected(feed)
                                expanded = false
                            },
                            text = {
                                Text(feed.title)
                            }
                        )
                    }
                }
            }
        } else {
            val text = feeds.firstOrNull()?.title ?: stringResource(R.string.activity_add_feed_no_feeds_available)
            Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 24.dp))
        }
    }
}


@Preview
@Composable
private fun PreviewSelectFeedScreen() {
    AppTheme3 {
        Surface(Modifier.fillMaxSize()) {
            val feeds = listOf(
                FeedsFinder.FeedResult(
                    FeedsFinder.Source.HTML,
                    href = "https://site.com/feeds/rss",
                    title = "RSS feed",
                ),
                FeedsFinder.FeedResult(
                    FeedsFinder.Source.HTML,
                    href = "https://site.com/feeds/atom",
                    title = "Atom feed",
                )
            )
            var selectedFeed by remember { mutableStateOf(feeds.firstOrNull()) }
            SelectFeedScreen(feeds,
                selectedFeed = selectedFeed,
                onFeedSelected = {
                    selectedFeed = it
                }
            )
        }
    }
}

@Preview
@Composable
private fun PreviewSelectFeedScreenSingle() {
    AppTheme3 {
        Surface(Modifier.fillMaxSize()) {
            val feeds = listOf(
                FeedsFinder.FeedResult(FeedsFinder.Source.HTML,
                    href = "https://site.com/feeds/rss",
                    title = "RSS feed",
                ))
            SelectFeedScreen(feeds, onFeedSelected = {})
        }
    }
}