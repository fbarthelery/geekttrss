/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
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

import android.app.Notification;
import android.app.NotificationManager;
import android.os.Build;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Control the different notification of the application.
 */
@Singleton
public class NotificationCenter {
    public static final int NOTIF_ID_ROOM_MIGRATION_SERVICE = 1;

    private NotificationManager notificationManager;

    @Inject
    public NotificationCenter(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void initializeChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //TODO some day
        }
    }

    public void notify(int id, Notification notification) {
        notificationManager.notify(id, notification);
    }


}
