/*
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

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatDelegate;
import com.geekorum.ttrss.di.ApplicationComponent;
import com.geekorum.ttrss.di.DaggerApplicationComponent;
import com.squareup.picasso.Picasso;
import dagger.android.support.DaggerApplication;
import timber.log.Timber;

import java.util.Set;

import javax.inject.Inject;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO;

/**
 * Initialize global component for the TTRSS application.
 */
public class Application extends DaggerApplication {

    @Inject
    Set<Timber.Tree> timberTrees;

    @Inject
    Picasso picasso;

    @Override
    public void onCreate() {
        super.onCreate();
        setupPicasso();
        Timber.plant(timberTrees.toArray(new Timber.Tree[0]));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String nighModeStr = sharedPreferences.getString(SettingsActivity.KEY_THEME, Integer.toString(MODE_NIGHT_AUTO));
        int nighMode = Integer.valueOf(nighModeStr);
        AppCompatDelegate.setDefaultNightMode(nighMode);
    }

    private void setupPicasso() {
        Picasso.setSingletonInstance(picasso);
    }

    @Override
    protected ApplicationComponent applicationInjector() {
        return DaggerApplicationComponent.builder().bindApplication(this).build();
    }
}
