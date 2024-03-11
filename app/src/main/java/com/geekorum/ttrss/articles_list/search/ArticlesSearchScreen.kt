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
package com.geekorum.ttrss.articles_list.search

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.geekorum.ttrss.articles_list.*
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.share.createShareArticleIntent
import kotlinx.coroutines.delay
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchResultCardList(
    viewModel: SearchViewModel,
    browserApplicationIcon: Drawable?,
    displayCompactItems: Boolean,
    onCardClick: (Int, Article) -> Unit,
    onShareClick: (Article) -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val pagingItems = viewModel.articles.collectAsLazyPagingItems()

    val listState = rememberLazyListState()
    var animateItemAppearance by remember { mutableStateOf(true) }
    val loadState by debouncedPagingViewStateFor(pagingItems)
    LaunchedEffect(loadState, pagingItems.itemCount) {
        if (loadState is LoadState.NotLoading) {
            Timber.i("loading item reset animate item appearance")
            animateItemAppearance = true
        }
    }

    val ltr = LocalLayoutDirection.current
    val additionalPadding = if (displayCompactItems) 0.dp else 8.dp
    val lazyListContentPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(ltr) + additionalPadding,
        end = contentPadding.calculateEndPadding(ltr) + additionalPadding,
        bottom = contentPadding.calculateBottomPadding() + additionalPadding,
        top = additionalPadding
    )

    val verticalArrangement = if (displayCompactItems) Arrangement.Top else Arrangement.spacedBy(16.dp)
    LazyColumn(
        state = listState,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = lazyListContentPadding,
        modifier = modifier.fillMaxSize()
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey {it.article.id },
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
                Timber.i("index $index animate appearance")
                visibilityState.targetState = true
            }

            AnimatedVisibility(visibilityState,
                enter = fadeIn() + slideInVertically { it / 3 },
                modifier = Modifier.animateItemPlacement(),
            ) {
                if (articleWithFeed != null) {
                    if (displayCompactItems) {
                        Column {
                            ArticleItem(
                                articleWithFeed = articleWithFeed,
                                viewModel = viewModel,
                                browserApplicationIcon = browserApplicationIcon,
                                displayCompactItem = displayCompactItems,
                                onItemClick = { onCardClick(index, articleWithFeed.article) },
                                onOpenInBrowserClick = onOpenInBrowserClick,
                                onShareClick = onShareClick
                            )
                            HorizontalDivider()
                        }
                    } else {
                        ArticleItem(
                            articleWithFeed = articleWithFeed,
                            viewModel = viewModel,
                            browserApplicationIcon = browserApplicationIcon,
                            displayCompactItem = displayCompactItems,
                            onItemClick = { onCardClick(index, articleWithFeed.article) },
                            onOpenInBrowserClick = onOpenInBrowserClick,
                            onShareClick = onShareClick
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
    viewModel: SearchViewModel,
    browserApplicationIcon: Drawable?,
    displayCompactItem: Boolean,
    onItemClick: () -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    onShareClick: (Article) -> Unit
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
            onItemClick = onItemClick,
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
            onCardClick = onItemClick,
            onOpenInBrowserClick = { onOpenInBrowserClick(article) },
            onStarChanged = { viewModel.setArticleStarred(article.id, it) },
            onShareClick = { onShareClick(article) },
            onToggleUnreadClick = {
                viewModel.setArticleUnread(article.id, !article.isTransientUnread)
            }
        )
    }
}


@Composable
fun ArticlesSearchScreen(
    windowSizeClass: WindowSizeClass,
    activityViewModel: ActivityViewModel,
    searchViewModel: SearchViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LaunchedEffect(activityViewModel, searchViewModel) {
        activityViewModel.searchQuery.collect {
            searchViewModel.setSearchQuery(it)
        }
    }

    val compactItemsInSmallScreens by activityViewModel.displayCompactItems.collectAsStateWithLifecycle()
    val displayCompactItems = compactItemsInSmallScreens
            && (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact ||
            windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact)

    val browserApplicationIcon by activityViewModel.browserIcon.collectAsStateWithLifecycle()
    Surface(Modifier.fillMaxSize()) {
        val context = LocalContext.current
        SearchResultCardList(
            viewModel = searchViewModel,
            onCardClick = activityViewModel::displayArticle,
            browserApplicationIcon = browserApplicationIcon,
            displayCompactItems = displayCompactItems,
            onShareClick = {
                onShareClicked(context, it)
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


private fun onShareClicked(context: Context, article: Article) {
    context.startActivity(createShareArticleIntent(context, article))
}
