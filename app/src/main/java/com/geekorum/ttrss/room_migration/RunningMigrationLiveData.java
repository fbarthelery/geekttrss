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

import androidx.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * A {@link LiveData} to follow a running migration.
 */
public class RunningMigrationLiveData extends LiveData<RunningMigrationLiveData.MigrationProgress> {

    private LocalBroadcastManager localBroadcastManager;
    private Context context;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (RoomMigrationService.ACTION_MIGRATION_PROGRESS.equals(intent.getAction())) {
                int progress = intent.getIntExtra(RoomMigrationService.EXTRA_PROGRESS, 0);
                int max = intent.getIntExtra(RoomMigrationService.EXTRA_MAX, 100);
                boolean success = intent.getBooleanExtra(RoomMigrationService.EXTRA_SUCCESS, false);
                MigrationProgress migrationProgress = new MigrationProgress(success, progress, max);
                setValue(migrationProgress);
            }
        }
    };

    public RunningMigrationLiveData(Context context) {
        this.context = context.getApplicationContext();
        this.localBroadcastManager = LocalBroadcastManager.getInstance(this.context);
    }

    @Override
    protected void onActive() {
        IntentFilter filter = new IntentFilter(RoomMigrationService.ACTION_MIGRATION_PROGRESS);
        localBroadcastManager.registerReceiver(receiver, filter);
        Intent intent = new Intent(context, RoomMigrationService.class);
        startService(intent);
    }

    private void startService(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    protected void onInactive() {
        localBroadcastManager.unregisterReceiver(receiver);
    }

    static class MigrationProgress {
        boolean isSuccessFull;
        int progress;
        int max;

        MigrationProgress(boolean isSuccessFull, int progress, int max) {
            this.isSuccessFull = isSuccessFull;
            this.progress = progress;
            this.max = max;
        }
    }

}
