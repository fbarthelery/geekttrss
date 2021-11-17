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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleContentIndexed
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.ui.AppTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop


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



@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ArticleCardList(
    viewModel: BaseArticlesViewModel,
    onCardClick: (Int, Article) -> Unit,
    onShareClick: (Article) -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    additionalContentPaddingBottom: Dp = 0.dp,
) {
    val isRefreshing by viewModel.isRefreshing.observeAsState(false)
    SwipeRefresh(rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            viewModel.refresh()
        },
        modifier = modifier
    ) {
        val pagingItems = viewModel.articles.collectAsLazyPagingItems()
        val loadState by pagingViewStateFor(pagingItems)
        val isEmpty = pagingItems.itemCount == 0
        var refreshIfEmpty = remember { true }
        LaunchedEffect(loadState, isEmpty) {
            if (loadState == PagingViewLoadState.LOADED) {
                if (isEmpty && refreshIfEmpty) {
                    viewModel.refresh()
                }
                // refresh again if we loaded some items
                refreshIfEmpty = !isEmpty
            }
        }

        if (isEmpty && loadState == PagingViewLoadState.LOADED) {
            FeedEmptyText(isRefreshing)
            return@SwipeRefresh
        }

        val listState = rememberLazyListState()
        var animateItemAppearance by remember { mutableStateOf(true) }
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.navigationBars,
                additionalBottom = additionalContentPaddingBottom,
                additionalStart = 8.dp,
                additionalTop = 8.dp,
                additionalEnd = 8.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(pagingItems,
                key = { _, articleWithFeed -> articleWithFeed.article.id }
            ) { index, articleWithFeed ->
                // initial state is visible if we don't animate
                val visibilityState = remember { MutableTransitionState(!animateItemAppearance) }
                // delay start of animation
                LaunchedEffect(index, Unit) {
                    if (!animateItemAppearance) {
                        return@LaunchedEffect
                    }
                    if (index == listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ) {
                        animateItemAppearance = false
                    }
                    delay(38L * index)
                    visibilityState.targetState = true
                }

                AnimatedVisibility(visibilityState,
                    enter = fadeIn() + slideInVertically { it / 3 },
                    modifier = Modifier.animateItemPlacement()
                ) {
                    if (articleWithFeed != null) {
                        SwipeableArticleCard(
                            articleWithFeed = articleWithFeed,
                            viewModel = viewModel,
                            onCardClick = { onCardClick(index, articleWithFeed.article) },
                            onOpenInBrowserClick = onOpenInBrowserClick,
                            onShareClick = onShareClick)
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
    viewModel: BaseArticlesViewModel,
    onCardClick: () -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    onShareClick: (Article) -> Unit
) {
    val (article, feed) = articleWithFeed
    val displayFeedName by viewModel.isMultiFeed.collectAsState()
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
        feedIconUrl = feed.feedIconUrl,
        isUnread = article.isUnread,
        isStarred = article.isStarred,
        onCardClick = onCardClick,
        onOpenInBrowserClick = { onOpenInBrowserClick(article) },
        onStarChanged = { viewModel.setArticleStarred(article.id, it) },
        onShareClick = { onShareClick(article) },
        onToggleUnreadClick = {
            viewModel.setArticleUnread(article.id, !article.isTransientUnread)
        },
        behindCardContent = { direction ->
            if (direction != null) {
                ChangeReadBehindItem(direction)
            }
        },
        onSwiped = {
            viewModel.setArticleUnread(article.id, false)
        }
    )
}


@Composable
private fun ChangeReadBehindItem(dismissDirection: DismissDirection) {
    val horizontalArrangement = when(dismissDirection) {
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
    val articles = Array(25) {
        Article(id = it.toLong(),
            contentData = ArticleContentIndexed("article $it", author = "author $it"),
            contentExcerpt = "Excerpt $it"
        )
    }
    val articlesState = remember { mutableStateListOf(*articles)}

    val isRefreshing = false
    SwipeRefresh(rememberSwipeRefreshState(isRefreshing),
        onRefresh = { /*TODO*/ }
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(articlesState, key = { it.id }) { article ->
                SwipeableArticleCard(
//                TODO add this on beta03
//                modifier = Modifier.animateItemPlacement(),
                    title = article.title,
                    flavorImageUrl = article.flavorImageUri,
                    excerpt = article.contentExcerpt,
                    feedNameOrAuthor = article.author,
                    feedIconUrl = "",
                    isUnread = article.isTransientUnread,
                    isStarred = article.isStarred,
                    onCardClick = {},
                    onOpenInBrowserClick = {},
                    onStarChanged = {  },
                    onShareClick = {},
                    onToggleUnreadClick = {  },
                    behindCardContent = { direction ->
                        if (direction != null) {
                            val color = if (direction == DismissDirection.StartToEnd)
                                Color.Blue
                            else Color.Red
                            Box(Modifier
                                .fillMaxSize()
                                .background(color)) {
                                ChangeReadBehindItem(direction)
                            }
                        }
                    },
                    onSwiped = {
                        articlesState.remove(article)
                    }
                )
            }
        }
    }
}


@Preview
@Composable
fun PreviewArticleCardList() {
    AppTheme {
        ArticleCardList()
    }
}
