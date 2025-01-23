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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.rememberAsyncImagePainter
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.FeedWithFavIcon
import com.geekorum.ttrss.ui.AppTheme3
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import com.geekorum.ttrss.R as appR

private const val CategoryContentType = "Category"
private const val FeedContentType = "Feed"
private const val SpecialFeedContentType = "SpecialFeed"

private val SpecialFeeds = createSpecialFeeds()
private fun createSpecialFeeds(): List<FeedWithFavIcon> {
    val allArticles = FeedWithFavIcon(
        feed = Feed.createVirtualFeedForId(Feed.FEED_ID_ALL_ARTICLES),
        favIcon = null)

    val freshArticles = FeedWithFavIcon(
        feed = Feed.createVirtualFeedForId(Feed.FEED_ID_FRESH),
        favIcon = null)
    val starredArticles = FeedWithFavIcon(
        feed = Feed.createVirtualFeedForId(Feed.FEED_ID_STARRED),
        favIcon = null)

    return listOf(allArticles, freshArticles, starredArticles)
}


@Composable
fun ManageFeedsListScreen(
    viewModel: ManageFeedViewModel = dfmHiltViewModel(),
    navigateToSubscribeToFeed: () -> Unit,
    navigateToEditFeed: (Long) -> Unit
) {
    val feedsData by viewModel.subscribedFeedsByCategories.collectAsStateWithLifecycle()
    ManageFeedsListScreen(
        feedsData = feedsData,
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
                colors = TopAppBarDefaults.topAppBarColors(
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
                contentType = feedsPagingItem.itemContentType { FeedContentType }) { idx ->
                val feedItem = feedsPagingItem[idx]
                FeedListItem(feedItem, onFeedClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFeedsListScreen(
    feedsData: Map<Category, List<FeedWithFavIcon>>,
    onFeedClick: (FeedWithFavIcon) -> Unit,
    onAddFeedClick: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
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
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        val expandedCategoriesId = rememberSaveable(
            saver = MutableStateListSaver
        ) { mutableStateListOf() }
        var isSpecialFeedsExpanded by rememberSaveable { mutableStateOf(true) }
        LazyColumn(
            modifier = Modifier.nestedScroll(nestedScrollInterop),
            contentPadding = contentPadding
        ) {
            item(contentType = CategoryContentType) {
                CategoryListItem(
                    stringResource(R.string.lbl_special_feeds_category),
                    isExpanded = isSpecialFeedsExpanded,
                    modifier = Modifier.clickable {
                        isSpecialFeedsExpanded = !isSpecialFeedsExpanded
                    }
                )
            }
            if (isSpecialFeedsExpanded) {
                items(
                    SpecialFeeds.size,
                    contentType = { SpecialFeedContentType }) { idx ->
                    val feedItem = SpecialFeeds[idx]
                    SpecialFeedListItem(feedItem, onFeedClick)
                }
            }

            for (category in feedsData.keys) {
                val isExpanded = category.id in expandedCategoriesId
                item(contentType = CategoryContentType) {
                    CategoryListItem(category = category.title, isExpanded = isExpanded,
                        modifier = Modifier.clickable {
                            if (isExpanded)
                                expandedCategoriesId.remove(category.id)
                            else
                                expandedCategoriesId.add(category.id)
                        }
                    )
                }
                if (isExpanded) {
                    val feeds = feedsData[category]!!
                    items(feeds.size,
                        contentType = { FeedContentType }) { idx ->
                        val feedItem = feeds[idx]
                        FeedListItem(feedItem, onFeedClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedListItem(
    feedItem: FeedWithFavIcon?,
    onFeedClick: (FeedWithFavIcon) -> Unit
) {
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

@Composable
private fun SpecialFeedListItem(
    feedItem: FeedWithFavIcon,
    onFeedClick: (FeedWithFavIcon) -> Unit
) {
    ListItem(
        modifier = Modifier.clickable {
            onFeedClick(feedItem)
        },
        leadingContent = {
            val iconVector = when {
                feedItem.feed.isArchivedFeed -> AppTheme3.Icons.Inventory2
                feedItem.feed.isStarredFeed -> AppTheme3.Icons.Star
                feedItem.feed.isPublishedFeed -> AppTheme3.Icons.CheckBox
                feedItem.feed.isFreshFeed -> AppTheme3.Icons.LocalCafe
                feedItem.feed.isAllArticlesFeed -> AppTheme3.Icons.FolderOpen
                else -> null
            }
            if (iconVector != null) {
                Icon(iconVector, contentDescription = null)
            }
        },
        headlineContent = {
            val label = run {
                val titleRes = when (feedItem.feed.id) {
                    Feed.FEED_ID_FRESH -> appR.string.label_fresh_feeds_title
                    Feed.FEED_ID_STARRED -> appR.string.label_starred_feeds_title
                    Feed.FEED_ID_ALL_ARTICLES -> appR.string.label_all_articles_feeds_title
                    else -> null
                }
                titleRes?.let { stringResource(it) }
                    ?:feedItem.feed.displayTitle.takeIf { it.isNotBlank() } ?: feedItem.feed.title
            }
            Text(
                label,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        })
}



@Composable
fun CategoryListItem(category: String, isExpanded: Boolean, modifier: Modifier = Modifier) {
    Column(modifier) {
        ListItem(
            leadingContent = {
                Icon(AppTheme3.Icons.Category, null,
                    tint = if (isExpanded) MaterialTheme.colorScheme.primary else LocalContentColor.current)
            },
            trailingContent = {
                if (isExpanded) {
                    Icon(AppTheme3.Icons.ExpandLess, null)
                } else {
                    Icon(AppTheme3.Icons.ExpandMore, null)
                }
            },
            headlineContent = {
                Text(category,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
            })
        HorizontalDivider()
    }
}

private val MutableStateListSaver = listSaver(
    save = { it },
    restore = { values ->
        mutableStateListOf<Long>().apply { addAll(values) }
    }
)

@Preview
@Composable
private fun PreviewCategoryListItem() {
    var isExpanded by remember { mutableStateOf(false) }
    CategoryListItem("Special feeds", isExpanded = isExpanded, modifier = Modifier.clickable {
        isExpanded = !isExpanded
    })
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