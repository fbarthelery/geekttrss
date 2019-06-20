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
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.observe
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.geekdroid.dagger.DaggerDelegateFragmentFactory
import com.geekorum.geekdroid.dagger.DaggerDelegateViewModelsFactory
import com.geekorum.ttrss.BaseDialogFragment
import com.geekorum.ttrss.activityViewModels
import com.geekorum.ttrss.applicationComponent
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.manage_feeds.databinding.ActivityManageFeedsBinding
import com.geekorum.ttrss.manage_feeds.databinding.DialogUnsubscribeFeedBinding
import com.geekorum.ttrss.manage_feeds.databinding.ItemFeedBinding
import com.geekorum.ttrss.session.SessionActivity
import com.geekorum.ttrss.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import javax.inject.Inject

class ManageFeedsActivity : SessionActivity() {
    private val viewModel: ManageFeedViewModel by viewModels()
    private lateinit var binding: ActivityManageFeedsBinding
    private val adapter = FeedsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_feeds)
        binding.recyclerView.adapter = adapter
        viewModel.feeds.observe(this) {
            adapter.submitList(it)
        }

        viewModel.feedClickedEvent.observe(this, EventObserver {
            showConfirmationDialog(it)
        })
    }

    override fun inject() {
        val manageFeedComponent = DaggerManageFeedComponent.builder()
            .manageFeedsDependencies(applicationComponent)
            .build()
        manageFeedComponent.activityInjector.inject(this)
    }

    private fun showConfirmationDialog(feed: Feed) {
        val confirmationFragment = ConfirmationFragment.newInstance(supportFragmentManager.fragmentFactory, feed)
        confirmationFragment.show(supportFragmentManager, null)
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

    class FeedViewHolder(val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {
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

    class ConfirmationFragment @Inject constructor(
        viewModelsFactory: DaggerDelegateViewModelsFactory,
        fragmentFactory: DaggerDelegateFragmentFactory
    ) : BaseDialogFragment(viewModelsFactory, fragmentFactory) {

        private val viewModel: ManageFeedViewModel by activityViewModels()

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val inflater =  requireActivity().layoutInflater

            val binding = DialogUnsubscribeFeedBinding.inflate(inflater, null, false)
            val arguments = requireArguments()
            with(arguments) {
                binding.title = getString(ARG_FEED_TITLE)
                binding.url = getString(ARG_FEED_URL)
            }
            val feedId = arguments.getLong(ARG_FEED_ID)
            return MaterialAlertDialogBuilder(requireActivity())
                .setView(binding.root)
                .setTitle(R.string.fragment_confirmation_title)
                .setPositiveButton(R.string.btn_confirm) { _, _ ->
                    viewModel.unsubscribeFeed(feedId)
                }
                .setNegativeButton(R.string.btn_cancel, null)
                .create()
        }

        companion object {
            const val ARG_FEED_ID = "feed_id"
            const val ARG_FEED_TITLE = "feed_title"
            const val ARG_FEED_URL = "feed_url"

            @JvmStatic
            fun newInstance(fragmentFactory: FragmentFactory, feed: Feed): ConfirmationFragment {
                return fragmentFactory.instantiate(ConfirmationFragment::class.java.classLoader!!,
                    ConfirmationFragment::class.java.name).apply {
                    arguments = bundleOf(
                        ARG_FEED_ID to feed.id,
                        ARG_FEED_TITLE to feed.title,
                        ARG_FEED_URL to feed.url)
                } as ConfirmationFragment
            }
        }
    }

}
