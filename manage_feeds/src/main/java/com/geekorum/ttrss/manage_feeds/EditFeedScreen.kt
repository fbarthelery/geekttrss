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
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import com.geekorum.ttrss.articles_list.ArticleListActivity
import com.geekorum.ttrss.articles_list.createFeedDeepLink
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.ManageFeedsDao
import com.geekorum.ttrss.data.feedsettings.FeedSettings
import com.geekorum.ttrss.data.feedsettings.FeedSettingsRepository
import com.geekorum.ttrss.data.feedsettings.copy
import com.geekorum.ttrss.manage_feeds.workers.UnsubscribeWorker
import com.geekorum.ttrss.ui.AppTheme3
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import com.geekorum.ttrss.R as appR

/**
 * ViewModel to edit preferences for a feed
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = EditFeedViewModel.Factory::class)
class EditFeedViewModel @AssistedInject constructor(
    @Assisted private val feedId: Long,
    private val application: Application,
    private val account: Account,
    private val feedsDao: ManageFeedsDao,
    private val feedSettingsRepository: FeedSettingsRepository,
): ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(feedId: Long): EditFeedViewModel
    }

    private val canCreatePinShortcut = ShortcutManagerCompat.isRequestPinShortcutSupported(application)

    private val feed = feedsDao.getFeedById(feedId)

    private val feedSettings = feedSettingsRepository.getFeedSettings(feedId)

    val uiState = feed.combine(feedSettings) { feedInfo, settings ->
        EditFeedUiState(feed = feedInfo?.feed,
            faviconUrl = feedInfo?.favIcon?.url,
            canCreatePinShortcut = canCreatePinShortcut,
            isSyncingAutomatically = settings?.syncPeriodically ?: true
        )
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

    fun createShortcut(context: Context) = viewModelScope.launch {
        val feed = checkNotNull(uiState.value.feed)
        val feedIconUrl = uiState.value.faviconUrl

        val shortcutTitle = feed.displayTitle.takeIf { it.isNotBlank() } ?: feed.title
        val intent = Intent(context, ArticleListActivity::class.java).apply {
            data = createFeedDeepLink(feed, shortcutTitle)
            action = Intent.ACTION_VIEW
        }

        val feedIconBitmap = try {
            getFeedIconBitmap(context, feedIconUrl)
        } catch (e: Exception) {
            Timber.w(e, "Unable to get favicon bitmap")
            null
        }

        val shortcutIcon = feedIconBitmap?.let { IconCompat.createWithBitmap(it) }
            ?: IconCompat.createWithResource(context, appR.mipmap.ic_launcher)

        val shortcutInfo = ShortcutInfoCompat.Builder(context, getShortcutId(feed.id))
            .setShortLabel(shortcutTitle)
            .setLongLabel(feed.displayTitle.takeIf { it.isNotBlank() } ?: feed.title)
            .setIcon(shortcutIcon)
            .setIntent(intent)
            .build()
        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }

    fun setSyncAutomatically(value: Boolean) = viewModelScope.launch {
        val feedSettings = feedSettingsRepository.getFeedSettings(feedId).firstOrNull() ?: FeedSettings.getDefaultInstance()
        val update = feedSettings.copy { syncPeriodically = value }
        feedSettingsRepository.updateFeedSettings(feedId, update)
    }

    private suspend fun getFeedIconBitmap(
        context: Context,
        feedIconUrl: String?
    ): Bitmap? {
        val imageLoader = context.imageLoader
        val request = ImageRequest.Builder(context)
            .data(feedIconUrl)
            .build()
        val drawable = imageLoader.execute(request).drawable
        val bitmap = (drawable as? BitmapDrawable)?.bitmap
        return bitmap
    }

    private fun getShortcutId(feedId: Long) = "FEED_$feedId"
}

@HiltViewModel(assistedFactory = EditSpecialFeedViewModel.Factory::class)
class EditSpecialFeedViewModel @AssistedInject constructor(
    @Assisted val feedId: Long,
    private val application: Application,
): ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(feedId: Long): EditSpecialFeedViewModel
    }

    val uiState = flow {
        val canCreatePinShortcut = ShortcutManagerCompat.isRequestPinShortcutSupported(application)
        val feed = Feed.createVirtualFeedForId(feedId)
        val state = EditFeedUiState(feed = feed, canCreatePinShortcut = canCreatePinShortcut)
        emit(state)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EditFeedUiState())

    fun createShortcut(context: Context, title: String) = viewModelScope.launch {
        val feed = checkNotNull(uiState.value.feed)

        val intent = Intent(context, ArticleListActivity::class.java).apply {
            data = createFeedDeepLink(feed, title)
            action = Intent.ACTION_VIEW
        }

        val shortcutIcon = IconCompat.createWithResource(context, appR.mipmap.ic_launcher)

        val shortcutInfo = ShortcutInfoCompat.Builder(context, getShortcutId(feed.id))
            .setShortLabel(title)
            .setLongLabel(title)
            .setIcon(shortcutIcon)
            .setIntent(intent)
            .build()
        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }

    private fun getShortcutId(feedId: Long) = "FEED_$feedId"
}



data class EditFeedUiState(
    val feed: Feed? = null,
    val faviconUrl: String? = null,
    val canCreatePinShortcut: Boolean = false,
    val isSyncingAutomatically: Boolean = true,
) {
    val title: String = feed?.title ?: ""
    val url: String = feed?.url ?: ""
    val isSubscribed: Boolean = feed?.isSubscribed ?: true
}


@Composable
fun EditFeedScreen(feedId: Long, navigateBack: () -> Unit) {
    if (Feed.isVirtualFeed(feedId)) {
        EditSpecialFeedScreen(feedId)
    } else {
        EditNormalFeedScreen(feedId = feedId, navigateBack = navigateBack)
    }
}

@Composable
fun EditNormalFeedScreen(
    feedId: Long,
    viewModel: EditFeedViewModel = dfmHiltViewModel { factory: EditFeedViewModel.Factory ->
        factory.create(feedId)
    },
    navigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.isSubscribed) {
        if (!uiState.isSubscribed) {
            navigateBack()
        }
    }
    val context = LocalContext.current
    EditFeedScreen(uiState = uiState,
        onUnsubscribeFeed = viewModel::unsubscribeFeed,
        onCreateShortcutToFeed = {
            viewModel.createShortcut(context)
        },
        onSyncAutomaticallyChange = {
            viewModel.setSyncAutomatically(it)
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFeedScreen(
    uiState: EditFeedUiState,
    onUnsubscribeFeed: () -> Unit,
    onCreateShortcutToFeed: () -> Unit,
    onSyncAutomaticallyChange: (Boolean) -> Unit,
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            ))
    }) {
        var showUnsubscribeDialog by remember { mutableStateOf(false) }

        Column(
            Modifier
                .padding(it)
                .fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.lbl_feed_url),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Text(uiState.url)
            }

            FeedsSettings(syncAutomatically = uiState.isSyncingAutomatically,
                onSyncAutomaticallyChange = onSyncAutomaticallyChange,
                modifier = Modifier.padding(bottom = 16.dp))

            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .align(Alignment.End)
            ) {
                if (uiState.canCreatePinShortcut) {
                    CreateShortcutButton(onClick = onCreateShortcutToFeed,
                        modifier = Modifier.align(Alignment.End))
                }

                TextButton(modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.End),
                    onClick = { showUnsubscribeDialog = true }) {
                    Text(stringResource(R.string.btn_unsubscribe))
                }
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
private fun FeedsSettings(
    syncAutomatically: Boolean,
    onSyncAutomaticallyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        SwitchPreference(
            headlineContent = {
                Text("Sync automatically")
            },
            checked = syncAutomatically,
            onCheckedChange = onSyncAutomaticallyChange
        )
    }
}

@Composable
private fun SwitchPreference(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    supportingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = modifier.clickable {
            onCheckedChange(!checked)
        }
    )
}


@Composable
fun EditSpecialFeedScreen(feedId: Long, viewModel: EditSpecialFeedViewModel = dfmHiltViewModel { factory: EditSpecialFeedViewModel.Factory ->
    factory.create(feedId)
}) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    EditSpecialFeedScreen(uiState = uiState,
        onCreateShortcutToFeed = {
            viewModel.createShortcut(context, it)
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSpecialFeedScreen(
    uiState: EditFeedUiState,
    onCreateShortcutToFeed: (String) -> Unit
) {
    val feedTitle = run {
        val titleRes = when (uiState.feed?.id) {
            Feed.FEED_ID_FRESH -> appR.string.label_fresh_feeds_title
            Feed.FEED_ID_STARRED -> appR.string.label_starred_feeds_title
            Feed.FEED_ID_ALL_ARTICLES -> appR.string.label_all_articles_feeds_title
            else -> null
        }
        titleRes?.let { stringResource(it) }
            ?: uiState.feed?.displayTitle?.takeIf { it.isNotBlank() }
            ?: uiState.feed?.title ?: ""
    }

    Scaffold(topBar = {
        LargeTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val feed = uiState.feed
                    if (feed != null) {
                        val iconVector = when {
                            feed.isArchivedFeed -> AppTheme3.Icons.Inventory2
                            feed.isStarredFeed -> AppTheme3.Icons.Star
                            feed.isPublishedFeed -> AppTheme3.Icons.CheckBox
                            feed.isFreshFeed -> AppTheme3.Icons.LocalCafe
                            feed.isAllArticlesFeed -> AppTheme3.Icons.FolderOpen
                            else -> null
                        }
                        if (iconVector != null) {
                            Icon(iconVector, contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(48.dp))
                        }
                    }

                    Text(feedTitle,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            ))
    }) {
        Column(
            Modifier
                .padding(it)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            if (uiState.feed != null) {
                val feed = uiState.feed
                val specialFeedDescriptionId = when {
                    feed.isStarredFeed -> R.string.lbl_starred_articles_feed_description
                    feed.isFreshFeed -> R.string.lbl_fresh_articles_feed_description
                    feed.isAllArticlesFeed -> R.string.lbl_all_articles_feed_description
                    else -> null
                }
                val description = specialFeedDescriptionId?.let { stringResource(it) } ?: ""
                Text(description)

                if (uiState.canCreatePinShortcut) {
                    CreateShortcutButton(
                        onClick = { onCreateShortcutToFeed(feedTitle) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateShortcutButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        modifier = modifier,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        onClick = onClick) {
        Row {
            Icon(
                AppTheme3.IconsAutoMirrored.AddToHomeScreen,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.btn_create_shortcut))
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
            faviconUrl = null,
            canCreatePinShortcut = true
        )

        EditFeedScreen(uiState, onUnsubscribeFeed = {}, onCreateShortcutToFeed = {}, onSyncAutomaticallyChange = {})
    }
}

@Preview
@Composable
private fun PreviewEditSpecialFeedScreen() {
    AppTheme3 {
        val uiState = EditFeedUiState(
            feed = Feed(
                id = Feed.FEED_ID_FRESH,
                title = "Fresh articles",
                url = "https://medium.com/feed/androiddevelopers"
            ),
            faviconUrl = null,
            canCreatePinShortcut = true
        )

        EditSpecialFeedScreen(uiState, onCreateShortcutToFeed = {})
    }
}