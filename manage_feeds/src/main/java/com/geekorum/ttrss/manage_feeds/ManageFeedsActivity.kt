/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2019 by Frederic-Charles Barthelery.
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
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.geekdroid.dagger.DaggerDelegateSavedStateVMFactory
import com.geekorum.ttrss.BaseDialogFragment
import com.geekorum.ttrss.BaseFragment
import com.geekorum.ttrss.activityViewModels
import com.geekorum.ttrss.applicationComponent
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.manage_feeds.databinding.ActivityManageFeedsBinding
import com.geekorum.ttrss.manage_feeds.databinding.DialogUnsubscribeFeedBinding
import com.geekorum.ttrss.manage_feeds.databinding.FragmentManageFeedsBinding
import com.geekorum.ttrss.manage_feeds.databinding.ItemFeedBinding
import com.geekorum.ttrss.session.SessionActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import javax.inject.Inject

class ManageFeedsActivity : SessionActivity() {
    private lateinit var binding: ActivityManageFeedsBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_feeds)
        navController = findNavController(R.id.nav_host_fragment)

        binding.fab.setOnClickListener {
            startSubscribeToFeed()
        }
    }

    override fun inject() {
        val manageFeedComponent = DaggerManageFeedComponent.builder()
            .manageFeedsDependencies(applicationComponent)
            .build()
        manageFeedComponent.activityInjector.inject(this)
    }

    private fun startSubscribeToFeed() {
        val direction = ManageFeedsFragmentDirections.actionSubscribeToFeed()
        navController.navigate(direction)
    }

}

class ManageFeedsFragment @Inject constructor(
    savedStateVmFactoryCreator: DaggerDelegateSavedStateVMFactory.Creator
) : BaseFragment(savedStateVmFactoryCreator) {

    private lateinit var binding: FragmentManageFeedsBinding
    private val viewModel: ManageFeedViewModel by viewModels()
    private val adapter = FeedsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManageFeedsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.adapter = adapter

        viewModel.feeds.observe(this) {
            adapter.submitList(it)
        }

        viewModel.feedClickedEvent.observe(this, EventObserver {
            showConfirmationDialog(it)
        })
    }

    private fun showConfirmationDialog(feed: Feed) {
        val direction = ManageFeedsFragmentDirections.actionConfirmUnsubscribe(
            feed.id, feed.title, feed.url)
        findNavController().navigate(direction)
    }


    private inner class FeedsAdapter : PagedListAdapter<Feed, FeedViewHolder>(DiffFeed) {
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

    class FeedViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setFeed(feed: Feed?) {
            val title = feed?.displayTitle?.takeIf { it.isNotEmpty() } ?: feed?.title
            binding.setVariable(BR.name, title)
            binding.setVariable(BR.feed, feed)
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

class ConfirmUnsubscribeFragment @Inject constructor(
    savedStateVmFactoryCreator: DaggerDelegateSavedStateVMFactory.Creator
) : BaseDialogFragment(savedStateVmFactoryCreator) {

    private val viewModel: ManageFeedViewModel by activityViewModels()
    private val args:ConfirmUnsubscribeFragmentArgs by navArgs()

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
