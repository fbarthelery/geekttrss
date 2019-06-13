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
package com.geekorum.ttrss.settings

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.geekorum.geekdroid.preferences.PreferenceSummaryBinder
import com.geekorum.ttrss.BaseActivity
import com.geekorum.ttrss.BuildConfig
import com.geekorum.ttrss.R
import com.geekorum.ttrss.databinding.ActivitySettingsBinding
import com.geekorum.ttrss.debugtools.withStrictMode


class SettingsActivity : BaseActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allowDiskReads()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.preferences_container, SettingsFragment())
            }
        }
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
    class SettingsFragment : PreferenceFragmentCompat() {
        private val summaryBinder = PreferenceSummaryBinder()

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            withStrictMode(StrictMode.allowThreadDiskReads()) {
                addPreferencesFromResource(R.xml.pref_general)
            }

            findPreference(KEY_THEME).apply {
                summaryBinder.bindPreferenceSummaryToValue(this)
                onPreferenceChangeListener = ThemePreferenceListener(
                    requireActivity(),
                    onPreferenceChangeListener)
            }
            displayVersion()
        }

        private fun displayVersion() {
            findPreference(KEY_ABOUT_VERSION).apply {
                title = getString(R.string.pref_title_about_version, BuildConfig.VERSION_NAME)
                summary = getString(R.string.pref_summary_about_version,
                    BuildConfig.REPOSITORY_CHANGESET,
                    BuildConfig.BUILD_TYPE)
            }
        }

    }

    private class ThemePreferenceListener(
        private val activity: Activity,
        private val wrapped: Preference.OnPreferenceChangeListener
    ) : Preference.OnPreferenceChangeListener {

        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            val result = wrapped.onPreferenceChange(preference, newValue)
            val oldValue = (preference as ListPreference).value
            if (newValue != oldValue) {
                val nighMode = Integer.valueOf(newValue as String)
                AppCompatDelegate.setDefaultNightMode(nighMode)
                activity.recreate()
            }
            return result
        }
    }

    companion object {
        const val KEY_THEME = "theme"
        const val KEY_ABOUT_VERSION = "about_version"
    }

}
