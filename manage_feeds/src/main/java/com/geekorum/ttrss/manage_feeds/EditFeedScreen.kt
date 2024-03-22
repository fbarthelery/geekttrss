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
package com.geekorum.ttrss.manage_feeds

import android.accounts.Account
import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.rememberAsyncImagePainter
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.ManageFeedsDao
import com.geekorum.ttrss.manage_feeds.workers.UnsubscribeWorker
import com.geekorum.ttrss.ui.AppTheme3
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.geekorum.ttrss.R as appR

private const val ARG_FEED_ID = "feedId"

/**
 * ViewModel to edit preferences for a feed
 */
@HiltViewModel
class EditFeedViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val account: Account,
    private val feedsDao: ManageFeedsDao
): ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val feed = savedStateHandle.getStateFlow<Long?>(ARG_FEED_ID, null)
        .filterNotNull()
        .flatMapLatest {
        feedsDao.getFeedById(it)
    }

    val uiState = feed.map {
        EditFeedUiState(feed = it?.feed, faviconUrl = it?.favIcon?.url )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EditFeedUiState())

    fun unsubscribeFeed() {
        val currentUiState = uiState.value
        val feed = checkNotNull(currentUiState.feed)
        unsubscribeFeed(feed.id)
    }

    private fun unsubscribeFeed(feedId: Long) {
        viewModelScope.launch {
            feedsDao.updateIsSubscribedFeed(feedId, false)
        }
        val workManager = WorkManager.getInstance(application)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<UnsubscribeWorker>()
            .setConstraints(constraints)
            .setInputData(UnsubscribeWorker.getInputData(account, feedId))
            .build()
        workManager.enqueue(request)
    }
}


data class EditFeedUiState(
    val feed: Feed? = null,
    val faviconUrl: String? = null
) {
    val title: String = feed?.title ?: ""
    val url: String = feed?.url ?: ""
    val isSubscribed: Boolean = feed?.isSubscribed ?: true
}


@Composable
fun EditFeedScreen(viewModel: EditFeedViewModel = dfmHiltViewModel(), navigateBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.isSubscribed) {
        if (!uiState.isSubscribed) {
            navigateBack()
        }
    }
    EditFeedScreen(uiState = uiState, onUnsubscribeFeed = viewModel::unsubscribeFeed)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFeedScreen(
    uiState: EditFeedUiState, onUnsubscribeFeed: () -> Unit
) {
    Scaffold(topBar = {
        LargeTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val feedIconPainter = rememberAsyncImagePainter(
                        model = uiState.faviconUrl,
                        placeholder = painterResource(appR.drawable.ic_rss_feed_orange),
                        fallback = painterResource(appR.drawable.ic_rss_feed_orange),
                        error = painterResource(appR.drawable.ic_rss_feed_orange),
                    )
                    Image(
                        painter = feedIconPainter,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(48.dp)
                    )
                    Text(uiState.title,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                    )
                }
            },
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            ))
    }) {
        var showUnsubscribeDialog by remember { mutableStateOf(false) }

        Column(
            Modifier
                .padding(it)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(stringResource(R.string.lbl_feed_url), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(uiState.url)

            TextButton(modifier = Modifier
                .padding(top = 32.dp)
                .align(Alignment.End),
                onClick = { showUnsubscribeDialog = true }) {
                Text(stringResource(R.string.btn_unsubscribe))
            }
        }

        if (showUnsubscribeDialog) {
            ConfirmUnsubscribeDialog(
                feed = uiState.feed!!,
                onDismissRequest = { showUnsubscribeDialog = false },
                onConfirmClick = onUnsubscribeFeed
            )
        }
    }
}


@Composable
fun ConfirmUnsubscribeDialog(
    feed: Feed,
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(stringResource(R.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.btn_cancel))
            }
        },
        title = {
            Text(stringResource(R.string.fragment_confirmation_title),
                style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column {
                Text(stringResource(R.string.lbl_unsubscribe_msg), style = MaterialTheme.typography.bodyLarge)
                Text(feed.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp))
                Text(feed.url)
            }
        })
}

@Preview
@Composable
private fun PreviewConfirmUnsubscribeDialog() {
    AppTheme3 {
        val feed = Feed(
            id = 4,
            title = "LinuxFr",
            url = "https://linuxfr.org/feed",
            unreadCount = 8,
        )
        ConfirmUnsubscribeDialog(feed,
            onDismissRequest = {},
            onConfirmClick = {})
    }
}

@Preview
@Composable
private fun PreviewEditFeedScreen() {
    AppTheme3 {
        val uiState = EditFeedUiState(
            feed = Feed(
                title = "Android developers",
                url = "https://medium.com/feed/androiddevelopers"
            ),
            faviconUrl = null
        )

        EditFeedScreen(uiState, onUnsubscribeFeed = {})
    }
}