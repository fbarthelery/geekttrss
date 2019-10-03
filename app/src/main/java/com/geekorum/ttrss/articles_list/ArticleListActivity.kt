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

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.ArticlesListDirections
import com.geekorum.ttrss.R
import com.geekorum.ttrss.article_details.ArticleDetailActivity
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.databinding.ActivityArticleListBinding
import com.geekorum.ttrss.in_app_update.InAppUpdateViewModel
import com.geekorum.ttrss.session.SessionActivity
import com.google.android.material.appbar.AppBarLayout
import timber.log.Timber

/**
 * An activity representing a list of Articles. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ArticleDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class ArticleListActivity : SessionActivity() {
    companion object {
        private const val FRAGMENT_FEEDS_LIST = "feeds_list"
        internal const val CODE_START_IN_APP_UPDATE = 1
    }

    private lateinit var binding: ActivityArticleListBinding
    private val drawerLayout: DrawerLayout
        get() = binding.headlinesDrawer


    private lateinit var navController: NavController
    private val activityViewModel: ActivityViewModel by viewModels()
    private val accountViewModel: TtrssAccountViewModel by viewModels()
    private val inAppUpdateViewModel: InAppUpdateViewModel by viewModels()
    private lateinit var inAppUpdatePresenter: InAppUpdatePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityViewModel.selectedFeed.observe(this) { bindFeedInformation(it) }
        activityViewModel.feedSelectedEvent.observe(this, EventObserver {
            navController.navigate(ArticlesListDirections.actionShowFeed(it.id, it.title))
            drawerLayout.closeDrawers()
        })

        activityViewModel.articleSelectedEvent.observe(this, EventObserver { (position, article) ->
            navController.navigate(ArticlesListFragmentDirections.actionShowArticle(article.id))
        })

        accountViewModel.selectedAccount.observe(this, Observer { account ->
            if (account != null) {
                activityViewModel.setAccount(account)
            } else {
                accountViewModel.startSelectAccountActivity(this)
            }
        })

        accountViewModel.noAccountSelectedEvent.observe(this, EventObserver {
            finish()
        })

        binding = DataBindingUtil.setContentView<ActivityArticleListBinding>(this,
            R.layout.activity_article_list).apply {
            lifecycleOwner = this@ArticleListActivity
            activityViewModel = this@ArticleListActivity.activityViewModel
        }

        inAppUpdatePresenter = InAppUpdatePresenter(binding.bannerContainer, this, inAppUpdateViewModel)

        navController = findNavController(R.id.middle_pane_layout).apply {
            addOnDestinationChangedListener { controller, destination, arguments ->
                when (destination.id) {
                    R.id.articlesListFragment -> {
                        binding.appBar.setExpanded(true)
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        binding.fab.show()
                    }
                    R.id.articlesSearchFragment -> {
                        binding.appBar.setExpanded(true)
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                        //TODO hide fab. but fab has scrollaware behavior that get it shown back when scrolling
                    }
                }
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace<FeedListFragment>(R.id.start_pane_layout, FRAGMENT_FEEDS_LIST)
            }
        }
        setupToolbar()
        setUpEdgeToEdge()
    }

    private fun setUpEdgeToEdge() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        // the appBar doesn't redraw statusBarForeground correctly. force it
        binding.appBar.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, _ ->
                binding.appBar.invalidate()
            })

        ViewCompat.setOnApplyWindowInsetsListener(binding.bannerContainer) { view, insets ->
            // consume top padding since we are not on top of screen
            val noTopInsets = insets.replaceSystemWindowInsets(insets.systemWindowInsetLeft,
                0,
                insets.systemWindowInsetRight,
                insets.systemWindowInsetBottom
            )
            WindowInsetsCompat.toWindowInsetsCompat(
                view.onApplyWindowInsets(noTopInsets.toWindowInsets())
            )
        }
    }

    private fun setupToolbar() {
        setupSearch()
        binding.toolbar.setupWithNavController(navController, drawerLayout)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("activity result $requestCode result $resultCode")
        super.onActivityResult(requestCode, resultCode, data)
        inAppUpdatePresenter.onActivityResult(requestCode, resultCode, data)
    }

    private fun setupSearch() {
        val searchItem = with(binding.toolbar) {
            inflateMenu(R.menu.activity_articles_list)
            menu.findItem(R.id.articles_search)
        }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.articlesListFragment -> if (searchItem.isActionViewExpanded) searchItem.collapseActionView()
            }
        }

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                navController.navigate(ArticlesListFragmentDirections.actionSearchArticle())
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                if (navController.currentDestination?.id == R.id.articlesSearchFragment)
                    navController.popBackStack()
                return true
            }
        })

        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                activityViewModel.setSearchQuery(query)
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                activityViewModel.setSearchQuery(query)
                return true
            }
        })
    }

    private fun bindFeedInformation(feed: Feed?) {
        title = feed?.title ?: ""
    }

}
