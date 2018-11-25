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

import android.accounts.Account;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import com.geekorum.ttrss.add_feed.AddFeedService;
import com.geekorum.ttrss.providers.ArticlesContract;
import com.geekorum.ttrss.providers.PurgeArticlesJobService;
import com.geekorum.ttrss.sync.SyncContract;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * Manage the different background jobs submitted to the JobScheduler class
 */
public class BackgroundJobManager {

    private static final int SUBSCRIBE_TO_FEED_JOB_ID = 1;
    private static final int PERIODIC_PURGE_JOB_ID = 3;

    public static final long PERIODIC_REFRESH_JOB_INTERVAL_S = TimeUnit.HOURS.toSeconds(2);
    private static final long PERIODIC_PURGE_JOB_INTERVAL_MILLIS = TimeUnit.DAYS.toMillis(1);
    public static final long PERIODIC_FULL_REFRESH_JOB_INTERVAL_S = TimeUnit.DAYS.toSeconds(1);

    private BackgroundJobManagerImpl impl;


    @Inject
    public BackgroundJobManager(android.app.Application application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            impl = new BackgroundJobManagerNougatImpl(application);
        } else {
            impl = new BackgroundJobManagerLollipopImpl(application);
        }
    }

    public void refresh(Account account) {
        impl.refresh(account);
    }

    public void refreshFeed(Account account, long feedId) {
        impl.refreshFeed(account, feedId);
    }

    public void setupPeriodicJobs() {
        setupPeriodicPurge();
    }

    public void subscribeToFeed(Account account, String feedUrl, long categoryId,
                                String feedLogin, String feedPassword) {
        impl.subscribeToFeed(account, feedUrl, categoryId, feedLogin, feedPassword);
    }

    private void setupPeriodicPurge() {
        impl.setupPeriodicPurge();
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private static class BackgroundJobManagerLollipopImpl extends BackgroundJobManager.BackgroundJobManagerImpl {
        JobScheduler jobScheduler;

        public BackgroundJobManagerLollipopImpl(Context context) {
            super(context);
            jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }

        protected JobInfo getPendingJob(int id) {
            for (JobInfo job : jobScheduler.getAllPendingJobs()) {
                if (job.getId() == id) {
                    return job;
                }
            }
            return null;
        }

        @Override
        public void setupPeriodicPurge() {
            // don't reschedule the job if it is already pending or running.
            // reschedule a job will stop a current running job and reset the timers
            JobInfo pendingJob = getPendingJob(PERIODIC_PURGE_JOB_ID);
            if (pendingJob != null) {
                return;
            }

            JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_PURGE_JOB_ID,
                    new ComponentName(context, PurgeArticlesJobService.class));
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                    .setPeriodic(PERIODIC_PURGE_JOB_INTERVAL_MILLIS)
                    .setPersisted(true)
                    .setRequiresDeviceIdle(true)
                    .setRequiresCharging(true);
            jobScheduler.schedule(builder.build());
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private static class BackgroundJobManagerNougatImpl extends BackgroundJobManager.BackgroundJobManagerLollipopImpl {
        public BackgroundJobManagerNougatImpl(Context context) {
            super(context);
        }

        protected JobInfo getPendingJob(int id) {
            return jobScheduler.getPendingJob(id);
        }
    }


    private static class BackgroundJobManagerImpl {
        protected Context context;

        BackgroundJobManagerImpl(Context context) {
            this.context = context;
        }

        public void refresh(Account account) {
            Bundle extras = new Bundle();
            requestSync(account, extras);
        }

        public void refreshFeed(Account account, long feedId) {
            Bundle extras = new Bundle();
            extras.putLong(SyncContract.EXTRA_FEED_ID, feedId);
            requestSync(account, extras);
        }

        private void requestSync(Account account, Bundle extras) {
            SyncRequest.Builder builder = new SyncRequest.Builder();
            builder.setSyncAdapter(account, ArticlesContract.AUTHORITY)
                    .setManual(true)
                    .setExpedited(true)
                    .setNoRetry(true)
                    .setExtras(extras)
                    .syncOnce();
            ContentResolver.requestSync(builder.build());
        }

        public void setupPeriodicPurge() {
            // nothing to do here
        }

        public void subscribeToFeed(Account account, String feedUrl,
                                    long categoryId, String feedLogin, String feedPassword) {
            AddFeedService.subscribeToFeed(SUBSCRIBE_TO_FEED_JOB_ID, context, account, feedUrl, categoryId, feedLogin, feedPassword );
        }
    }

}
