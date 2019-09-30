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
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.updatePadding
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
import com.geekorum.geekdroid.views.banners.BannerSpec
import com.geekorum.geekdroid.views.banners.buildBanner
import com.geekorum.ttrss.ArticlesListDirections
import com.geekorum.ttrss.R
import com.geekorum.ttrss.article_details.ArticleDetailActivity
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.databinding.ActivityArticleListBinding
import com.geekorum.ttrss.in_app_update.InAppUpdateViewModel
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.session.SessionActivity
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
        private const val CODE_START_IN_APP_UPDATE = 1
    }

    private lateinit var binding: ActivityArticleListBinding
    private val drawerLayout: DrawerLayout
        get() = binding.headlinesDrawer


    private lateinit var navController: NavController
    private val activityViewModel: ActivityViewModel by viewModels()
    private val accountViewModel: TtrssAccountViewModel by viewModels()
    private val inAppUpdateViewModel: InAppUpdateViewModel by viewModels()

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

        inAppUpdateViewModel.isUpdateAvailable.observe(this) {
            if (it) {
                Timber.d("Update available")
                val banner = buildBanner(this) {
                    messageId = R.string.banner_update_msg
                    icon = IconCompat.createWithResource(this@ArticleListActivity,
                        R.mipmap.ic_launcher)
                    setPositiveButton(R.string.banner_update_btn) {
                        inAppUpdateViewModel.startUpdateFlow(this@ArticleListActivity,
                            CODE_START_IN_APP_UPDATE)
                        hideBanner()
                    }
                    setNegativeButton(R.string.banner_dismiss_btn) {
                        hideBanner()
                    }
                }

                showBanner(banner)
            }
        }

        inAppUpdateViewModel.isUpdateReadyToInstall.observe(this) {
            if (it) {
                Timber.d("Update ready to install")
                val banner = buildBanner(this) {
                    message = "Update ready to install"
                    icon = IconCompat.createWithResource(this@ArticleListActivity,
                        R.mipmap.ic_launcher)
                    setPositiveButton("Restart") {
                        hideBanner()
                        inAppUpdateViewModel.completeUpdate()
                    }
                }

                showBanner(banner)
            }
        }

        binding = DataBindingUtil.setContentView<ActivityArticleListBinding>(this,
            R.layout.activity_article_list).apply {
            lifecycleOwner = this@ArticleListActivity
            activityViewModel = this@ArticleListActivity.activityViewModel
        }

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
        if (requestCode == CODE_START_IN_APP_UPDATE && resultCode != Activity.RESULT_OK) {
            inAppUpdateViewModel.cancelUpdateFlow()
        }
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

    private fun showBanner(bannerSpec: BannerSpec) {
        binding.bannerContainer.show(bannerSpec)
        val behavior = BottomSheetBehavior.from(binding.bannerContainer)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // wait for expanded state to set non hideable
        behavior.bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // nothing to do
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.isHideable = false
                    behavior.bottomSheetCallback = null
                }
            }
        }

        binding.root.doOnNextLayout {
            val fragmentContainerView = supportFragmentManager.findFragmentById(R.id.middle_pane_layout)!!.requireView()
            fragmentContainerView.updatePadding(bottom = binding.bannerContainer.height)
        }
    }

    private fun hideBanner() {
        val behavior = BottomSheetBehavior.from(binding.bannerContainer)
        behavior.isHideable = true
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        val fragmentContainerView = supportFragmentManager.findFragmentById(R.id.middle_pane_layout)!!.requireView()
        fragmentContainerView.updatePadding(bottom = 0)
    }
}
