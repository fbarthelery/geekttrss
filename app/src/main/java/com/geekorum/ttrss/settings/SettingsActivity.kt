/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2023 by Frederic-Charles Barthelery.
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

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsService
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.geekorum.ttrss.BuildConfig
import com.geekorum.ttrss.R
import com.geekorum.ttrss.core.BaseActivity
import com.geekorum.ttrss.databinding.SettingsPreferencesContainerBinding
import com.geekorum.ttrss.debugtools.withStrictMode
import com.geekorum.ttrss.ui.AppTheme3
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SettingsActivity : BaseActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        allowDiskReads()
        setContent {
            AppTheme3 {
                val sysUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                DisposableEffect(sysUiController, useDarkIcons) {
                    sysUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
                    onDispose {  }
                }
                SettingsScreen(windowSizeClass = calculateWindowSizeClass(activity = this@SettingsActivity),
                    onNavigateUpClick = {
                        onSupportNavigateUp()
                    })
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.backStackEntryCount == 0) {
            return super.onSupportNavigateUp()
        }
        supportFragmentManager.popBackStack()
        return true
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
        val fragmentClass = requireNotNull(pref.fragment)
        supportFragmentManager.commit {
            val fragment = daggerDelegateFragmentFactory.instantiate(classLoader, fragmentClass)
            addToBackStack("preferences_screen")
            replace(R.id.preferences_container, fragment)
        }
        return true
    }

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    windowSizeClass: WindowSizeClass,
    onNavigateUpClick: () -> Unit,
) {
    val useTabletLayout = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (useTabletLayout) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface,
                titleContentColor = if (useTabletLayout) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
            )
            TopAppBar(
                scrollBehavior = scrollBehavior,
                colors = colors,
                title = {
                    Text(stringResource(R.string.activity_settings_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUpClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
                )
        },
    ) {
        if (useTabletLayout) {
            TabletLayoutContent(Modifier.padding(it)) {
                PreferencesContainer(Modifier.padding(4.dp))
            }
        } else {
            PreferencesContainer(Modifier.padding(it))
        }
    }
}

@Composable
private fun TabletLayoutContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .height(136.dp) // 192.dp - 56.dp of appbar
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .testTag("fakeAppBar")
        )

        ElevatedCard(
            Modifier
                .width(512.dp)
                .fillMaxSize()
                .testTag("contentCard"),
            content = content
        )
    }
}

@Composable
private fun PreferencesContainer(modifier: Modifier = Modifier) {
    AndroidViewBinding(factory = SettingsPreferencesContainerBinding::inflate, modifier)
}


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(device = Devices.TABLET)
@Preview(device = Devices.TABLET,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)@Composable
fun PreviewSettingsScreen() {
    AppTheme3 {
        BoxWithConstraints {
            val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
            SettingsScreen(windowSizeClass = windowSizeClass, onNavigateUpClick = {})
        }
    }
}