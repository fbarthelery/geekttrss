/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2019 by Frederic-Charles Barthelery.
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

/**
 * Define the Api to sync Tiny Tiny Rss accounts.
 */
public final class SyncContract {

    private SyncContract() {
        // non instantiable
    }

    /**
     * Set to -1 to refresh the full database of articles, default value is 500.
     */
    public static final String EXTRA_NUMBER_OF_LATEST_ARTICLES_TO_REFRESH = "numberOfArticlesToRefresh";

    /**
     * Id of the feed to synchronize.
     * default value is to synchronize all feeds
     * Type: Long
     */
    public static final String EXTRA_FEED_ID = "feedId";

}
