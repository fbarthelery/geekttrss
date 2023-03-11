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
package com.geekorum.ttrss.articles_list

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavController
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Feed.Companion.FEED_ID_ALL_ARTICLES
import com.geekorum.ttrss.on_demand_modules.OnDemandModuleNavHostProgressDestinationProvider
import com.geekorum.ttrss.ui.AppTheme
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar


/**
 * Controls the behavior of the AppBar
 */
internal class AppBarPresenter(
    private val appBarLayout: AppBarLayout,
    private val toolbar: MaterialToolbar,
    private val tagsListCompose: ComposeView,
    private val tagsViewModel: TagsViewModel,
    private val onDemandModuleNavHostProgressDestinationProvider: OnDemandModuleNavHostProgressDestinationProvider,
    private val navController: NavController,
){

    private var currentTag: String? by mutableStateOf(null)

    init {
        setup()
        setupTagsListBar()
    }

    private fun setup() {
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            val progressDestinationId = onDemandModuleNavHostProgressDestinationProvider.progressDestinationId
            when (destination.id) {
                R.id.articlesListFragment,
                R.id.articlesListByTagFragment,
                R.id.articlesSearchFragment -> appBarLayout.setExpanded(true)

                progressDestinationId -> {
                    appBarLayout.setExpanded(true)
                    toolbar.title = toolbar.resources.getString(R.string.lbl_install_feature_title)
                }
            }
            val tagsVisibility = when (destination.id) {
                R.id.articlesListFragment,
                R.id.articlesListByTagFragment -> {
                    val feedId = arguments?.let { ArticlesListFragmentArgs.fromBundle(it) }?.feedId ?: FEED_ID_ALL_ARTICLES
                    if (feedId == FEED_ID_ALL_ARTICLES) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
                else -> View.GONE
            }
            tagsListCompose.visibility = tagsVisibility
            currentTag = if (destination.id == R.id.articlesListByTagFragment) {
                arguments?.getString("tag")
            } else {
                null
            }
        }
    }

    private fun setupTagsListBar() {
        tagsListCompose.setContent {
            Content()
        }
    }

    @Composable
    fun Content() {
        AppTheme {
            val tags by tagsViewModel.tags.observeAsState()
            val tagsSet = tags?.toSet() ?: emptySet()
            if (tagsSet.isNotEmpty()) {
                TagsListBar(tags = tagsSet,
                    selectedTag = currentTag,
                    selectedTagChange = { tag ->
                        currentTag = tag
                        if (tag == null) {
                            if (navController.currentDestination?.id == R.id.articlesListByTagFragment) {
                                navController.popBackStack()
                            }
                        } else {
                            val showTag = ArticlesListFragmentDirections.actionShowTag(tag)
                            navController.navigate(showTag)
                        }
                    }
                )
            }
        }
    }

}
