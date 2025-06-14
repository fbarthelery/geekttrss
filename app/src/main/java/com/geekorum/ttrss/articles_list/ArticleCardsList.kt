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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleContentIndexed
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.FeedFavIcon
import com.geekorum.ttrss.data.FeedWithFavIcon
import com.geekorum.ttrss.ui.AppTheme3
import com.geekorum.ttrss.ui.components.SwipeableItemBackgroundContainer
import com.geekorum.ttrss.ui.components.alignInSwipeableItemBackgroundContainer
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf


/**
 * Get a debounced paging [LoadState] for [pagingItems].
 * As we make lots of write while loading data, this allows to not
 * go between loading/not loading in quick successions.
 */
@OptIn(FlowPreview::class)
@Composable
fun debouncedPagingViewStateFor(pagingItems: LazyPagingItems<ArticleWithFeed>) =
    produceState(initialValue = pagingItems.loadState.refresh, key1 = pagingItems) {
        snapshotFlow { pagingItems.loadState.refresh }
            .distinctUntilChanged()
            .debounce(500)
            .collect {
                value = it
            }
    }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleCardList(
    viewModel: BaseArticlesViewModel,
    listState: LazyListState = rememberLazyListState(),
    browserApplicationIcon: Drawable?,
    displayCompactItems: Boolean,
    onCardClick: (Int, Article) -> Unit,
    onShareClick: (Article) -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val pullRefreshState = rememberPullToRefreshState()
    val articles = viewModel.articles.collectAsLazyPagingItems()
    val isMultiFeedList by viewModel.isMultiFeed.collectAsState()

    val loadState by debouncedPagingViewStateFor(articles)
    val isEmpty = articles.itemCount == 0
    var refreshIfEmpty by remember { mutableStateOf(true) }
    LaunchedEffect(loadState, isEmpty) {
        if (loadState is LoadState.NotLoading) {
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
        browserApplicationIcon = browserApplicationIcon,
        displayCompactItems = displayCompactItems,
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
        onRefresh = {
            viewModel.refresh()
        },
        modifier = modifier,
        contentPadding = contentPadding
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleCardList(
    articles: LazyPagingItems<ArticleWithFeed>,
    isMultiFeedList: Boolean,
    isRefreshing: Boolean,
    pullRefreshState: PullToRefreshState,
    browserApplicationIcon: Drawable?,
    displayCompactItems: Boolean,
    onRefresh: () -> Unit,
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

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullRefreshState,
        modifier = modifier
            .padding(pullRefreshBoxContentPadding)
    ) {
        val loadState by debouncedPagingViewStateFor(articles)
        val isEmpty = articles.itemCount == 0
        if (isEmpty && loadState is LoadState.NotLoading) {
            FeedEmptyText(isRefreshing)
        } else {
            ArticlesList(
                articles,
                listState,
                isMultiFeedList,
                browserApplicationIcon,
                lazyListContentPadding,
                displayCompactItems,
                onCardClick,
                onOpenInBrowserClick,
                onShareClick,
                onStarChanged,
                onToggleUnreadClick,
                onSwiped
            )
        }
    }
}

@Composable
private fun ArticlesList(
    articles: LazyPagingItems<ArticleWithFeed>,
    listState: LazyListState,
    isMultiFeedList: Boolean,
    browserApplicationIcon: Drawable?,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    displayCompactItems: Boolean = false,
    onCardClick: (Int, Article) -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    onShareClick: (Article) -> Unit,
    onStarChanged: (Article, Boolean) -> Unit,
    onToggleUnreadClick: (Article) -> Unit,
    onSwiped: (Article) -> Unit
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
                modifier = Modifier.animateItem(fadeInSpec = null)
            ) {
                if (articleWithFeed != null) {
                    if (displayCompactItems) {
                        Column {
                            SwipeableArticleItem(
                                articleWithFeed = articleWithFeed,
                                displayFeedName = isMultiFeedList,
                                browserApplicationIcon = browserApplicationIcon,
                                displayCompactItem = displayCompactItems,
                                onItemClick = { onCardClick(index, articleWithFeed.article) },
                                onOpenInBrowserClick = onOpenInBrowserClick,
                                onShareClick = onShareClick,
                                onStarChanged = onStarChanged,
                                onToggleUnreadClick = onToggleUnreadClick,
                                onSwiped = onSwiped
                            )
                            HorizontalDivider()
                        }
                    } else {
                        SwipeableArticleItem(
                            articleWithFeed = articleWithFeed,
                            displayFeedName = isMultiFeedList,
                            browserApplicationIcon = browserApplicationIcon,
                            displayCompactItem = displayCompactItems,
                            onItemClick = { onCardClick(index, articleWithFeed.article) },
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
}

@Composable
fun FeedEmptyText(isRefreshing: Boolean) {
    val emptyText = if (isRefreshing) {
        stringResource(R.string.fragment_articles_list_no_articles_and_sync_lbl)
    } else stringResource(R.string.fragment_articles_list_no_articles_lbl)
    Box(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(emptyText,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 192.dp)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableArticleItem(
    articleWithFeed: ArticleWithFeed,
    displayFeedName: Boolean,
    browserApplicationIcon: Drawable?,
    displayCompactItem: Boolean,
    onItemClick: () -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    onShareClick: (Article) -> Unit,
    onToggleUnreadClick: (Article) -> Unit,
    onStarChanged: (Article, Boolean) -> Unit,
    onSwiped: (Article) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (article, feedWithFavIcon) = articleWithFeed
    val (feed, favIcon) = feedWithFavIcon
    val feedNameOrAuthor = if (displayFeedName) {
        feed.displayTitle.takeIf { it.isNotBlank() } ?: feed.title
    } else {
        stringResource(R.string.author_formatted, article.author)
    }

    if (displayCompactItem) {
        SwipeableCompactArticleListItem(
            title = article.title,
            flavorImageUrl = article.flavorImageUri,
            feedNameOrAuthor = feedNameOrAuthor,
            browserApplicationIcon = browserApplicationIcon,
            feedIconUrl = favIcon?.url,
            isUnread = article.isUnread,
            isStarred = article.isStarred,
            onItemClick = onItemClick,
            onOpenInBrowserClick = { onOpenInBrowserClick(article) },
            onStarChanged = { onStarChanged(article, it) },
            onShareClick = { onShareClick(article) },
            onToggleUnreadClick = { onToggleUnreadClick(article) },
            behindCardContent = { direction, _, progress ->
                ChangeReadBehindItem(direction, progress)
            },
            onSwiped = { onSwiped(article) },
            modifier = modifier
        )
    } else {
        SwipeableArticleCard(
            title = article.title,
            flavorImageUrl = article.flavorImageUri,
            excerpt = article.contentExcerpt,
            feedNameOrAuthor = feedNameOrAuthor,
            browserApplicationIcon = browserApplicationIcon,
            feedIconUrl = favIcon?.url,
            isUnread = article.isUnread,
            isStarred = article.isStarred,
            onCardClick = onItemClick,
            onOpenInBrowserClick = { onOpenInBrowserClick(article) },
            onStarChanged = { onStarChanged(article, it) },
            onShareClick = { onShareClick(article) },
            onToggleUnreadClick = { onToggleUnreadClick(article) },
            behindCardContent = { direction, _, progress ->
                ChangeReadBehindItem(direction, progress)
            },
            onSwiped = { onSwiped(article) },
            modifier = modifier
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeReadBehindItem(dismissDirection: SwipeToDismissBoxValue) {
    val horizontalArrangement = when (dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Arrangement.Start
        else -> Arrangement.End
    }
    Row(modifier = Modifier
        .fillMaxSize()
        // center it horizontally then take max width of a card (384.dp)
        .wrapContentWidth()
        .width(384.dp)
        .padding(horizontal = 16.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = stringResource(id = R.string.mark_as_read)
        if (dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
            Icon(AppTheme3.Icons.Archive, contentDescription = text,
                modifier = Modifier.padding(end = 8.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        Text(text,
            style = MaterialTheme.typography.bodySmall)
        if (dismissDirection == SwipeToDismissBoxValue.EndToStart) {
            Icon(AppTheme3.Icons.Archive, contentDescription = text,
                modifier = Modifier.padding(start = 8.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun ChangeReadBehindItem(dismissDirection: SwipeToDismissBoxValue, progress: Float) {
    val containerHorizontalAlignment =
        if (dismissDirection == SwipeToDismissBoxValue.EndToStart) Alignment.End else Alignment.Start

    SwipeableItemBackgroundContainer(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.extraLarge,
        progress = progress,
        modifier = Modifier.fillMaxWidth()
            .wrapContentWidth(containerHorizontalAlignment)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxHeight()
                .wrapContentWidth(unbounded = true, // layout with Infinite MaxWidth
                    align = alignInSwipeableItemBackgroundContainer(dismissDirection, Alignment.CenterHorizontally)
                )
        ){
            val text = stringResource(id = R.string.mark_as_read)
            // use inner padding to avoid to deal with it during alignment
            Spacer(Modifier.width(24.dp))
            if (dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                Icon(AppTheme3.Icons.Archive, contentDescription = text,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            Text(text)
            if (dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Icon(AppTheme3.Icons.Archive, contentDescription = text,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Spacer(Modifier.width(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
        browserApplicationIcon = null,
        displayCompactItems = false,
        pullRefreshState = rememberPullToRefreshState(),
        onCardClick = { _, _ -> },
        onShareClick = {},
        onOpenInBrowserClick = {},
        onToggleUnreadClick = {},
        onStarChanged = { _, _ -> },
        onSwiped = {},
        onRefresh = {}
    )
}



/**
 * Returns whether the lazy list is currently scrolling up.
 */
@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
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
    AppTheme3 {
        ArticleCardList()
    }
}
