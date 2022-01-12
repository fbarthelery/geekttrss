/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.geekorum.geekdroid.accounts.CancellableSyncAdapter;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Allows the Android Sync Framework to synchronize Articles of Tiny Tiny Rss.
 */
public class ArticleSyncAdapter extends CancellableSyncAdapter {

    private SyncComponent.Builder syncBuilder;

    @Inject
    public ArticleSyncAdapter(@ApplicationContext Context context, SyncComponent.Builder syncBuilder) {
        super(context, true, false); //no parallel sync for now
        this.syncBuilder = syncBuilder;
    }

    @NonNull
    @Override
    public CancellableSync createCancellableSync(@NonNull Account account, @NonNull Bundle extras,
                                                 @NonNull String authority, @NonNull ContentProviderClient provider,
                                                 @NonNull SyncResult syncResult) {
        SyncComponent syncComponent = syncBuilder
                .seedAccount(account)
                .build();

        return syncComponent.getArticleSynchronizerFactory().create(extras);
    }
}
