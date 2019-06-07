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

import android.os.Bundle
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.observe
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.geekorum.ttrss.applicationComponent
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.manage_feeds.databinding.ActivityManageFeedsBinding
import com.geekorum.ttrss.manage_feeds.databinding.ItemFeedBinding
import com.geekorum.ttrss.session.SessionActivity
import com.geekorum.ttrss.viewModels

class ManageFeedsActivity : SessionActivity() {
    private val viewModel: ManageFeedViewModel by viewModels()
    private lateinit var binding: ActivityManageFeedsBinding
    private val adapter = FeedsAdapter()

    private val moduleComponent by lazy {
        DaggerManageFeedComponent.builder()
            .manageFeedsDependencies(applicationComponent)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_feeds)
        binding.recyclerView.adapter = adapter
        viewModel.feeds.observe(this) {
            adapter.submitList(it)
        }
    }

    override fun inject() {
        moduleComponent.inject(this)
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
            holder.binding.executePendingBindings()
        }
    }

    class FeedViewHolder(val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root) {
        fun setFeed(feed: Feed?) {
            val title = feed?.displayTitle?.takeIf { it.isNotEmpty() } ?: feed?.title
            binding.setVariable(BR.name, title)
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
