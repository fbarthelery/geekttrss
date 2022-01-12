/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.geekdroid.views.doOnApplyWindowInsets
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.manage_feeds.databinding.ActivityManageFeedsBinding
import com.geekorum.ttrss.manage_feeds.databinding.DialogUnsubscribeFeedBinding
import com.geekorum.ttrss.manage_feeds.databinding.FragmentManageFeedsBinding
import com.geekorum.ttrss.manage_feeds.databinding.ItemFeedBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

    private lateinit var binding: FragmentManageFeedsBinding
    private val viewModel: ManageFeedViewModel by activityViewModels()
    private val adapter = FeedsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManageFeedsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.feeds.collectLatest {
                adapter.submitData(it)
            }
        }

        viewModel.feedClickedEvent.observe(viewLifecycleOwner, EventObserver {
            showConfirmationDialog(it)
        })
        setupEdgeToEdge()
    }

    private fun setupEdgeToEdge() {
        binding.recyclerView.doOnApplyWindowInsets { view, windowInsets, padding ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = padding.bottom + insets.bottom)
            windowInsets
        }
    }

    private fun showConfirmationDialog(feed: Feed) {
        val direction = ManageFeedsFragmentDirections.actionConfirmUnsubscribe(
            feed.id, feed.title, feed.url)
        findNavController().navigate(direction)
    }


    private inner class FeedsAdapter : PagingDataAdapter<Feed, FeedViewHolder>(DiffFeed) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
            val itemFeedBinding = ItemFeedBinding.inflate(layoutInflater, parent, false)
            return FeedViewHolder(itemFeedBinding)
        }

        override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
            val feed = getItem(position)
            checkNotNull(feed)
            holder.setFeed(feed)
            holder.setViewModel(viewModel)
            holder.binding.executePendingBindings()
        }
    }

    class FeedViewHolder(val binding: ItemFeedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setFeed(feed: Feed?) {
            val title = feed?.displayTitle?.takeIf { it.isNotEmpty() } ?: feed?.title
            binding.setVariable(BR.name, title)
            binding.setVariable(BR.feed, feed)
            with(binding.feedIcon) {
                // set the tint for errors and place holder drawable
                imageTintList = resources.getColorStateList(R.color.rss_feed_orange, null)
                load(feed?.feedIconUrl) {
                    error(R.drawable.ic_rss_feed_black_24dp)
                    placeholder(R.drawable.ic_rss_feed_black_24dp)
                    listener { _, _ ->
                        binding.feedIcon.imageTintList = null
                    }
                }
            }
        }

        fun setViewModel(viewModel: ManageFeedViewModel) {
            binding.setVariable(BR.viewModel, viewModel)
        }
    }

    private object DiffFeed : DiffUtil.ItemCallback<Feed>() {
        override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }
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
