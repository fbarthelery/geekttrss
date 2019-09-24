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

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.view.get
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import com.geekorum.geekdroid.dagger.DaggerDelegateSavedStateVMFactory
import com.geekorum.ttrss.BaseFragment
import com.geekorum.ttrss.Features
import com.geekorum.ttrss.R
import com.geekorum.ttrss.activityViewModels
import com.geekorum.ttrss.data.Category
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.databinding.FragmentFeedsBinding
import com.geekorum.ttrss.databinding.MenuFeedActionViewBinding
import com.geekorum.ttrss.doOnApplyWindowInsets
import com.geekorum.ttrss.on_demand_modules.OnDemandModuleManager
import com.geekorum.ttrss.settings.SettingsActivity
import com.geekorum.ttrss.settings.manage_features.InstallFeatureActivity
import com.google.android.material.navigation.NavigationView
import timber.log.Timber
import javax.inject.Inject

/**
 * Display the list of feeds.
 */
class FeedListFragment @Inject
constructor(
    savedStateVmFactoryCreator: DaggerDelegateSavedStateVMFactory.Creator,
    private val moduleManager: OnDemandModuleManager,
    private val preferences: SharedPreferences
) : BaseFragment(savedStateVmFactoryCreator), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: FragmentFeedsBinding
    private var currentFeeds: List<Feed>? = null
    private var categoriesDisplayed: Boolean = false
    private val feedsViewModel: FeedsViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private var feedsForCategory: LiveData<List<Feed>>? = null
    private val accountViewModel: TtrssAccountViewModel by activityViewModels()

    private val isManageFeedInstalled: Boolean
        get() = moduleManager.installedModules.contains(Features.MANAGE_FEEDS)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFeedsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.navigationView.setNavigationItemSelectedListener(this)
        setUpEdgeToEdge()
        setupViewModels()
    }

    private fun setUpEdgeToEdge() {
        val innerView = binding.navigationView[0]
        val innerViewInitialPaddingBottom = innerView.paddingBottom
        binding.navigationView.doOnApplyWindowInsets { _, insets, _ ->
            innerView.updatePadding(
                bottom = innerViewInitialPaddingBottom + insets.systemWindowInsetBottom)
            insets
        }
    }

    private fun setupViewModels() {
        val showUnreadOnly = preferences.getBoolean("show_unread_only", true)
        feedsViewModel.setOnlyUnread(showUnreadOnly)

        categoriesDisplayed = preferences.getBoolean("enable_cats", false)
        //        categoriesDisplayed = true;
        if (categoriesDisplayed) {
            feedsViewModel.categories.observe(viewLifecycleOwner) { categories ->
                transformCategoriesInMenuEntry(binding.navigationView.menu, categories)
                binding.navigationView.inflateMenu(R.menu.fragment_feed_list)
            }
        } else {
            feedsViewModel.allFeeds.observe(viewLifecycleOwner) { feeds ->
                currentFeeds = feeds
                transformFeedsInMenuEntry(binding.navigationView.menu, feeds)
                binding.navigationView.inflateMenu(R.menu.fragment_feed_list)
            }
        }
        activityViewModel.selectedFeed.observe(viewLifecycleOwner) { feed ->
            val menu = binding.navigationView.menu
            val item = feed?.let { menu.findItem(it.id.toInt()) }
            item?.isChecked = true
        }

        accountViewModel.selectedAccount.observe(viewLifecycleOwner) { account ->
            val headerView = binding.navigationView.getHeaderView(0)
            val login = headerView.findViewById<TextView>(R.id.drawer_header_login)
            login.text = account.name
        }

        accountViewModel.selectedAccountHost.observe(viewLifecycleOwner) { host ->
            val headerView = binding.navigationView.getHeaderView(0)
            val server = headerView.findViewById<TextView>(R.id.drawer_header_server)
            server.text = host
        }
    }

    private fun navigateToSettings() {
        val intent = Intent(requireActivity(), SettingsActivity::class.java)
        ActivityCompat.startActivity(requireActivity(), intent, null)
    }

    private fun transformFeedsInMenuEntry(menu: Menu, feeds: List<Feed>) {
        menu.clear()
        val currentFeed = activityViewModel.selectedFeed.value
        feeds.forEach {
            val title = if (it.displayTitle.isEmpty()) it.title else it.displayTitle
            val feedId = it.id.toInt()
            val menuItem = if (feedId < 0) {
                menu.add(Menu.NONE, feedId, 0, title)
            } else {
                menu.add(MENU_GROUP_ID_SPECIAL, feedId, 0, title)
            }
            setMenuItemIcon(it, menuItem)
            setMenuItemUnreadCount(it, menuItem)
            menuItem.isCheckable = true
            menuItem.isChecked = currentFeed?.id == it.id
        }
        categoriesDisplayed = false
    }

    private fun transformCategoriesInMenuEntry(menu: Menu, categories: List<Category>) {
        menu.clear()
        categories.forEach {
            val menuItem = menu.add(Menu.NONE, it.id.toInt(), Menu.NONE, it.title)
            setMenuItemIcon(it, menuItem)
            setMenuItemUnreadCount(it, menuItem)
            menuItem.isCheckable = true
        }
        categoriesDisplayed = true
    }

    private fun setMenuItemUnreadCount(feed: Feed, menuItem: MenuItem) {
        val layoutInflater = LayoutInflater.from(context)
        val menuView = MenuFeedActionViewBinding.inflate(layoutInflater,
            null, false).apply {
            unreadCounter.text = feed.unreadCount.toString()
            unreadCounter.visibility = if (feed.unreadCount > 0) View.VISIBLE else View.INVISIBLE
        }
        menuItem.actionView = menuView.root
    }

    private fun setMenuItemUnreadCount(category: Category, menuItem: MenuItem) {
        //TODO
        val layoutInflater = LayoutInflater.from(context)
        val menuView = MenuFeedActionViewBinding.inflate(layoutInflater, null, false).apply {
            unreadCounter.text = category.unreadCount.toString()
            unreadCounter.visibility = if (category.unreadCount > 0) View.VISIBLE else View.INVISIBLE
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

    private fun setMenuItemIcon(category: Category, menuItem: MenuItem) {
        menuItem.setIcon(R.drawable.ic_folder_outline)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when {
            item.itemId == R.id.manage_feeds -> {
                installOrStartManageFeed()
                true
            }
            categoriesDisplayed -> onCategoriesSelected(item)
            item.itemId == R.id.settings -> {
                navigateToSettings()
                true
            }
            else -> onFeedSelected(item)
        }
    }

    private fun onFeedSelected(item: MenuItem): Boolean {
        val browseCatsLikeFeeds = preferences.getBoolean("browse_cats_like_feeds", false)

        // find feed
        val feedFound = currentFeeds?.find { it.id == item.itemId.toLong() }
        feedFound?.let {
            activityViewModel.setSelectedFeed(it)
            return true
        }
        return false
    }

    private fun displayFeedCategory(category: Category) {
        categoriesDisplayed = false

        feedsViewModel.setSelectedCategory(category.id)
        if (feedsForCategory == null) {
            feedsForCategory = feedsViewModel.feedsForCategory
            feedsForCategory!!.observe(viewLifecycleOwner) { feeds ->
                currentFeeds = feeds
                transformFeedsInMenuEntry(binding.navigationView.menu, feeds)
            }
        }
    }

    private fun onCategoriesSelected(item: MenuItem): Boolean {
        val browseCatsLikeFeeds = preferences.getBoolean("browse_cats_like_feeds", false)

        // find categories
        //TODO
        val category = feedsViewModel.categories.value?.find { it.id == item.itemId.toLong() }
        category?.let {
            if (browseCatsLikeFeeds && false) { //TODO make this work but for now just disable this option
                val feed = Feed(id = it.id, title = it.title)
                // TODO is cat = true);
                // activityViewModel.setSelectedFeed(feed);
            } else {
                displayFeedCategory(category)
            }
            return true
        }
        return false
    }

    private fun installOrStartManageFeed() {
        val context = requireContext()
        if (isManageFeedInstalled) {
            try {
                val freshContext = context.createPackageContext(context.packageName, 0)
                val intent = Intent().apply {
                    component = ComponentName.createRelative(freshContext,
                        "com.geekorum.ttrss.manage_feeds.ManageFeedsActivity")
                }
                startActivity(intent)
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.wtf(e, "Unable to create our package context")
            }
        } else {
            val intent = Intent(context, InstallFeatureActivity::class.java).apply {
                putExtra(InstallFeatureActivity.EXTRA_FEATURES_LIST,
                    arrayOf(Features.MANAGE_FEEDS))
            }
            startActivityForResult(intent, CODE_INSTALL_MANAGE_FEED)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CODE_INSTALL_MANAGE_FEED) {
            if (resultCode == Activity.RESULT_OK) {
                installOrStartManageFeed()
            }
        }
    }

    companion object {
        private const val MENU_GROUP_ID_SPECIAL = 1
        private const val CODE_INSTALL_MANAGE_FEED = 1
    }

}
