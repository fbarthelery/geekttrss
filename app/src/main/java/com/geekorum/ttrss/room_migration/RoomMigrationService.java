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
package com.geekorum.ttrss.room_migration;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.geekorum.ttrss.Application;
import com.geekorum.ttrss.NotificationCenter;
import com.geekorum.ttrss.R;
import com.geekorum.ttrss.di.AndroidComponentsModule;
import com.geekorum.ttrss.di.ApplicationComponent;
import com.geekorum.ttrss.providers.DbHelper;

import java.util.Arrays;

import javax.inject.Inject;

import static android.app.NotificationChannel.DEFAULT_CHANNEL_ID;
import static com.geekorum.ttrss.NotificationCenter.NOTIF_ID_ROOM_MIGRATION_SERVICE;

/**
 * Service to migrate data to Room database.
 */
public class RoomMigrationService extends IntentService {
    public static final String ACTION_MIGRATION_PROGRESS = "com.geekorum.ttrss.room_migration_progress";
    public static final String EXTRA_PROGRESS = "progress";
    public static final String EXTRA_MAX = "max";
    public static final String EXTRA_SUCCESS = "success";

    @Inject
    NotificationCenter notificationCenter;

    @Inject
    RoomMigration.Factory roomMigrationFactory;

    @Inject
    LocalBroadcastManager localBroadcastManager;

    public RoomMigrationService() {
        super("Room migration service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getRoomMigrationComponent().inject(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (needMigration(this)) {
            Notification notification = buildNotification(0, 100);
            startForeground(NOTIF_ID_ROOM_MIGRATION_SERVICE, notification);
            RoomMigration migration = roomMigrationFactory
                    .setProgressListener(this::notifyProgress)
                    .setCompletionListener(this::onCompletion)
                    .build();
            migration.execute();
        } else {
            onCompletion(true);
        }
    }

    private void notifyProgress(int progress, int max) {
        updateNotification(progress, max);
        Intent intent = new Intent(ACTION_MIGRATION_PROGRESS);
        intent.putExtra(EXTRA_PROGRESS, progress);
        intent.putExtra(EXTRA_MAX, max);
        localBroadcastManager.sendBroadcast(intent);
    }

    public static boolean needMigration(Context context) {
        return Arrays.asList(context.databaseList()).contains(DbHelper.DATABASE_NAME);
    }

    private void onCompletion(boolean success) {
        stopForeground(true);
        Intent intent = new Intent(ACTION_MIGRATION_PROGRESS);
        intent.putExtra(EXTRA_PROGRESS, 100);
        intent.putExtra(EXTRA_MAX, 100);
        intent.putExtra(EXTRA_SUCCESS, success);
        localBroadcastManager.sendBroadcast(intent);
    }

    private Notification buildNotification(int progress, int max) {
        NotificationCompat.Builder builder = getNotificationBuilder();
        Intent intent = new Intent(this, RoomMigrationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return builder.setContentTitle(getString(R.string.notif_room_migration_title))
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setProgress(max, progress, false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).build();
    }

    @SuppressWarnings("deprecation")
    @NonNull
    private NotificationCompat.Builder getNotificationBuilder() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID);
        }
        return new NotificationCompat.Builder(this);
    }

    private void updateNotification(int progress, int max) {
        Notification notification = buildNotification(progress, max);
        notificationCenter.notify(NotificationCenter.NOTIF_ID_ROOM_MIGRATION_SERVICE, notification);
    }

    private RoomMigrationComponent getRoomMigrationComponent() {
        ApplicationComponent applicationComponent = ((Application) getApplication()).getApplicationComponent();
        return applicationComponent
                .createRoomMigrationComponent()
                .androidComponentsModule(new AndroidComponentsModule(this))
                .build();
    }

}
