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
package com.geekorum.ttrss.articles_list.magazine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.articles_list.*
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.ui.AppTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MagazineFragment: Fragment() {
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val magazineViewModel: MagazineViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ProvideWindowInsets {
                    AppTheme {
                        val appBarHeightDp = with(LocalDensity.current) {
                            activityViewModel.appBarHeight.toDp()
                        }
                        Surface(Modifier.fillMaxSize()) {
                            ArticlesMagazine(
                                viewModel = magazineViewModel,
                                onCardClick = activityViewModel::displayArticle,
                                onShareClick = ::onShareClicked,
                                onOpenInBrowserClick = {
                                    activityViewModel.displayArticleInBrowser(requireContext(), it)
                                },
                                additionalContentPaddingBottom = appBarHeightDp,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityViewModel.refreshClickedEvent.observe(viewLifecycleOwner, EventObserver {
            magazineViewModel.refreshFeeds()
            magazineViewModel.refreshMagazine()
        })
    }

    private fun onShareClicked(article: Article) {
        startActivity(createShareIntent(requireActivity(), article))
    }

    private fun createShareIntent(activity: Activity, article: Article): Intent {
        val shareIntent = ShareCompat.IntentBuilder(activity)
        shareIntent.setSubject(article.title)
            .setHtmlText(article.content)
            .setText(article.link)
            .setType("text/plain")
        return shareIntent.createChooserIntent()
    }

}


@Composable
private fun ArticlesMagazine(
    viewModel: MagazineViewModel,
    onCardClick: (Int, Article) -> Unit,
    onShareClick: (Article) -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    additionalContentPaddingBottom: Dp = 0.dp,
) {

    val isRefreshing by viewModel.isRefreshing.observeAsState(false)
    SwipeRefresh(rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            viewModel.refreshMagazine()
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
                    viewModel.refreshFeeds()
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
                    enter = fadeIn() + slideInVertically { it / 3 }) {
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
    val (article, feed) = articleWithFeed
    val feedNameOrAuthor = feed.displayTitle.takeIf { it.isNotBlank() } ?: feed.title

    ArticleCard(
//                TODO add this on beta03
//                modifier = Modifier.animateItemPlacement(),
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
        }
    )
}
