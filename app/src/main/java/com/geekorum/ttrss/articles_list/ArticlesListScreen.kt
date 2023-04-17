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

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.core.app.ShareCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.ui.AppTheme


@Composable
fun ArticlesListScreen(
    activityViewModel: ActivityViewModel,
    articlesListViewModel: ArticlesListViewModel = hiltViewModel()
) {
    BaseArticlesListScreen(activityViewModel = activityViewModel, articlesListViewModel)
}

@Composable
fun ArticlesListByTagScreen(
    activityViewModel: ActivityViewModel,
    articlesListByTagViewModel: ArticlesListByTagViewModel = hiltViewModel()
) {
    BaseArticlesListScreen(activityViewModel = activityViewModel, articlesListByTagViewModel)
}

@Composable
private fun BaseArticlesListScreen(
    activityViewModel: ActivityViewModel,
    articlesListViewModel: BaseArticlesViewModel
) {
    AppTheme {
        val viewLifecycleOwner = LocalLifecycleOwner.current

        val pendingArticlesSetUnread by articlesListViewModel.pendingArticlesSetUnread.collectAsStateWithLifecycle()
        LaunchedEffect(pendingArticlesSetUnread, activityViewModel) {
            val snackBarMessage = UndoReadSnackbarMessage(pendingArticlesSetUnread,
                onAction = articlesListViewModel::undoSetUnreadActions,
                onDismiss = articlesListViewModel::commitSetUnreadActions)
            activityViewModel.setUndoReadSnackBarMessge(snackBarMessage)
        }

        LaunchedEffect(activityViewModel, articlesListViewModel, viewLifecycleOwner) {
            activityViewModel.refreshClickedEvent.observe(viewLifecycleOwner, EventObserver {
                articlesListViewModel.refresh()
            })

            activityViewModel.mostRecentSortOrder.observe(viewLifecycleOwner) {
                articlesListViewModel.setSortByMostRecentFirst(it)
            }

            activityViewModel.onlyUnreadArticles.observe(viewLifecycleOwner) {
                articlesListViewModel.setNeedUnread(it)
            }
        }

        val appBarHeightDp = with(LocalDensity.current) {
            activityViewModel.appBarHeight.toDp()
        }

        val lazyListState = rememberLazyListState()
        val isScrollingUp = lazyListState.isScrollingUp()
        LaunchedEffect(activityViewModel, isScrollingUp) {
            activityViewModel.setIsScrollingUp(isScrollingUp)
        }

        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        Surface(
            Modifier
                .fillMaxSize()
                // seems to be much better in compose 1.3.0 but keep a look on it
                // https://issuetracker.google.com/issues/236451818
                .nestedScroll(nestedScrollInterop)
        ) {
            val context = LocalContext.current
            ArticleCardList(
                viewModel = articlesListViewModel,
                listState = lazyListState,
                onCardClick = activityViewModel::displayArticle,
                onShareClick = {
                    onShareClicked(context, it)
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

private fun onShareClicked(context: Context, article: Article) {
    context.startActivity(createShareIntent(context, article))
}

private fun createShareIntent(context: Context, article: Article): Intent {
    val shareIntent = ShareCompat.IntentBuilder(context)
    shareIntent.setSubject(article.title)
        .setHtmlText(article.content)
        .setText(article.link)
        .setType("text/plain")
    return shareIntent.createChooserIntent()
}
