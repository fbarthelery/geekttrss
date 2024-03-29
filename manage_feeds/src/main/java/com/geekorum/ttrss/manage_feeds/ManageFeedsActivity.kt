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

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.rememberAsyncImagePainter
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.FeedWithFavIcon
import com.geekorum.ttrss.manage_feeds.add_feed.SubscribeToFeedActivity
import com.geekorum.ttrss.ui.AppTheme3
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import com.geekorum.ttrss.R as appR

class ManageFeedsActivity : BaseSessionActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme3 {
                ManageFeedsListScreen(navigateToSubscribeToFeed = {
                    startSubscribeToFeed()
                })
            }
        }
    }

    private fun startSubscribeToFeed() {
        val intent = Intent(this, SubscribeToFeedActivity::class.java)
        startActivity(intent)
    }

}

@Composable
fun ManageFeedsListScreen(
    viewModel: ManageFeedViewModel = viewModel(),
    navigateToSubscribeToFeed: () -> Unit,
) {
    ManageFeedsListScreen(
        feedsData = viewModel.feeds,
        onFeedClick = {
            viewModel.feedToUnsubscribe = it.feed
        },
        onAddFeedClick = navigateToSubscribeToFeed
    )

    viewModel.feedToUnsubscribe?.let {
        ConfirmUnsubscribeDialog(feed = it,
            onDismissRequest = { viewModel.feedToUnsubscribe = null },
            onConfirmClick = {
                viewModel.unsubscribeFeed()
            })
    }
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
                scrollBehavior = scrollBehavior)
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
                            placeholder = painterResource(appR.drawable.ic_rss_feed_orange),
                            fallback = painterResource(appR.drawable.ic_rss_feed_orange),
                            error = painterResource(appR.drawable.ic_rss_feed_orange),
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


@Composable
fun ConfirmUnsubscribeDialog(
    feed: Feed,
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(stringResource(R.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.btn_cancel))
            }
        },
        title = {
            Text(stringResource(R.string.fragment_confirmation_title),
                style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column {
                Text(stringResource(R.string.lbl_unsubscribe_msg), style = MaterialTheme.typography.bodyLarge)
                Text(feed.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp))
                Text(feed.url)
            }
        })
}

@Preview
@Composable
private fun PreviewConfirmUnsubscribeDialog() {
    AppTheme3 {
        val feed = Feed(
            id = 4,
            title = "LinuxFr",
            url = "https://linuxfr.org/feed",
            unreadCount = 8,
        )
        ConfirmUnsubscribeDialog(feed,
            onDismissRequest = {},
            onConfirmClick = {})
    }
}
