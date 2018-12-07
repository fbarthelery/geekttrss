/**
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2018 by Frederic-Charles Barthelery.
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

import android.accounts.AccountManager
import android.content.ContentUris
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.transaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.BackgroundJobManager
import com.geekorum.ttrss.R
import com.geekorum.ttrss.article_details.ArticleDetailActivity
import com.geekorum.ttrss.article_details.ArticleDetailFragment
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.databinding.ActivityArticleListBinding
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.session.SessionActivity
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
        private const val FRAGMENT_ARTICLES_LIST = "articles_list"
        private const val FRAGMENT_FEEDS_LIST = "feeds_list"
    }

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private val twoPane:Boolean by lazy {
        binding.articleDetailContainer != null
    }

    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null

    private lateinit var binding: ActivityArticleListBinding
    private val drawerLayout: DrawerLayout?
        get() = binding.headlinesDrawer

    @Inject
    lateinit var backgroundJobManager: BackgroundJobManager

    @Inject
    lateinit var accountManager: AccountManager

    lateinit var activityViewModel: ActivityViewModel
    lateinit var accountViewModel: TtrssAccountViewModel

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPeriodicJobs()
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false)

        activityViewModel = ViewModelProviders.of(this).get()
        activityViewModel.selectedFeed.observe(this, Observer<Feed> { onFeedSelected(it) })

        activityViewModel.articleSelectedEvent.observe(this, EventObserver { parameters ->
            onArticleSelected(parameters.position, parameters.article)
        })

        accountViewModel = ViewModelProviders.of(this, viewModelFactory).get()
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

        binding = DataBindingUtil.setContentView(this, R.layout.activity_article_list)

        binding.setLifecycleOwner(this)
        binding.activityViewModel = activityViewModel
        if (binding.headlinesDrawer != null) {
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
        }

        if (savedInstanceState == null) {
            val feedListFragment = FeedListFragment()
            supportFragmentManager.transaction {
                replace(R.id.start_pane_layout, feedListFragment, FRAGMENT_FEEDS_LIST)
            }
            val feed = Feed.createVirtualFeedForId(Feed.FEED_ID_ALL_ARTICLES)
            activityViewModel.setSelectedFeed(feed)
        }
        setupToolbar()
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbar.toolbar
        toolbar.title = title
        if (!twoPane) {
            actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.headlinesDrawer, toolbar,
                R.string.drawer_open, R.string.drawer_close)
            binding.headlinesDrawer?.addDrawerListener(actionBarDrawerToggle!!)
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_menu_24dp)
            toolbar.setNavigationOnClickListener {
                binding.middlePaneLayout.visibility = View.GONE
                binding.startPaneLayout.visibility = View.VISIBLE
            }
        }
    }


    private fun setupPeriodicJobs() {
        backgroundJobManager.setupPeriodicJobs()
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
        if (twoPane) {
            val articleDetailFragment = ArticleDetailFragment.newInstance(articleUri)
            supportFragmentManager.transaction {
                replace(R.id.article_detail_container, articleDetailFragment)
            }
            binding.startPaneLayout.visibility = View.GONE
            // TODO: add some good animation
        } else {
            val intent = Intent(this, ArticleDetailActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = articleUri
            }
            startActivity(intent)
        }
    }

    /*
     *  From MasterActivity
     */
    private fun onFeedSelected(feed: Feed) {
        title = feed.title
        binding.toolbar.toolbar.title = title
        supportFragmentManager.transaction {
            val hf = ArticlesListFragment.newInstance(feed.id)
            replace(R.id.middle_pane_layout, hf, FRAGMENT_ARTICLES_LIST)
        }
        if (twoPane) {
            binding.middlePaneLayout.visibility = View.VISIBLE
            binding.startPaneLayout.visibility = View.GONE
        }
        drawerLayout?.closeDrawers()
    }

}
