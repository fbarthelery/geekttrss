/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.geekorum.ttrss.R
import com.geekorum.ttrss.articles_list.ActivityViewModel
import com.geekorum.ttrss.articles_list.ArticleCard
import com.geekorum.ttrss.articles_list.CompactArticleListItem
import com.geekorum.ttrss.articles_list.debouncedPagingViewStateFor
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.share.createShareArticleIntent
import com.geekorum.ttrss.ui.components.plus
import kotlinx.coroutines.delay
import timber.log.Timber

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
    val loadState by debouncedPagingViewStateFor(pagingItems)

    val isEmpty = pagingItems.itemCount == 0
    if (isEmpty && (loadState is LoadState.NotLoading || loadState is LoadState.Error)) {
        NoResultsForQuery(viewModel.query, modifier = Modifier.padding(contentPadding))
        return
    }

    val listState = rememberLazyListState()
    var animateItemAppearance by remember { mutableStateOf(true) }
    LaunchedEffect(loadState, pagingItems.itemCount) {
        if (loadState is LoadState.NotLoading) {
            Timber.i("loading item reset animate item appearance")
            animateItemAppearance = true
        }
    }

    val verticalArrangement = if (displayCompactItems) Arrangement.Top else Arrangement.spacedBy(16.dp)
    LazyColumn(
        state = listState,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = contentPadding,
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
                modifier = Modifier.animateItem(fadeInSpec = null),
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


// don't pass content padding value.
// we always layout like we take fullscreen and there is a search bar on top of screen
@Composable
fun ArticlesSearchScreen(
    query: String,
    windowSizeClass: WindowSizeClass,
    activityViewModel: ActivityViewModel,
    searchViewModel: SearchViewModel = hiltViewModel{ factory: SearchViewModel.Factory ->
        factory.create(query)
    },
) {
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
            // 72dp = SearchBar + searchbar vertical padding
            // add 16.dp of top padding between first item and bar
            contentPadding = PaddingValues(top = 88.dp) + WindowInsets.safeDrawing.asPaddingValues(),
            modifier = Modifier
                .fillMaxSize()
        )
    }
}


@Composable
private fun NoResultsForQuery(query: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 16.dp)) {
        Text(buildNoArticlesText(query), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.label_articles_search_no_results_instructions),
            Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun buildNoArticlesText(query: String): AnnotatedString {
    val text = stringResource(R.string.label_articles_search_no_results, query)
    return buildAnnotatedString {
        append(text)
        val startIndex = text.indexOf('"')
        val endIndex = text.lastIndexOf('"')
        val queryStyle = SpanStyle(color = MaterialTheme.colorScheme.primary)
        addStyle(queryStyle, startIndex, endIndex)
    }
}

private fun onShareClicked(context: Context, article: Article) {
    context.startActivity(createShareArticleIntent(context, article))
}
