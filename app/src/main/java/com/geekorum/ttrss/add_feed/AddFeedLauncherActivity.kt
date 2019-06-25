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

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.geekorum.ttrss.BaseActivity
import com.geekorum.ttrss.Features
import com.geekorum.ttrss.on_demand_modules.OnDemandModuleManager
import com.geekorum.ttrss.settings.manage_features.InstallFeatureActivity
import timber.log.Timber
import javax.inject.Inject


private const val CODE_INSTALL_MANAGE_FEED = 1

/**
 * Install the [Features.MANAGE_FEEDS] feature module if necessary then launch [AddFeedActivity] activity
 */
class AddFeedLauncherActivity : BaseActivity() {
    @Inject
    lateinit var moduleManager: OnDemandModuleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installOrStartManageFeed()
    }

    private fun installOrStartManageFeed() {
        if (isManageFeedInstalled()) {
            try {
                val freshContext = createPackageContext(packageName, 0)
                val intent = intent
                intent.component = ComponentName(freshContext, AddFeedActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.wtf(e, "Unable to create our package context")
            }
        } else {
            val intent = Intent(this, InstallFeatureActivity::class.java)
            intent.putExtra(InstallFeatureActivity.EXTRA_FEATURES_LIST, arrayOf(Features.MANAGE_FEEDS))
            startActivityForResult(intent, CODE_INSTALL_MANAGE_FEED)
        }
    }

    private fun isManageFeedInstalled(): Boolean {
        return moduleManager.installedModules.contains(Features.MANAGE_FEEDS)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CODE_INSTALL_MANAGE_FEED) {
            if (resultCode == Activity.RESULT_OK) {
                installOrStartManageFeed()
            } else {
                finish()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
            finish()
        }
    }
}
