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
package com.geekorum.ttrss.articles_list.magazine

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.articles_list.*
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.share.createShareArticleIntent
import kotlinx.coroutines.delay


@Composable
fun MagazineScreen(
    windowSizeClass: WindowSizeClass,
    activityViewModel: ActivityViewModel,
    magazineViewModel: MagazineViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val viewLifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(activityViewModel, magazineViewModel, viewLifecycleOwner) {
        activityViewModel.refreshClickedEvent.observe(viewLifecycleOwner, EventObserver {
            magazineViewModel.refreshFeeds()
        })
    }

    val browserApplicationIcon by activityViewModel.browserIcon.collectAsStateWithLifecycle()
    Surface(Modifier.fillMaxSize()) {
        val context = LocalContext.current
        val lazyListState = rememberLazyListState()
        val isScrollingUp = lazyListState.isScrollingUp()
        LaunchedEffect(activityViewModel, isScrollingUp) {
            activityViewModel.setIsScrollingUp(isScrollingUp)
        }

        val compactItemsInSmallScreens by activityViewModel.displayCompactItems.collectAsStateWithLifecycle()
        val displayCompactItems = compactItemsInSmallScreens
                && (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact ||
                        windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact)

        ArticlesMagazine(
            viewModel = magazineViewModel,
            listState = lazyListState,
            browserApplicationIcon = browserApplicationIcon,
            displayCompactItems = displayCompactItems,
            onCardClick = activityViewModel::displayArticle,
            onShareClick = { article ->
                context.startActivity(createShareArticleIntent(context, article))
            },
            onOpenInBrowserClick = {
                activityViewModel.displayArticleInBrowser(context, it)
            },
            contentPadding = contentPadding,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticlesMagazine(
    viewModel: MagazineViewModel,
    browserApplicationIcon: Drawable?,
    displayCompactItems: Boolean,
    onCardClick: (Int, Article) -> Unit,
    onShareClick: (Article) -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {

    val isRefreshing by viewModel.isRefreshing.observeAsState(false)
    val pullRefreshState = rememberPullToRefreshState()
    val ltr = LocalLayoutDirection.current
    val pullRefreshBoxContentPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(ltr),
        end = contentPadding.calculateEndPadding(ltr),
        top = contentPadding.calculateTopPadding()
    )

    val additionalPadding = if (displayCompactItems) 0.dp else 8.dp
    val lazyListContentPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(ltr) + additionalPadding,
        end = contentPadding.calculateEndPadding(ltr) + additionalPadding,
        bottom = contentPadding.calculateBottomPadding() + additionalPadding,
        top = additionalPadding
    )

    // trigger refresh and sync pull refresh state
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(isRefreshing) {
            if (!isRefreshing) { // not making a full refresh
                viewModel.refreshMagazine()
                // display loading for a few frames
                delay(700)
                pullRefreshState.endRefresh()
            }
        }
    } else if (isRefreshing){
        LaunchedEffect(Unit) {
            pullRefreshState.startRefresh()
        }
    }
    if (!isRefreshing) {
        LaunchedEffect(Unit) {
            pullRefreshState.endRefresh()
        }
    }
    // workaround vertical offset state not correctly restored
    // https://issuetracker.google.com/issues/312220305
    if (pullRefreshState.isRefreshing && pullRefreshState.verticalOffset == 0f) {
        LaunchedEffect(Unit) {
            pullRefreshState.startRefresh()
        }
    }

    Box(
        modifier
            .padding(pullRefreshBoxContentPadding)
            .nestedScroll(pullRefreshState.nestedScrollConnection)) {
        val pagingItems = viewModel.articles.collectAsLazyPagingItems()
        val loadState by debouncedPagingViewStateFor(pagingItems)
        val isEmpty = pagingItems.itemCount == 0
        var refreshIfEmpty = remember { true }
        LaunchedEffect(loadState, isEmpty) {
            if (loadState is LoadState.NotLoading) {
                if (isEmpty && refreshIfEmpty) {
                    viewModel.refreshFeeds()
                }
                // refresh again if we loaded some items
                refreshIfEmpty = !isEmpty
            }
        }

        if (isEmpty && loadState is LoadState.NotLoading) {
            FeedEmptyText(isRefreshing)
        } else {
            ArticlesList(
                viewModel,
                pagingItems,
                listState,
                browserApplicationIcon,
                displayCompactItems,
                lazyListContentPadding,
                onCardClick,
                onOpenInBrowserClick,
                onShareClick,
            )
        }

        PullToRefreshContainer(
            pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
        )
    }

}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ArticlesList(
    viewModel: MagazineViewModel,
    pagingItems: LazyPagingItems<ArticleWithFeed>,
    listState: LazyListState,
    browserApplicationIcon: Drawable?,
    displayCompactItems: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onCardClick: (Int, Article) -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    onShareClick: (Article) -> Unit,
) {
    var animateItemAppearance by remember { mutableStateOf(true) }
    val verticalArrangement = if (displayCompactItems) Arrangement.Top else Arrangement.spacedBy(16.dp)
    LazyColumn(
        state = listState,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.article.id },
            contentType = pagingItems.itemContentType()
        ) { index ->
            val articleWithFeed = pagingItems[index]
            // initial state is visible if we don't animate
            val visibilityState = remember { MutableTransitionState(!animateItemAppearance) }
            // delay start of animation
            LaunchedEffect(index, Unit) {
                if (!animateItemAppearance) {
                    return@LaunchedEffect
                }
                if (index == listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index) {
                    animateItemAppearance = false
                }
                delay(38L * index)
                visibilityState.targetState = true
            }

            AnimatedVisibility(
                visibilityState,
                enter = fadeIn() + slideInVertically { it / 3 },
                modifier = Modifier.animateItemPlacement()
            ) {
                if (articleWithFeed != null) {
                    if (displayCompactItems) {
                        Column {
                            ArticleItem(
                                articleWithFeed = articleWithFeed,
                                viewModel = viewModel,
                                browserApplicationIcon = browserApplicationIcon,
                                onCardClick = { onCardClick(index, articleWithFeed.article) },
                                onOpenInBrowserClick = onOpenInBrowserClick,
                                onShareClick = onShareClick,
                                displayCompactItem = displayCompactItems
                            )
                            HorizontalDivider()
                        }
                    } else {
                        ArticleItem(
                            articleWithFeed = articleWithFeed,
                            viewModel = viewModel,
                            browserApplicationIcon = browserApplicationIcon,
                            onCardClick = { onCardClick(index, articleWithFeed.article) },
                            onOpenInBrowserClick = onOpenInBrowserClick,
                            onShareClick = onShareClick,
                            displayCompactItem = displayCompactItems
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleItem(
    articleWithFeed: ArticleWithFeed,
    viewModel: MagazineViewModel,
    browserApplicationIcon: Drawable?,
    onCardClick: () -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    onShareClick: (Article) -> Unit,
    displayCompactItem: Boolean,
) {
    val (article, feedWithFavIcon) = articleWithFeed
    val (feed, favIcon) = feedWithFavIcon
    val feedNameOrAuthor = feed.displayTitle.takeIf { it.isNotBlank() } ?: feed.title

    if (displayCompactItem) {
        CompactArticleListItem(
            title = article.title,
            flavorImageUrl = article.flavorImageUri,
            feedNameOrAuthor = feedNameOrAuthor,
            feedIconUrl = favIcon?.url,
            browserApplicationIcon = browserApplicationIcon,
            isUnread = article.isUnread,
            isStarred = article.isStarred,
            onItemClick = onCardClick,
            onOpenInBrowserClick = { onOpenInBrowserClick(article) },
            onStarChanged = { viewModel.setArticleStarred(article.id, it) },
            onShareClick = { onShareClick(article) },
            onToggleUnreadClick = {
                viewModel.setArticleUnread(article.id, !article.isTransientUnread)
            }
        )
    } else {
        ArticleCard(
            title = article.title,
            flavorImageUrl = article.flavorImageUri,
            excerpt = article.contentExcerpt,
            feedNameOrAuthor = feedNameOrAuthor,
            feedIconUrl = favIcon?.url,
            browserApplicationIcon = browserApplicationIcon,
            isUnread = article.isUnread,
            isStarred = article.isStarred,
            onCardClick = onCardClick,
            onOpenInBrowserClick = { onOpenInBrowserClick(article) },
            onStarChanged = { viewModel.setArticleStarred(article.id, it) },
            onShareClick = { onShareClick(article) },
            onToggleUnreadClick = {
                viewModel.setArticleUnread(article.id, !article.isTransientUnread)
            }
        )
    }
}
