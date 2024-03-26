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
package com.geekorum.ttrss.manage_feeds

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.rememberAsyncImagePainter
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.FeedWithFavIcon
import com.geekorum.ttrss.ui.AppTheme3
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ManageFeedsListScreen(
    viewModel: ManageFeedViewModel = dfmHiltViewModel(),
    navigateToSubscribeToFeed: () -> Unit,
    navigateToEditFeed: (Long) -> Unit
) {
    ManageFeedsListScreen(
        feedsData = viewModel.feeds,
        onFeedClick = {
            navigateToEditFeed(it.feed.id)
        },
        onAddFeedClick = navigateToSubscribeToFeed
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFeedsListScreen(
    feedsData: Flow<PagingData<FeedWithFavIcon>>,
    onFeedClick: (FeedWithFavIcon) -> Unit,
    onAddFeedClick: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                title = { Text(stringResource(id = R.string.activity_manage_feed_title)) },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddFeedClick) {
                Icon(Icons.AutoMirrored.Default.PlaylistAdd, contentDescription = null)
            }
        }
    ) { contentPadding ->
        val feedsPagingItem = feedsData.collectAsLazyPagingItems()
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        LazyColumn(
            modifier = Modifier.nestedScroll(nestedScrollInterop),
            contentPadding = contentPadding
        ) {
            items(feedsPagingItem.itemCount,
                key = feedsPagingItem.itemKey { it.feed.id },
                contentType = feedsPagingItem.itemContentType { "Feed" }) { idx ->
                val feedItem = feedsPagingItem[idx]
                ListItem(
                    modifier = Modifier.clickable {
                        if (feedItem != null)
                            onFeedClick(feedItem)
                    },
                    leadingContent = {
                        val feedIconPainter = rememberAsyncImagePainter(
                            model = feedItem?.favIcon?.url,
                            placeholder = painterResource(com.geekorum.ttrss.R.drawable.ic_rss_feed_orange),
                            fallback = painterResource(com.geekorum.ttrss.R.drawable.ic_rss_feed_orange),
                            error = painterResource(com.geekorum.ttrss.R.drawable.ic_rss_feed_orange),
                        )
                        Image(
                            painter = feedIconPainter,
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.size(40.dp)
                        )
                    },
                    headlineContent = {
                        Text(
                            feedItem?.feed?.title ?: "",
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    })
            }
        }
    }
}

@Preview
@Composable
private fun PreviewManageFeedsListScreen() {
    AppTheme3 {
        val feeds = PagingData.from(
            data = listOf(
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
        )
        val pagingFlow = MutableStateFlow(feeds)
        ManageFeedsListScreen(pagingFlow, onFeedClick = {}, onAddFeedClick = {})
    }
}