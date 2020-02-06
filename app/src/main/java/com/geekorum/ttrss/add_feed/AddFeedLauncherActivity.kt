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
package com.geekorum.ttrss.add_feed

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.geekorum.ttrss.core.BaseActivity
import com.geekorum.ttrss.Features
import com.geekorum.ttrss.R
import com.geekorum.ttrss.add_feed.StartInstallFragmentDirections.Companion.actionInstallManageFeed
import com.geekorum.ttrss.manage_feeds.add_feed.CompleteInstallFragmentDirections.Companion.actionAddFeed
import com.geekorum.ttrss.on_demand_modules.OnDemandModuleManager
import timber.log.Timber
import javax.inject.Inject


/**
 * Install the [Features.MANAGE_FEEDS] feature module if necessary then launch [AddFeedActivity] activity
 */
class AddFeedLauncherActivity : BaseActivity() {
    @Inject
    lateinit var moduleManager: OnDemandModuleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installOrStartManageFeed()
        finish()
    }

    private fun installOrStartManageFeed() {
        if (isManageFeedInstalled()) {
            try {
                val freshContext = createPackageContext(packageName, 0)
                val intent = intent.apply {
                    component = ComponentName.createRelative(freshContext, "com.geekorum.ttrss.manage_feeds.add_feed.AddFeedActivity")
                }
                startActivity(intent)
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.wtf(e, "Unable to create our package context")
            }
        } else {
            // copy intent but replace component
            val installerIntent = intent.apply {
                setClass(this@AddFeedLauncherActivity, AddFeedInstallerActivity::class.java)
            }
            startActivity(installerIntent)
        }
    }

    private fun isManageFeedInstalled(): Boolean {
        return moduleManager.installedModules.contains(Features.MANAGE_FEEDS)
    }
}

/**
 * Use navigation to install manage_feeds module and start AddFeedActivity
 *
 * This is needed because we can't know when we installed the module and launch the AddFeedActivity:
 *   - the default progress install fragment of navigation doesn't allow us to monitor the installation
 *   - the destination is an Activity which doesn't trigger OnDestinationChangeListener
 */
class AddFeedInstallerActivity : BaseActivity() {
    private var hasShownLauncherFragment = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_feed_installer)
        findNavController(R.id.nav_host_fragment).apply {
            addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.installerStart -> {
                        if (hasShownLauncherFragment)
                            finish()
                        else {
                            navigate(actionInstallManageFeed())
                            hasShownLauncherFragment = true
                        }
                    }
                    R.id.installerComplete -> {
                        navigate(actionAddFeed(intent.extras?.getString(Intent.EXTRA_TEXT)))
                        finish()
                    }
                }
            }
        }
    }
}

/**
 * Only used as a destination to fall back if feature module installation failed.
 * The AddFeedInstallerActivity takes care of the logic.
 */
class StartInstallFragment : Fragment()
