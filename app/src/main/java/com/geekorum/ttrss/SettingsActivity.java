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
package com.geekorum.ttrss;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragmentCompat;
import com.geekorum.geekdroid.preferences.PreferenceSummaryBinder;
import com.geekorum.ttrss.databinding.ActivitySettingsBinding;

public class SettingsActivity extends BatteryFriendlyActivity {

    public static final String KEY_THEME = "theme";
    public static final String KEY_ABOUT_VERSION = "about_version";
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SettingsFragment extends PreferenceFragmentCompat {
        private final PreferenceSummaryBinder summaryBinder = new PreferenceSummaryBinder();

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general);

            Preference themePreference = findPreference(KEY_THEME);
            summaryBinder.bindPreferenceSummaryToValue(themePreference);
            themePreference.setOnPreferenceChangeListener(new ThemePreferenceListener(getActivity(),
                    themePreference.getOnPreferenceChangeListener()));
            displayVersion();
        }

        private void displayVersion() {
            Preference preference = findPreference(KEY_ABOUT_VERSION);
            preference.setTitle(getString(R.string.pref_title_about_version, BuildConfig.VERSION_NAME));
            preference.setSummary(getString(R.string.pref_summary_about_version,
                    BuildConfig.REPOSITORY_CHANGESET,
                    BuildConfig.BUILD_TYPE));
        }

    }

    private static class ThemePreferenceListener implements OnPreferenceChangeListener {
        private final Activity activity;
        private final OnPreferenceChangeListener wrapped;

        private ThemePreferenceListener(Activity activity, OnPreferenceChangeListener wrapped) {
            this.activity = activity;
            this.wrapped = wrapped;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean result = wrapped.onPreferenceChange(preference, newValue);
            String oldValue = ((ListPreference) preference).getValue();
            if (!newValue.equals(oldValue)) {
                int nighMode = Integer.valueOf((String) newValue);
                AppCompatDelegate.setDefaultNightMode(nighMode);
                activity.recreate();
            }
            return result;
        }
    }

}
