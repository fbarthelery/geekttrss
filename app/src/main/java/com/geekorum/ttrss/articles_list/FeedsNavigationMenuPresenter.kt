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
package com.geekorum.ttrss.articles_list

import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.observe
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.databinding.MenuFeedActionViewBinding
import com.google.android.material.navigation.NavigationView

private const val MENU_GROUP_ID_SPECIAL = 1


/**
 * Display the feeds in a NavigationView menu.
 */
class FeedsNavigationMenuPresenter(
    private val lifeCycleOwner: LifecycleOwner,
    view: NavigationView,
    private val feedsViewModel: FeedsViewModel,
    private val activityViewModel: ActivityViewModel,
    private val menu: Menu
) {

    private val layoutInflater = LayoutInflater.from(view.context)

    init {
        observeViewModels()
    }

    fun onFeedSelected(item: MenuItem): Boolean {
        item.feed?.let {
            activityViewModel.setSelectedFeed(it)
            return true
        }
        return false
    }

    private fun observeViewModels() {
        feedsViewModel.feeds.observe(lifeCycleOwner) { feeds ->
            transformFeedViewsInMenuEntry(menu, feeds)
        }
    }

    private fun transformFeedViewsInMenuEntry(menu: Menu, feeds: List<FeedsViewModel.FeedView>) {
        menu.clear()
        feeds.forEach { (feed, isSelected) ->
            val title = if (feed.displayTitle.isEmpty()) feed.title else feed.displayTitle
            val feedId = feed.id.toInt()
            val menuItem = if (feedId < 0) {
                menu.add(Menu.NONE, feedId, 0, title)
            } else {
                menu.add(MENU_GROUP_ID_SPECIAL, feedId, 0, title)
            }
            setMenuItemIcon(feed, menuItem)
            setMenuItemUnreadCount(feed, menuItem)
            menuItem.isCheckable = true
            menuItem.isChecked = isSelected
            menuItem.feed = feed
        }
    }

    private fun setMenuItemUnreadCount(feed: Feed, menuItem: MenuItem) {
        val menuView = MenuFeedActionViewBinding.inflate(layoutInflater,
            null, false).apply {
            unreadCounter.text = feed.unreadCount.toString()
            unreadCounter.visibility = if (feed.unreadCount > 0) View.VISIBLE else View.INVISIBLE
        }
        menuItem.actionView = menuView.root
    }

    private fun setMenuItemIcon(feed: Feed, menuItem: MenuItem) {
        val iconRes = when {
            feed.isArchivedFeed -> R.drawable.ic_archive
            feed.isStarredFeed -> R.drawable.ic_star
            feed.isPublishedFeed -> R.drawable.ic_checkbox_marked
            feed.isFreshFeed -> R.drawable.ic_coffee
            feed.isAllArticlesFeed -> R.drawable.ic_folder_outline
            else -> R.drawable.ic_rss_box
        }
        menuItem.setIcon(iconRes)
    }

    private var MenuItem.feed: Feed?
        get() = actionView?.tag as? Feed
        set(value) { actionView.tag = value }

}
