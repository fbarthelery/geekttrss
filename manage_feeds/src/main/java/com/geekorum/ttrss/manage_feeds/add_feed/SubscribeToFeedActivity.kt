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
package com.geekorum.ttrss.manage_feeds.add_feed

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.geekorum.ttrss.manage_feeds.BaseSessionActivity
import com.geekorum.ttrss.manage_feeds.R
import com.geekorum.ttrss.manage_feeds.databinding.ActivitySubscribeToFeedBinding


class SubscribeToFeedActivity : BaseSessionActivity() {

    private lateinit var binding: ActivitySubscribeToFeedBinding
    private val viewModel: SubscribeToFeedViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscribe_to_feed)
        navController = findNavController(R.id.nav_host_fragment)

        binding.cancel.setOnClickListener {
            if (!navController.popBackStack())
                finish()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.enter_feed_url -> {
                    binding.cancel.setText(R.string.activity_subscribe_feed_btn_cancel)
                    binding.next.setText(R.string.activity_subscribe_feed_btn_subscribe)
                    binding.next.setOnClickListener {
                        viewModel.submitUrl(viewModel.urlTyped)
                    }
                    viewModel.resetAvailableFeeds()
                }

                R.id.select_feed -> {
                    binding.cancel.setText(R.string.activity_subscribe_feed_btn_back)
                    binding.next.setText(R.string.activity_subscribe_feed_btn_subscribe)
                    binding.next.setOnClickListener {
                        if (viewModel.selectedFeed != null) {
                            viewModel.subscribeToFeed(viewModel.selectedFeed!!.toFeedInformation())
                            finish()
                        }
                    }
                }

                R.id.display_error -> {
                    binding.cancel.setText(R.string.activity_subscribe_feed_btn_back)
                    binding.next.setText(R.string.activity_subscribe_feed_btn_close)
                    binding.next.setOnClickListener {
                        finish()
                    }
                }
            }
        }
    }
}
