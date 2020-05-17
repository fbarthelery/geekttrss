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
package com.geekorum.ttrss.articles_list

import android.view.View
import androidx.core.view.plusAssign
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.dynamicfeatures.DynamicGraphNavigator
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Feed.Companion.FEED_ID_ALL_ARTICLES
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


/**
 * Controls the behavior of the AppBar
 */
internal class AppBarPresenter(
    private val appBarLayout: AppBarLayout,
    private val toolbar: MaterialToolbar,
    private val tagsList: View,
    private val tagsGroup: ChipGroup,
    private val lifecycleOwner: LifecycleOwner,
    private val tagsViewModel: TagsViewModel,
    private val navController: NavController
){

    init {
        setup()
        observeTags()
    }

    private fun setup() {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            val progressDestinationId = (controller.graph as? DynamicGraphNavigator.DynamicNavGraph)?.progressDestination ?: 0
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
            tagsList.visibility = tagsVisibility
        }
    }

    private fun observeTags() {
        tagsViewModel.tags.observe(lifecycleOwner) { tags ->
            tagsGroup.removeAllViews()
            for (tag in tags) {
                val chip = Chip(tagsGroup.context).apply {
                    text = tag
                    setOnClickListener {
                        val showTag = ArticlesListFragmentDirections.actionShowTag(tag)
                        navController.navigate(showTag)
                    }
                }
                tagsGroup += chip
            }
        }
    }
}
