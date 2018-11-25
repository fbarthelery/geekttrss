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
package com.geekorum.ttrss.di;

import android.accounts.AccountManager;
import android.app.Application;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import dagger.Module;
import dagger.Provides;

/**
 * Provides some Android component for the life of the Application process
 */
@Module
public abstract class AndroidApplicationModule {

    @Provides
    static NotificationManager providesNotificationManager(Application application) {
        return application.getApplicationContext().getSystemService(NotificationManager.class);
    }

    @Provides
    static LocalBroadcastManager providesLocalBroadcastManager(Application application) {
        return LocalBroadcastManager.getInstance(application.getApplicationContext());
    }

    @Provides
    static ConnectivityManager providesConnectivityManager(Application application) {
        return application.getSystemService(ConnectivityManager.class);
    }

    @Provides
    static PackageManager providesPackageManager(Application application) {
        return application.getPackageManager();
    }

    @Provides
    static AccountManager providesAccountManager(Application application) {
        return AccountManager.get(application);
    }
}
