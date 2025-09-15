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
package com.geekorum.ttrss.articles_list

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.share.createShareArticleIntent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@Composable
fun ArticlesListScreen(
    feedId: Long,
    windowSizeClass: WindowSizeClass,
    activityViewModel: ActivityViewModel,
    articlesListViewModel: ArticlesListViewModel = hiltViewModel { factory: ArticlesListViewModel.Factory ->
        factory.create(feedId)
    },
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val compactItemsInSmallScreens by activityViewModel.displayCompactItems.collectAsStateWithLifecycle()
    val displayCompactItems = compactItemsInSmallScreens
            && (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact ||
            windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact)

    BaseArticlesListScreen(activityViewModel = activityViewModel, articlesListViewModel,
        displayCompactItems, contentPadding)
}

@Composable
fun ArticlesListByTagScreen(
    tag: String,
    windowSizeClass: WindowSizeClass,
    activityViewModel: ActivityViewModel,
    articlesListByTagViewModel: ArticlesListByTagViewModel = hiltViewModel { factory: ArticlesListByTagViewModel.Factory ->
        factory.create(tag)
    },
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val compactItemsInSmallScreens by activityViewModel.displayCompactItems.collectAsStateWithLifecycle()
    val displayCompactItems = compactItemsInSmallScreens
            && (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact ||
            windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact)

    BaseArticlesListScreen(activityViewModel = activityViewModel, articlesListByTagViewModel,
        displayCompactItems, contentPadding)
}

@Composable
private fun BaseArticlesListScreen(
    activityViewModel: ActivityViewModel,
    articlesListViewModel: BaseArticlesViewModel,
    displayContactItems: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val viewLifecycleOwner = LocalLifecycleOwner.current

    val pendingArticlesSetUnread by articlesListViewModel.pendingArticlesSetUnread.collectAsStateWithLifecycle()
    LaunchedEffect(pendingArticlesSetUnread, activityViewModel) {
        val snackBarMessage = UndoReadSnackbarMessage(pendingArticlesSetUnread,
            onAction = articlesListViewModel::undoSetUnreadActions,
            onDismiss = articlesListViewModel::commitSetUnreadActions)
        activityViewModel.setUndoReadSnackBarMessage(snackBarMessage)
    }

    LaunchedEffect(activityViewModel, articlesListViewModel, viewLifecycleOwner) {
        activityViewModel.refreshClickedEvent.observe(viewLifecycleOwner, EventObserver {
            articlesListViewModel.refresh()
        })

        activityViewModel.mostRecentSortOrder.onEach {
            articlesListViewModel.setSortByMostRecentFirst(it)
        }.launchIn(this)

        activityViewModel.onlyUnreadArticles.onEach {
            articlesListViewModel.setNeedUnread(it)
        }.launchIn(this)
    }

    val lazyListState = rememberLazyListState()
    val isScrollingUp = lazyListState.isScrollingUp()
    LaunchedEffect(activityViewModel, isScrollingUp) {
        activityViewModel.setIsScrollingUp(isScrollingUp)
    }

    val browserIcon by activityViewModel.browserIcon.collectAsStateWithLifecycle()
    Surface(
        Modifier.fillMaxSize()
    ) {
        val context = LocalContext.current
        ArticleCardList(
            viewModel = articlesListViewModel,
            listState = lazyListState,
            browserApplicationIcon = browserIcon,
            displayCompactItems = displayContactItems,
            onCardClick = activityViewModel::displayArticle,
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
