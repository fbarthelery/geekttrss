/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2020 by Frederic-Charles Barthelery.
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import com.geekorum.ttrss.manage_feeds.R
import com.geekorum.ttrss.manage_feeds.databinding.FragmentAddFeedSelectFeedBinding

class SelectFeedFragment : Fragment() {

    private lateinit var binding: FragmentAddFeedSelectFeedBinding
    private val viewModel: SubscribeToFeedViewModel by activityViewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAddFeedSelectFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val feedAdapter = FeedAdapter(requireContext())
        binding.availableFeeds.adapter = feedAdapter

        viewModel.feedsFound.observe(viewLifecycleOwner) {
            val text = it?.firstOrNull()?.title ?: getString(
                R.string.activity_add_feed_no_feeds_available)
            binding.availableFeedsSingle.text = text
            with(feedAdapter) {
                clear()
                addAll(it ?: emptyList())
            }
        }
    }
}
