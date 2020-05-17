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

import android.view.LayoutInflater
import android.view.View
import androidx.core.view.children
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

    private var tagsIds: Map<Int, String>? = null
    private var currentTag: String? = null

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
            if (destination.id == R.id.articlesListByTagFragment) {
                currentTag = arguments?.getString("tag")
            } else {
                currentTag = null
                tagsGroup.clearCheck()
            }
        }
    }

    private fun observeTags() {
        tagsViewModel.tags.observe(lifecycleOwner) { tags ->
            tagsIds = tags.map { View.generateViewId() to it }.toMap()
            transformTagsToChips(tags)
        }
        tagsGroup.setOnCheckedChangeListener { _, checkedId ->
            val tag = tagsIds?.get(checkedId)
            if (tag == null) {
                if (navController.currentDestination?.id == R.id.articlesListByTagFragment) {
                    navController.popBackStack()
                }
            } else {
                val showTag = ArticlesListFragmentDirections.actionShowTag(tag)
                navController.navigate(showTag)
            }
        }
    }

    private fun transformTagsToChips(tags: List<String>) {
        tagsGroup.removeAllViews()
        val layoutInflater = LayoutInflater.from(tagsGroup.context)
        for ((id, tag) in tagsIds!!) {
            layoutInflater.inflate(R.layout.chip_tag, tagsGroup)
            val chip = tagsGroup.children.last() as Chip
            chip.apply {
                setId(id)
                text = tag
                setTag(tag)
            }
        }
        checkCurrentTagChip()
    }

    private fun checkCurrentTagChip() {
        val checkedId = currentTag?.let {
            tagsGroup.findViewWithTag<View>(it)?.id
        } ?: View.NO_ID
        tagsGroup.check(checkedId)
    }
}
