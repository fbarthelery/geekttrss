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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.*
import com.geekorum.ttrss.ui.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOf


/**
 * Get [PagingViewLoadState] for [pagingItems].
 * This allows to make the disctinction between the initial [LoadState] of [LoadState.NotLoading]
 * and the final [LoadState.NotLoading] at the end of the operation
 */
@Composable
fun pagingViewStateFor(pagingItems: LazyPagingItems<ArticleWithFeed>) =
    produceState(initialValue = PagingViewLoadState.INITIAL, key1 = pagingItems) {
        snapshotFlow { pagingItems.loadState.refresh }
            .drop(1) // the initial one is always LoadState.NotLoading, drop it
            .distinctUntilChanged()
            .collect {
                value = when (it) {
                    is LoadState.NotLoading -> PagingViewLoadState.LOADED
                    is LoadState.Loading -> PagingViewLoadState.LOADING
                    is LoadState.Error -> PagingViewLoadState.ERROR
                }
            }
    }

enum class PagingViewLoadState {
    INITIAL,
    LOADING,
    LOADED,
    ERROR
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ArticleCardList(
    viewModel: BaseArticlesViewModel,
    listState: LazyListState = rememberLazyListState(),
    onCardClick: (Int, Article) -> Unit,
    onShareClick: (Article) -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val pullRefreshState = rememberPullRefreshState(isRefreshing, onRefresh = {
        viewModel.refresh()
    })
    val articles = viewModel.articles.collectAsLazyPagingItems()
    val isMultiFeedList by viewModel.isMultiFeed.collectAsState()

    val loadState by pagingViewStateFor(articles)
    val isEmpty = articles.itemCount == 0
    var refreshIfEmpty by remember { mutableStateOf(true) }
    LaunchedEffect(loadState, isEmpty) {
        if (loadState == PagingViewLoadState.LOADED) {
            if (isEmpty && refreshIfEmpty) {
                viewModel.refresh()
            }
            // refresh again if we loaded some items
            refreshIfEmpty = !isEmpty
        }
    }

    ArticleCardList(
        articles = articles,
        listState = listState,
        isMultiFeedList = isMultiFeedList,
        isRefreshing = isRefreshing,
        pullRefreshState = pullRefreshState,
        onCardClick = onCardClick,
        onShareClick = onShareClick,
        onOpenInBrowserClick = onOpenInBrowserClick,
        onToggleUnreadClick = {
            viewModel.setArticleUnread(it.id, !it.isTransientUnread)
        },
        onStarChanged = { article, newValue ->
            viewModel.setArticleStarred(article.id, newValue)
        },
        onSwiped = {
            viewModel.setArticleUnread(it.id, false)
        },
        modifier = modifier,
        contentPadding = contentPadding
    )

}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
private fun ArticleCardList(
    articles: LazyPagingItems<ArticleWithFeed>,
    isMultiFeedList: Boolean,
    isRefreshing: Boolean,
    pullRefreshState: PullRefreshState,
    onCardClick: (Int, Article) -> Unit,
    onShareClick: (Article) -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    onToggleUnreadClick: (Article) -> Unit,
    onStarChanged: (Article, Boolean) -> Unit,
    onSwiped: (Article) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val ltr = LocalLayoutDirection.current
    val topContentPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(ltr),
        end = contentPadding.calculateEndPadding(ltr),
        top = contentPadding.calculateTopPadding()
    )

    val bottomContentPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(ltr),
        end = contentPadding.calculateEndPadding(ltr),
        bottom = contentPadding.calculateBottomPadding()
    )

    Box(modifier.padding(topContentPadding).pullRefresh(pullRefreshState)) {
        val loadState by pagingViewStateFor(articles)
        val isEmpty = articles.itemCount == 0
        if (isEmpty && loadState == PagingViewLoadState.LOADED) {
            FeedEmptyText(isRefreshing)
        } else {
            ArticlesList(
                articles,
                listState,
                isMultiFeedList,
                bottomContentPadding,
                onCardClick,
                onOpenInBrowserClick,
                onShareClick,
                onStarChanged,
                onToggleUnreadClick,
                onSwiped
            )
        }

        PullRefreshIndicator(
            isRefreshing,
            pullRefreshState,
            Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ArticlesList(
    articles: LazyPagingItems<ArticleWithFeed>,
    listState: LazyListState,
    isMultiFeedList: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onCardClick: (Int, Article) -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    onShareClick: (Article) -> Unit,
    onStarChanged: (Article, Boolean) -> Unit,
    onToggleUnreadClick: (Article) -> Unit,
    onSwiped: (Article) -> Unit
) {
    var animateItemAppearance by remember { mutableStateOf(true) }
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
/*    val contentPadding = PaddingValues(
        start = navBarPadding.calculateStartPadding(LocalLayoutDirection.current) + 8.dp,
        top = navBarPadding.calculateTopPadding() + 8.dp,
        end = navBarPadding.calculateEndPadding(LocalLayoutDirection.current) + 8.dp,
        bottom = navBarPadding.calculateBottomPadding() + additionalContentPaddingBottom
    )*/
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = articles.itemCount,
            key = articles.itemKey{ it.article.id },
            contentType = articles.itemContentType()
        ) { index ->
            val articleWithFeed = articles[index]
            // initial state is visible if we don't animate
            val visibilityState = remember { MutableTransitionState(!animateItemAppearance) }
            // delay start of animation
            LaunchedEffect(index) {
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
                    SwipeableArticleCard(
                        articleWithFeed = articleWithFeed,
                        displayFeedName = isMultiFeedList,
                        onCardClick = { onCardClick(index, articleWithFeed.article) },
                        onOpenInBrowserClick = onOpenInBrowserClick,
                        onShareClick = onShareClick,
                        onStarChanged = onStarChanged,
                        onToggleUnreadClick = onToggleUnreadClick,
                        onSwiped = onSwiped
                    )
                }
            }
        }
    }
}

