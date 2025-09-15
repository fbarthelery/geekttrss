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
package com.geekorum.ttrss.manage_feeds.add_feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geekorum.geekdroid.app.lifecycle.Event
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.manage_feeds.R
import com.geekorum.ttrss.ui.AppTheme3
import com.geekorum.ttrss.R as appR


@Composable
fun EnterFeedUrlScreen(
    viewModel: SubscribeToFeedViewModel = viewModel(),
    navigateToDisplayError: (Int) -> Unit,
    navigateToShowAvailableFeeds: () -> Unit,
    finishActivity: () -> Unit,
) {
    val feedsFound by viewModel.feedsFound.observeAsState()
    LaunchedEffect(feedsFound) {
        if (feedsFound != null) {
            when {
                feedsFound!!.isEmpty() -> navigateToDisplayError(R.string.fragment_display_error_no_feeds_found)

                feedsFound!!.size == 1 -> {
                    val info = feedsFound!!.single()
                    if (info.source == FeedsFinder.Source.URL) {
                        viewModel.subscribeToFeed(info.toFeedInformation())
                        finishActivity()
                    } else {
                        navigateToShowAvailableFeeds()
                    }
                }

                else -> navigateToShowAvailableFeeds()
            }
        }
    }

    viewModel.ioErrorEvent.observeEvent(onEvent = { navigateToDisplayError(R.string.fragment_display_error_io_error) })

    var invalidUrl by remember { mutableStateOf<String?>(null) }
    viewModel.invalidUrlEvent.observeEvent(onEvent = { invalidUrl = it })

    EnterFeedUrlScreen(
        url = viewModel.urlTyped,
        errorMessage = stringResource(appR.string.error_invalid_http_url).takeIf { invalidUrl != null },
        onUrlChange = { viewModel.urlTyped = it.filterNot { it == '\n' } },
        onImeNext = { viewModel.submitUrl(viewModel.urlTyped) }
    )
}

@Composable
private fun <T> LiveData<Event<T>>.observeEvent(onEvent: (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(this, lifecycleOwner) {
        val observer = EventObserver<T> { onEvent(it) }
        observe(lifecycleOwner, observer)
        onDispose {
            removeObserver(observer)
        }
    }
}

@Composable
fun EnterFeedUrlScreen(
    url: String,
    errorMessage: String? = null,
    onUrlChange: (String) -> Unit,
    onImeNext: () -> Unit
) {
    Column(
        Modifier
            .padding(16.dp)
            .fillMaxSize()) {
        Text(stringResource(R.string.fragment_enter_feed_url_label))
        TextField(value = url, onValueChange = onUrlChange,
            label = {
                Text(stringResource(R.string.fragment_enter_feed_url_field_label))
            },
            isError = errorMessage != null,
            trailingIcon = {
                if (errorMessage != null) {
                    Icon(Icons.Default.Error, contentDescription = null)
                }
            },
            keyboardActions = KeyboardActions(onNext = { onImeNext() }),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        )
        if (errorMessage != null) {
            Text(errorMessage, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Preview
@Composable
private fun PreviewEnterFeedUrlScreen() {
    AppTheme3 {
        Surface {
            var url by remember { mutableStateOf("") }
            val errorMessage by remember {
                derivedStateOf {
                    if (!url.startsWith("http")) {
                        "Not an url"
                    } else null
                }
            }
            EnterFeedUrlScreen(url, errorMessage = errorMessage, onUrlChange = { url = it }, onImeNext = {})
        }
    }
}
