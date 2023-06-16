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
package com.geekorum.ttrss.manage_feeds.add_feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.geekorum.geekdroid.app.lifecycle.Event
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.manage_feeds.R
import com.geekorum.ttrss.ui.AppTheme
import com.geekorum.ttrss.R as appR

class EnterFeedUrlFragment : Fragment() {

    private val viewModel: SubscribeToFeedViewModel by activityViewModels()
    private val navController by lazy { findNavController() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    EnterFeedUrlScreen(
                        viewModel,
                        navigateToDisplayError = {
                            val action = EnterFeedUrlFragmentDirections.actionDisplayError(
                                R.string.fragment_display_error_no_feeds_found
                            )
                            navController.navigate(action)
                        },
                        navigateToShowAvailableFeeds = {
                            navController.navigate(EnterFeedUrlFragmentDirections.actionShowAvailableFeeds())
                        },
                        finishActivity = {
                            requireActivity().finish()
                        }
                    )
                }
            }
        }
    }
}

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
        onUrlChange = { viewModel.urlTyped = it }
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
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        )
        if (errorMessage != null) {
            Text(errorMessage, style = MaterialTheme.typography.caption, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Preview
@Composable
private fun PreviewEnterFeedUrlScreen() {
    AppTheme {
        Surface {
            var url by remember { mutableStateOf("") }
            val errorMessage by remember {
                derivedStateOf {
                    if (!url.startsWith("http")) {
                        "Not an url"
                    } else null
                }
            }
            EnterFeedUrlScreen(url, errorMessage = errorMessage, onUrlChange = { url = it })
        }
    }
}