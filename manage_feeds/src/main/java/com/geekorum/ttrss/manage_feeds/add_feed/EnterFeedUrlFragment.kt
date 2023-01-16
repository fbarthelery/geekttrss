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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.manage_feeds.R
import com.geekorum.ttrss.manage_feeds.databinding.FragmentAddFeedEnterUrlBinding

class EnterFeedUrlFragment : Fragment() {

    private lateinit var binding: FragmentAddFeedEnterUrlBinding
    private val viewModel: SubscribeToFeedViewModel by activityViewModels()
    private val navController by lazy { findNavController() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddFeedEnterUrlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.invalidUrlEvent.observe(viewLifecycleOwner, EventObserver {
            binding.feedUrl.error = "Invalid url"
        })

        viewModel.feedsFound.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            when {
                it.isEmpty() -> {
                    val action = EnterFeedUrlFragmentDirections.actionDisplayError(
                        R.string.fragment_display_error_no_feeds_found)
                    navController.navigate(action)
                }

                it.size == 1 -> {
                    val info = it.single()
                    if (info.source == FeedsFinder.Source.URL) {
                        viewModel.subscribeToFeed(info.toFeedInformation())
                        requireActivity().finish()
                    } else {
                        navController.navigate(EnterFeedUrlFragmentDirections.actionShowAvailableFeeds())
                    }
                }

                it.size > 1 -> navController.navigate(EnterFeedUrlFragmentDirections.actionShowAvailableFeeds())
            }
        }

        viewModel.ioErrorEvent.observe(viewLifecycleOwner, EventObserver {
            val action = EnterFeedUrlFragmentDirections.actionDisplayError(
                R.string.fragment_display_error_io_error)
            navController.navigate(action)
        })
    }
}
