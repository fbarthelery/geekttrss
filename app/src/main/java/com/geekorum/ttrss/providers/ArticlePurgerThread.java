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
package com.geekorum.ttrss.providers;

import android.app.job.JobParameters;
import android.os.Process;
import android.util.Log;
import com.geekorum.geekdroid.jobs.JobThread;
import com.geekorum.geekdroid.jobs.ThreadedJobService;

import java.util.concurrent.TimeUnit;

/**
 * Purge old articles that will probably not be read again
 */
public class ArticlePurgerThread extends JobThread {

    private static final String TAG = ArticlePurgerThread.class.getSimpleName();

    private final ArticlesProvidersDao articlesProvidersDao;

    public ArticlePurgerThread(ThreadedJobService jobService, JobParameters parameters, ArticlesProvidersDao articlesProvidersDao) {
        super(jobService, parameters);
        this.articlesProvidersDao = articlesProvidersDao;
    }

    @Override
    public void run() {
        setProcessPriority(Process.THREAD_PRIORITY_BACKGROUND);
        purgeOldArticles();
        completeJob(false);
    }

    private void purgeOldArticles() {
        // older than 3 months
        long oldTimeSec = System.currentTimeMillis() / 1000 - TimeUnit.DAYS.toSeconds(90);

        int deleted = articlesProvidersDao.deleteNonImportantArticlesBeforeTime(oldTimeSec);
        Log.i(TAG, "Purge " + deleted + " old articles");
    }

}
