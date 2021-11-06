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

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.graphics.Insets
import androidx.core.view.*
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * An activity representing a list of Articles. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ArticleDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
@AndroidEntryPoint
class ArticleListActivity : SessionActivity() {

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
    private var drawerLayoutPresenter: DrawerLayoutPresenter? = null

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

        inAppUpdatePresenter = InAppUpdatePresenter(
            binding.bannerContainer,
            this,
            inAppUpdateViewModel,
            activityResultRegistry)

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
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // the appBar doesn't redraw statusBarForeground correctly. force it
        binding.appBar.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, _ ->
                binding.appBar.invalidate()
            })

        // banner container
        ViewCompat.setOnApplyWindowInsetsListener(binding.bannerContainer) { view, windowInsets ->
            // consume top padding since we are not on top of screen
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val noTopInsets = WindowInsetsCompat.Builder().setInsets(WindowInsetsCompat.Type.systemBars(),
                Insets.of(insets.left,
                    0,
                    insets.right,
                    insets.bottom)
            ).build()
            WindowInsetsCompat.toWindowInsetsCompat(
                view.onApplyWindowInsets(noTopInsets.toWindowInsets())
            )
        }
    }

    private fun setupToolbar() {
        setupSearch()
        setupSortOrder()
        val appBarConfiguration = AppBarConfiguration.Builder(R.id.articlesListFragment, R.id.articlesListByTagFragment)
            .setOpenableLayout(drawerLayout)
            .build()
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        appBarPresenter = AppBarPresenter(binding.appBar, binding.toolbar,
            binding.tagsList,
            tagsViewModel, navController)

        // workaround with compose fragment
        // it looks like appbar_scrolling_view_behavior is not applied correctly
        // I don't know exactly what additional size is added to the height of the screen.
        // anyway we need to communicate it to the compose view to add it as bottom padding.
        // appBar height works fine
        binding.appBar.doOnLayout {
            activityViewModel.appBarHeight = it.height
        }
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
        feedNavigationPresenter =
            FeedsNavigationMenuPresenter(binding.navigationView, navController,
                feedsViewModel, accountViewModel, activityViewModel)

        drawerLayout?.let {
            drawerLayoutPresenter = DrawerLayoutPresenter(it, navController, this, this)
        }
    }

    private fun setupSearch() {
        val searchItem = with(binding.toolbar) {
            inflateMenu(R.menu.activity_articles_list)
            menu.findItem(R.id.articles_search)
        }
        searchToolbarPresenter = SearchToolbarPresenter(searchItem, navController, activityViewModel)
    }

}
