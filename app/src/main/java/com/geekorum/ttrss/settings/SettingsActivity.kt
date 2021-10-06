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
package com.geekorum.ttrss.settings

import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsService
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.geekorum.ttrss.BuildConfig
import com.geekorum.ttrss.R
import com.geekorum.ttrss.core.BaseActivity
import com.geekorum.ttrss.databinding.ActivitySettingsBinding
import com.geekorum.ttrss.debugtools.withStrictMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SettingsActivity : BaseActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allowDiskReads()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun allowDiskReads() {
        val threadPolicy = StrictMode.allowThreadDiskReads()
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                StrictMode.setThreadPolicy(threadPolicy)
            }
        })
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val fragmentClass = pref.fragment
        supportFragmentManager.commit {
            val fragment = daggerDelegateFragmentFactory.instantiate(classLoader, fragmentClass)
            addToBackStack("preferences_screen")
            replace(R.id.preferences_container, fragment)
        }
        return true
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class SettingsFragment @Inject constructor(
        private val packageManager: PackageManager
    ) : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            withStrictMode(StrictMode.allowThreadDiskReads()) {
                addPreferencesFromResource(R.xml.pref_general)
            }

            findPreference<Preference>(KEY_THEME)!!.apply {
                summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                onPreferenceChangeListener = ThemePreferenceListener()
            }
            findPreference<ListPreference>(KEY_IN_APP_BROWSER_ENGINE)!!.apply {
                summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                val inAppBrowser = getInAppBrowserPackages()
                entryValues = inAppBrowser.keys.toTypedArray()
                entries = inAppBrowser.values.toTypedArray()
            }

            displayVersion()
        }

        private fun displayVersion() {
            findPreference<Preference>(KEY_ABOUT_VERSION)!!.apply {
                title = getString(R.string.pref_title_about_version, BuildConfig.VERSION_NAME)
                summary = getString(R.string.pref_summary_about_version,
                    BuildConfig.REPOSITORY_CHANGESET,
                    BuildConfig.BUILD_TYPE)
            }
        }

        private fun getInAppBrowserPackages(): Map<String, CharSequence> {
            val activityIntent = Intent(Intent.ACTION_VIEW, "http://".toUri()).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            val browsersResolveInfoList =
                packageManager.queryIntentActivities(activityIntent, PackageManager.MATCH_ALL)

            val customTabsSupported = browsersResolveInfoList.filter {
                val customTabServiceIntent = Intent(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION).apply {
                    `package` = it.activityInfo.packageName
                }
                packageManager.resolveService(customTabServiceIntent, 0) != null
            }
            return customTabsSupported.associate { it.activityInfo.packageName to it.loadLabel(packageManager) }
        }
    }

    private class ThemePreferenceListener : Preference.OnPreferenceChangeListener {

        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            val oldValue = (preference as ListPreference).value
            if (newValue != oldValue) {
                val nighMode = Integer.valueOf(newValue as String)
                AppCompatDelegate.setDefaultNightMode(nighMode)
            }
            return true
        }
    }

    companion object {
        const val KEY_THEME = "theme"
        const val KEY_IN_APP_BROWSER_ENGINE = "in_app_browser_engine"
        const val KEY_ABOUT_VERSION = "about_version"
    }

}
