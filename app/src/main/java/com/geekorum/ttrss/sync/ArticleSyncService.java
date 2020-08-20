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
package com.geekorum.ttrss.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * Background service to synchronize Tiny Tiny Rss articles.
 */
@AndroidEntryPoint
public class ArticleSyncService extends Service {
    @Inject ArticleSyncAdapter articleSyncAdapter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return articleSyncAdapter.getSyncAdapterBinder();
    }

}
