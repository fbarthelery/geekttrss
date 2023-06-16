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
package com.geekorum.ttrss.manage_feeds

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.rememberAsyncImagePainter
import com.geekorum.geekdroid.views.doOnApplyWindowInsets
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.FeedWithFavIcon
import com.geekorum.ttrss.manage_feeds.databinding.ActivityManageFeedsBinding
import com.geekorum.ttrss.manage_feeds.databinding.DialogUnsubscribeFeedBinding
import com.geekorum.ttrss.ui.AppTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import com.geekorum.ttrss.R as appR

class ManageFeedsActivity : BaseSessionActivity() {
    private lateinit var binding: ActivityManageFeedsBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_feeds)
        navController = findNavController(R.id.nav_host_fragment)

        binding.fab.setOnClickListener {
            startSubscribeToFeed()
        }
        setupEdgeToEdge()
    }

    private fun startSubscribeToFeed() {
        val direction = ManageFeedsFragmentDirections.actionSubscribeToFeed()
        navController.navigate(direction)
    }

    private fun setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)


        // CollapsingToolbar consumes the insets by default.
        // override it to not consume them so that they can be dispathed to the recycler view
        binding.collapsingToolbar.doOnApplyWindowInsets { _, insets, _ ->
            insets
        }
    }
}

class ManageFeedsFragment : Fragment() {

    private val viewModel: ManageFeedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    ManageFeedsListScreen(viewModel, onFeedClick = {
                        showConfirmationDialog(it.feed)
                    })
                }
            }
        }
    }

    private fun showConfirmationDialog(feed: Feed) {
        val direction = ManageFeedsFragmentDirections.actionConfirmUnsubscribe(
            feed.id, feed.title, feed.url)
        findNavController().navigate(direction)
    }
}

@Composable
fun ManageFeedsListScreen(
    viewModel: ManageFeedViewModel = viewModel(),
    onFeedClick: (FeedWithFavIcon) -> Unit
) {
    val contentPadding = PaddingValues(
        top = 8.dp,
        bottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding(),
    )

    ManageFeedsListScreen(
        feedsData = viewModel.feeds,
        onFeedClick = onFeedClick,
        contentPadding = contentPadding
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ManageFeedsListScreen(
    feedsData: Flow<PagingData<FeedWithFavIcon>>,
    onFeedClick: (FeedWithFavIcon) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Surface(Modifier.fillMaxSize()) {
        val feedsPagingItem = feedsData.collectAsLazyPagingItems()
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        LazyColumn(
            modifier = Modifier.nestedScroll(nestedScrollInterop),
            contentPadding = contentPadding
        ) {
            items(feedsPagingItem.itemCount,
                key = feedsPagingItem.itemKey { it.feed.id },
                contentType = feedsPagingItem.itemContentType { "Feed" }) { idx ->
                val feedItem = feedsPagingItem[idx]
                ListItem(
                    modifier = Modifier.clickable {
                        if (feedItem != null)
                            onFeedClick(feedItem)
                    },
                    icon = {
                        val feedIconPainter = rememberAsyncImagePainter(
                            model = feedItem?.favIcon?.url,
                            placeholder = painterResource(appR.drawable.ic_rss_feed_orange),
                            fallback = painterResource(appR.drawable.ic_rss_feed_orange),
                            error = painterResource(appR.drawable.ic_rss_feed_orange),
                        )
                        Image(
                            painter = feedIconPainter,
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.size(40.dp)
                        )
                    }) {
                    Text(feedItem?.feed?.title ?: "", overflow = TextOverflow.Ellipsis, maxLines = 1)
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewManageFeedsListScreen() {
    AppTheme {
        val feeds = PagingData.from(
            data = listOf(
                FeedWithFavIcon(
                    feed = Feed(
                        id = 2,
                        title = "Frandroid",
                        unreadCount = 42,
                    ),
                    favIcon = null
                ),
                FeedWithFavIcon(
                    feed = Feed(
                        id = 3,
                        title = "Gentoo universe",
                        unreadCount = 10,
                    ),
                    favIcon = null
                ),
                FeedWithFavIcon(
                    feed = Feed(
                        id = 4,
                        title = "LinuxFr",
                        unreadCount = 8,
                    ),
                    favIcon = null
                ),
            )
        )
        val pagingFlow = MutableStateFlow(feeds)
        ManageFeedsListScreen(pagingFlow, onFeedClick = {})
    }
}


class ConfirmUnsubscribeFragment : DialogFragment() {

    private val viewModel: ManageFeedViewModel by activityViewModels()
    private val args: ConfirmUnsubscribeFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater

        val binding = DialogUnsubscribeFeedBinding.inflate(inflater, null, false)
        with(args) {
            binding.title = feedTitle
            binding.url = feedUrl
        }
        val feedId = args.feedId
        return MaterialAlertDialogBuilder(requireActivity())
            .setView(binding.root)
            .setTitle(R.string.fragment_confirmation_title)
            .setPositiveButton(R.string.btn_confirm) { _, _ ->
                viewModel.unsubscribeFeed(feedId)
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()
    }
}
