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
import android.content.ContentUris
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.R
import com.geekorum.ttrss.article_details.ArticleDetailActivity
import com.geekorum.ttrss.articles_list.search.ArticlesSearchFragment
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.databinding.ActivityArticleListBinding
import com.geekorum.ttrss.in_app_update.InAppUpdateViewModel
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.session.SessionActivity
import com.google.android.material.snackbar.Snackbar
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
        private const val FRAGMENT_ARTICLES_LIST = "articles_list"
        private const val FRAGMENT_BACKSTACK_SEARCH = "search"
        private const val FRAGMENT_FEEDS_LIST = "feeds_list"
        private const val CODE_START_IN_APP_UPDATE = 1
    }

    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null

    private lateinit var binding: ActivityArticleListBinding
    private val drawerLayout: DrawerLayout
        get() = binding.headlinesDrawer


    private val activityViewModel: ActivityViewModel by viewModels()
    private val accountViewModel: TtrssAccountViewModel by viewModels()
    private val inAppUpdateViewModel: InAppUpdateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityViewModel.selectedFeed.observe(this) { bindFeedInformation(it) }
        activityViewModel.feedSelectedEvent.observe(this, EventObserver {
            onFeedSelected(it)
        })

        activityViewModel.articleSelectedEvent.observe(this, EventObserver { (position, article) ->
            onArticleSelected(position, article)
        })

        activityViewModel.searchOpenedEvent.observe(this, EventObserver {
            navigateToSearch()
        })

        activityViewModel.searchClosedEvent.observe(this, EventObserver {
            navigateUpToList()
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

        inAppUpdateViewModel.isUpdateAvailable.observe(this) {
            if (it) {
                Timber.d("Update available")
                val snackbar = Snackbar.make(binding.middlePaneLayout, "There is a new update",
                    Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction("Install") {
                    inAppUpdateViewModel.startUpdateFlow(this, CODE_START_IN_APP_UPDATE)
                }
                snackbar.show()
            }
        }

        inAppUpdateViewModel.isUpdateReadyToInstall.observe(this) {
            if (it) {
                Timber.d("Update ready to install")
                val snackbar = Snackbar.make(binding.middlePaneLayout, "Update is ready to install",
                    Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction("Restart") {
                    inAppUpdateViewModel.completeUpdate()
                }
                snackbar.show()
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_article_list)

        binding.lifecycleOwner = this
        binding.activityViewModel = activityViewModel
        with(binding.startPaneLayout) {
            // dispatch window inset up to the navigation view (FeedsListFragment)
            setOnApplyWindowInsetsListener { _, insets ->
                var result = insets
                children.forEach {
                    result = it.dispatchApplyWindowInsets(result)
                }
                result
            }
        }

        if (savedInstanceState == null) {
            val feedListFragment = FeedListFragment.newInstance(supportFragmentManager.fragmentFactory)
            supportFragmentManager.commit {
                replace(R.id.start_pane_layout, feedListFragment, FRAGMENT_FEEDS_LIST)
            }
            activityViewModel.setSelectedFeed(Feed.FEED_ID_ALL_ARTICLES)
        }
        setupToolbar()
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbar
        setupSearch()

        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.headlinesDrawer, toolbar,
            R.string.drawer_open, R.string.drawer_close).also {
            binding.headlinesDrawer.addDrawerListener(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("activity result $requestCode result $resultCode")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_START_IN_APP_UPDATE && resultCode != Activity.RESULT_OK) {
            inAppUpdateViewModel.cancelUpdateFlow()
        }
    }

    private fun setupSearch() {
        val searchItem = with(binding.toolbar) {
            inflateMenu(R.menu.activity_articles_list)
            menu.findItem(R.id.articles_search)
        }

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                activityViewModel.onSearchOpened()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                activityViewModel.onSearchClosed()
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

        supportFragmentManager.addOnBackStackChangedListener {
            val isOnSearchFragment = supportFragmentManager.run {
                if (backStackEntryCount > 0) {
                    val backStackEntry = getBackStackEntryAt(backStackEntryCount - 1)
                    return@run backStackEntry.name == FRAGMENT_BACKSTACK_SEARCH
                }
                false
            }
            if (!isOnSearchFragment) {
                searchItem.collapseActionView()
            }
        }
    }

    public override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle?.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle?.onConfigurationChanged(newConfig)
    }


    private fun onArticleSelected(position: Int, item: Article) {
        val articleUri = ContentUris.withAppendedId(ArticlesContract.Article.CONTENT_URI, item.id)
        val intent = Intent(this, ArticleDetailActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = articleUri
        }
        startActivity(intent)
    }

    private fun bindFeedInformation(feed: Feed?) {
        title = feed?.title ?: ""
        binding.toolbar.title = title
    }

    private fun onFeedSelected(feedId: Long) {
        navigateUpToList()
        supportFragmentManager.commit {
            val hf = ArticlesListFragment.newInstance(supportFragmentManager.fragmentFactory, feedId)
            replace(R.id.middle_pane_layout, hf, FRAGMENT_ARTICLES_LIST)
        }
        drawerLayout.closeDrawers()
    }

    private fun navigateToSearch() {
        supportFragmentManager.commit {
            val hf = ArticlesSearchFragment.newInstance(supportFragmentManager.fragmentFactory)
            replace(R.id.middle_pane_layout, hf, FRAGMENT_ARTICLES_LIST)
            addToBackStack(FRAGMENT_BACKSTACK_SEARCH)
        }
        drawerLayout.closeDrawers()
    }

    private fun navigateUpToList() {
        supportFragmentManager.popBackStack(FRAGMENT_BACKSTACK_SEARCH, POP_BACK_STACK_INCLUSIVE)
        binding.appBar.setExpanded(true)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
}
