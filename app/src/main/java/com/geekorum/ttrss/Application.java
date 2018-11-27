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

import android.app.Activity;
import android.app.Service;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatDelegate;
import com.geekorum.ttrss.di.ApplicationComponent;
import com.geekorum.ttrss.di.DaggerApplicationComponent;
import com.squareup.picasso.Picasso;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import timber.log.Timber;

import java.util.Set;

import javax.inject.Inject;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO;

/**
 * Initialize global component for the TTRSS application.
 */
public class Application extends android.app.Application implements HasActivityInjector, HasServiceInjector {
    @Inject
    ApplicationComponent applicationComponent;

    @Inject
    DispatchingAndroidInjector<Activity> dispatchinActivityInjector;
    @Inject
    DispatchingAndroidInjector<Service> dispatchingServiceInjector;

    @Inject
    Set<Timber.Tree> timberTrees;

    @Inject
    Picasso picasso;

    private boolean needToInject = true;

    @Override
    public void onCreate() {
        injectIfNecessary();
        super.onCreate();
        Timber.plant(timberTrees.toArray(new Timber.Tree[0]));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String nighModeStr = sharedPreferences.getString(SettingsActivity.KEY_THEME, Integer.toString(MODE_NIGHT_AUTO));
        int nighMode = Integer.valueOf(nighModeStr);
        AppCompatDelegate.setDefaultNightMode(nighMode);
    }

    public void injectIfNecessary() {
        if (needToInject) {
            synchronized (this) {
                if (needToInject) {
                    DaggerApplicationComponent.builder()
                            .bindApplication(this)
                            .build().inject(this);
                    setupPicasso();
                    needToInject = false;
                }
            }
        }
    }

    public ApplicationComponent getApplicationComponent() {
        // Content providers can be created before Application.onCreate() is called
        injectIfNecessary();
        return applicationComponent;
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchinActivityInjector;
    }

    private void setupPicasso() {
        Picasso.setSingletonInstance(picasso);
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingServiceInjector;
    }
}
