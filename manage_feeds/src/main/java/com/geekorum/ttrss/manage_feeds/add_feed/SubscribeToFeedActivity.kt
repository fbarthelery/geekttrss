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
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.findNavController
import com.geekorum.ttrss.manage_feeds.BaseSessionActivity
import com.geekorum.ttrss.manage_feeds.R
import com.geekorum.ttrss.manage_feeds.databinding.ActivitySubscribeToFeedBinding
import com.geekorum.ttrss.ui.AppTheme


class SubscribeToFeedActivity : BaseSessionActivity() {

    private lateinit var binding: ActivitySubscribeToFeedBinding
    private val viewModel: SubscribeToFeedViewModel by viewModels()
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscribe_to_feed)

        binding.cancel.setOnClickListener {
            if (!navController.popBackStack())
                finish()
        }

        binding.composeNavHost.setContent {
            AppTheme {
                navController = rememberNavController()
                SubscribeToFeedNavHost(
                    viewModel = viewModel,
                    navController = navController,
                    finishActivity = {
                        finish()
                    }
                )

                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                    val destination = currentBackStackEntry?.destination
                SideEffect {
                    when (destination?.route) {
                        ROUTE_ENTER_FEED_URL -> {
                            binding.cancel.setText(R.string.activity_subscribe_feed_btn_cancel)
                            binding.next.setText(R.string.activity_subscribe_feed_btn_subscribe)
                            binding.next.setOnClickListener {
                                viewModel.submitUrl(viewModel.urlTyped)
                            }
                            viewModel.resetAvailableFeeds()
                        }

                        ROUTE_SELECT_FEED -> {
                            binding.cancel.setText(R.string.activity_subscribe_feed_btn_back)
                            binding.next.setText(R.string.activity_subscribe_feed_btn_subscribe)
                            binding.next.setOnClickListener {
                                if (viewModel.selectedFeed != null) {
                                    viewModel.subscribeToFeed(viewModel.selectedFeed!!.toFeedInformation())
                                    finish()
                                }
                            }
                        }

                        ROUTE_DISPLAY_ERROR -> {
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
    }
}
