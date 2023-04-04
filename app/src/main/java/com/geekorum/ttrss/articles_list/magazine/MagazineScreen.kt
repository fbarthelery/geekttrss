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
package com.geekorum.ttrss.articles_list.magazine

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.geekorum.ttrss.articles_list.*
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.ui.AppTheme
import kotlinx.coroutines.delay


private fun createShareIntent(context: Context, article: Article): Intent {
    val shareIntent = ShareCompat.IntentBuilder(context)
    shareIntent.setSubject(article.title)
        .setHtmlText(article.content)
        .setText(article.link)
        .setType("text/plain")
    return shareIntent.createChooserIntent()
}


@Composable
fun MagazineScreen(
    activityViewModel: ActivityViewModel,
    magazineViewModel: MagazineViewModel = hiltViewModel()
) {
    AppTheme {
        val appBarHeightDp = with(LocalDensity.current) {
            activityViewModel.appBarHeight.toDp()
        }

        Surface(Modifier.fillMaxSize()) {
            val context = LocalContext.current
            val lazyListState = rememberLazyListState()
            val isScrollingUp = lazyListState.isScrollingUp()
            LaunchedEffect(activityViewModel, isScrollingUp) {
                activityViewModel.setIsScrollingUp(isScrollingUp)
            }
            ArticlesMagazine(
                viewModel = magazineViewModel,
                listState = lazyListState,
                onCardClick = activityViewModel::displayArticle,
                onShareClick = { article ->
                    context.startActivity(createShareIntent(context, article))
                },
                onOpenInBrowserClick = {
                    activityViewModel.displayArticleInBrowser(context, it)
                },
                additionalContentPaddingBottom = appBarHeightDp,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
private fun ArticlesMagazine(
    viewModel: MagazineViewModel,
    onCardClick: (Int, Article) -> Unit,
    onShareClick: (Article) -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    additionalContentPaddingBottom: Dp = 0.dp,
) {

    val isRefreshing by viewModel.isRefreshing.observeAsState(false)
    val pullRefreshState = rememberPullRefreshState(isRefreshing, onRefresh = {
        viewModel.refreshMagazine()
    })
    Box(modifier.pullRefresh(pullRefreshState)) {
        val pagingItems = viewModel.articles.collectAsLazyPagingItems()
        val loadState by pagingViewStateFor(pagingItems)
        val isEmpty = pagingItems.itemCount == 0
        var refreshIfEmpty = remember { true }
        LaunchedEffect(loadState, isEmpty) {
            if (loadState == PagingViewLoadState.LOADED) {
                if (isEmpty && refreshIfEmpty) {
                    viewModel.refreshFeeds()
                }
                // refresh again if we loaded some items
                refreshIfEmpty = !isEmpty
            }
        }

        if (isEmpty && loadState == PagingViewLoadState.LOADED) {
            FeedEmptyText(isRefreshing)
            return@Box
        }

        var animateItemAppearance by remember { mutableStateOf(true) }
        val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
        val contentPadding = PaddingValues(
            start = navBarPadding.calculateStartPadding(LocalLayoutDirection.current) + 8.dp,
            top = navBarPadding.calculateTopPadding() + 8.dp,
            end = navBarPadding.calculateEndPadding(LocalLayoutDirection.current) + 8.dp,
            bottom = navBarPadding.calculateBottomPadding() + additionalContentPaddingBottom
        )
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = contentPadding,
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
                    if (index == listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index) {
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
                        ArticleCard(
                            articleWithFeed = articleWithFeed,
                            viewModel = viewModel,
                            onCardClick = { onCardClick(index, articleWithFeed.article) },
                            onOpenInBrowserClick = onOpenInBrowserClick,
                            onShareClick = onShareClick)
                    }
                }
            }
        }

        PullRefreshIndicator(isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
    }

}

@Composable
private fun ArticleCard(
    articleWithFeed: ArticleWithFeed,
    viewModel: MagazineViewModel,
    onCardClick: () -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    onShareClick: (Article) -> Unit
) {
    val (article, feedWithFavIcon) = articleWithFeed
    val (feed, favIcon) = feedWithFavIcon
    val feedNameOrAuthor = feed.displayTitle.takeIf { it.isNotBlank() } ?: feed.title

    ArticleCard(
        title = article.title,
        flavorImageUrl = article.flavorImageUri,
        excerpt = article.contentExcerpt,
        feedNameOrAuthor = feedNameOrAuthor,
        feedIconUrl = favIcon?.url,
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
