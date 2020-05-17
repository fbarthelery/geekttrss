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

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.geekdroid.views.doOnApplyWindowInsets
import com.geekorum.ttrss.R
import com.geekorum.ttrss.article_details.ArticleDetailActivity
import com.geekorum.ttrss.databinding.ActivityArticleListBinding
import com.geekorum.ttrss.in_app_update.InAppUpdateViewModel
import com.geekorum.ttrss.on_demand_modules.OnDemandModuleManager
import com.geekorum.ttrss.session.SessionActivity
import com.google.android.material.appbar.AppBarLayout
import timber.log.Timber
import javax.inject.Inject

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
        internal const val CODE_START_IN_APP_UPDATE = 1
    }

    private lateinit var binding: ActivityArticleListBinding
    private val drawerLayout: DrawerLayout?
        get() = binding.headlinesDrawer


    private lateinit var navController: NavController
    private val activityViewModel: ActivityViewModel by viewModels()
    private val accountViewModel: TtrssAccountViewModel by viewModels()
    private val inAppUpdateViewModel: InAppUpdateViewModel by viewModels()
    private val feedsViewModel: FeedsViewModel by viewModels()
    private val tagsViewModel: TagsViewModel by viewModels()

    private lateinit var inAppUpdatePresenter: InAppUpdatePresenter
    private lateinit var searchToolbarPresenter: SearchToolbarPresenter
    private lateinit var appBarPresenter: AppBarPresenter
    private lateinit var fabPresenter: FabPresenter
    private lateinit var feedNavigationPresenter: FeedsNavigationMenuPresenter
    private lateinit var accountHeaderPresenter: AccountHeaderPresenter
    private lateinit var drawerLayoutPresenter: DrawerLayoutPresenter

    @Inject
    lateinit var moduleManager: OnDemandModuleManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        //TODO use a preference ?
        feedsViewModel.setOnlyUnread(true)

        binding = DataBindingUtil.setContentView<ActivityArticleListBinding>(this,
            R.layout.activity_article_list).apply {
            lifecycleOwner = this@ArticleListActivity
            activityViewModel = this@ArticleListActivity.activityViewModel
        }

        inAppUpdatePresenter = InAppUpdatePresenter(binding.bannerContainer, this, inAppUpdateViewModel)

        navController = findNavController(R.id.middle_pane_layout)

        setupToolbar()
        setupNavigationView()
        setupEdgeToEdge()
        setupFab()
    }

    private fun setupFab() {
        fabPresenter = FabPresenter(binding.fab, navController)
    }

    private fun setupEdgeToEdge() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        // the appBar doesn't redraw statusBarForeground correctly. force it
        binding.appBar.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, _ ->
                binding.appBar.invalidate()
            })

        // banner container
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

        // navigation view
        val innerView = binding.navigationView[0]
        val innerViewInitialPaddingBottom = innerView.paddingBottom
        binding.navigationView.doOnApplyWindowInsets { _, insets, _ ->
            innerView.updatePadding(
                bottom = innerViewInitialPaddingBottom + insets.systemWindowInsetBottom)
            insets
        }
    }

    private fun setupToolbar() {
        setupSearch()
        setupSortOrder()
        val appBarConfiguration = AppBarConfiguration.Builder(R.id.articlesListFragment, R.id.articlesListByTagFragment)
            .setDrawerLayout(drawerLayout)
            .build()
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        appBarPresenter = AppBarPresenter(binding.appBar, binding.toolbar, binding.tagsList, binding.tagsGroup,
            this, tagsViewModel, navController)
    }

    private fun setupSortOrder() {
        val mostRecent = binding.toolbar.menu.findItem(R.id.articles_sort_order_most_recent)!!
        val oldestFirst = binding.toolbar.menu.findItem(R.id.articles_sort_order_oldest)!!
        activityViewModel.mostRecentSortOrder.observe(this) {
            if (it) {
                mostRecent.isChecked = true
            } else {
                oldestFirst.isChecked = true
            }
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.articles_sort_order_oldest -> {
                    activityViewModel.setSortByMostRecentFirst(false)
                    true
                }
                R.id.articles_sort_order_most_recent -> {
                    activityViewModel.setSortByMostRecentFirst(true)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNavigationView() {
        val feedsMenu = binding.navigationView.menu.addSubMenu(R.string.title_feeds_menu)
        binding.navigationView.inflateMenu(R.menu.fragment_feed_list)

        feedNavigationPresenter =
            FeedsNavigationMenuPresenter(binding.navigationView, feedsMenu, this, navController,
                feedsViewModel, activityViewModel)
        binding.navigationView.setupWithNavController(navController, feedNavigationPresenter)

        val headerView = binding.navigationView.getHeaderView(0)
        accountHeaderPresenter = AccountHeaderPresenter(headerView, this, accountViewModel)
        drawerLayout?.let {
            drawerLayoutPresenter = DrawerLayoutPresenter(it, navController, this)
        }
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
        searchToolbarPresenter = SearchToolbarPresenter(searchItem, navController, activityViewModel)
    }

}