@Composable
fun FeedEmptyText(isRefreshing: Boolean) {
    val emptyText = if (isRefreshing) {
        stringResource(R.string.fragment_articles_list_no_articles_and_sync_lbl)
    } else stringResource(R.string.fragment_articles_list_no_articles_lbl)
    Box(Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(emptyText,
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 192.dp)
        )
    }
}


@Composable
private fun SwipeableArticleCard(
    articleWithFeed: ArticleWithFeed,
    displayFeedName: Boolean,
    onCardClick: () -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    onShareClick: (Article) -> Unit,
    onToggleUnreadClick: (Article) -> Unit,
    onStarChanged: (Article, Boolean) -> Unit,
    onSwiped: (Article) -> Unit,
) {
    val (article, feedWithFavIcon) = articleWithFeed
    val (feed, favIcon) = feedWithFavIcon
    val feedNameOrAuthor = if (displayFeedName) {
        feed.displayTitle.takeIf { it.isNotBlank() } ?: feed.title
    } else {
        stringResource(R.string.author_formatted, article.author)
    }

    SwipeableArticleCard(
        title = article.title,
        flavorImageUrl = article.flavorImageUri,
        excerpt = article.contentExcerpt,
        feedNameOrAuthor = feedNameOrAuthor,
        feedIconUrl = favIcon?.url,
        isUnread = article.isUnread,
        isStarred = article.isStarred,
        onCardClick = onCardClick,
        onOpenInBrowserClick = { onOpenInBrowserClick(article) },
        onStarChanged = { onStarChanged(article, it) },
        onShareClick = { onShareClick(article) },
        onToggleUnreadClick = { onToggleUnreadClick(article) },
        behindCardContent = { direction ->
            if (direction != null) {
                ChangeReadBehindItem(direction)
            }
        },
        onSwiped = { onSwiped(article) },
    )
}


@Composable
private fun ChangeReadBehindItem(dismissDirection: DismissDirection) {
    val horizontalArrangement = when (dismissDirection) {
        DismissDirection.StartToEnd -> Arrangement.Start
        else -> Arrangement.End
    }
    Row(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = stringResource(id = R.string.mark_as_read)
        if (dismissDirection == DismissDirection.StartToEnd) {
            Icon(painter = painterResource(R.drawable.ic_archive), contentDescription = text,
                modifier = Modifier.padding(end = 8.dp),
                tint = MaterialTheme.colors.secondary
            )
        }
        Text(text,
            style = MaterialTheme.typography.caption)
        if (dismissDirection == DismissDirection.EndToStart) {
            Icon(painter = painterResource(R.drawable.ic_archive), contentDescription = text,
                modifier = Modifier.padding(start = 8.dp),
                tint = MaterialTheme.colors.secondary
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ArticleCardList() {
    val articles = List(25) {
        val a = Article(id = it.toLong(),
            contentData = ArticleContentIndexed("article $it", author = "author $it"),
            contentExcerpt = "Excerpt $it"
        )
        val feed = Feed(id = it.toLong(), title = "Feed $it")
        ArticleWithFeed(a, FeedWithFavIcon(feed, FeedFavIcon()))
    }
    val articlesFlow = flowOf(PagingData.from(articles))
    val pagingItems = articlesFlow.collectAsLazyPagingItems()

    ArticleCardList(
        articles = pagingItems,
        isMultiFeedList = false,
        isRefreshing = false,
        pullRefreshState = rememberPullRefreshState(refreshing = false, onRefresh = { }),
        onCardClick = { _, _ -> },
        onShareClick = {},
        onOpenInBrowserClick = {},
        onToggleUnreadClick = {},
        onStarChanged = { _, _ -> },
        onSwiped = {},
    )
}



/**
 * Returns whether the lazy list is currently scrolling up.
 */
@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Preview
@Composable
fun PreviewArticleCardList() {
    AppTheme {
        ArticleCardList()
    }
}
