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
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.view.get
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.geekorum.geekdroid.dagger.DaggerDelegateSavedStateVMFactory
import com.geekorum.ttrss.BaseFragment
import com.geekorum.ttrss.Features
import com.geekorum.ttrss.R
import com.geekorum.ttrss.activityViewModels
import com.geekorum.ttrss.databinding.FragmentFeedsBinding
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
    private lateinit var feedNavigationPresenter: FeedsNavigationMenuPresenter

    private val feedsViewModel: FeedsViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by activityViewModels()
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
        setUpNavigationView()
        setUpEdgeToEdge()
        setupViewModels()
    }

    private fun setUpNavigationView() {
        binding.navigationView.setNavigationItemSelectedListener(this)
        val feedsMenu = binding.navigationView.menu.addSubMenu(R.string.title_feeds_menu)
        binding.navigationView.inflateMenu(R.menu.fragment_feed_list)

        feedNavigationPresenter =
            FeedsNavigationMenuPresenter(viewLifecycleOwner, binding.navigationView, feedsViewModel,
                activityViewModel, feedsMenu)
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

        activityViewModel.selectedFeed.observe(viewLifecycleOwner) { feed ->
            feed?.let { feedsViewModel.setSelectedFeed(it.id) }
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when {
            item.itemId == R.id.manage_feeds -> {
                installOrStartManageFeed()
                true
            }
            item.itemId == R.id.settings -> {
                navigateToSettings()
                true
            }
            else -> feedNavigationPresenter.onFeedSelected(item)
        }
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
        private const val CODE_INSTALL_MANAGE_FEED = 1
    }

}
