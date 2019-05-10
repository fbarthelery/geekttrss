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
package com.geekorum.ttrss.providers;

import android.net.Uri;
import android.provider.BaseColumns;
import com.geekorum.ttrss.BuildConfig;

/**
 * Allows to interact with the ArticlesProvider.
 */
public class ArticlesContract {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".providers.articles";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);



    public static final class Article implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "articles");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.geekorum.ttrss.article";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.geekorum.ttrss.article";

        // columns of an Article
        // booleans
        public static final String UNREAD = "unread";
        // only use temporary to prevent cursors to remove the articles to be shown after reading it
        public static final String TRANSIENT_UNREAD = "transiant_unread";
        public static final String STARRED = "marked";
        public static final String PUBLISHED = "published";
        public static final String IS_UPDATED = "is_updated";

        // integers
        public static final String SCORE = "score";

        // longs
        public static final String LAST_TIME_UPDATE = "last_time_update";
        public static final String FEED_ID = "feed_id";

        public static final String TITLE = "title";
        public static final String LINK = "link";
        public static final String TAGS = "tags";
        public static final String CONTENT = "content";
        public static final String AUTHOR = "author";
        public static final String FLAVOR_IMAGE_URI = "flavor_image_uri";
        public static final String CONTENT_EXCERPT = "content_excerpt";

        private Article() {
            // not instanciable
        }
    }

    public static final class Feed implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "feeds");

        // columns of a Feed
        public static final String URL = "url";
        public static final String TITLE = "title" ;
        public static final String CAT_ID =  "cat_id";
        public static final String LAST_TIME_UPDATE  = "last_time_update";
        public static final String DISPLAY_TITLE = "display_title";
        public static final String UNREAD_COUNT = "unread_count";


        private Feed() {
            // not instanciable
        }
    }

    public static final class Category implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "categories");

        // columns of a Category
        public static final String TITLE = "title" ;
        public static final String UNREAD_COUNT = "unread_count";


        private Category() {
            // not instanciable
        }
    }

    public static final class Transaction implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "transactions");

        // columns of a Transaction
        // long
        public static final String ARTICLE_ID = "article_id";
        // String
        public static final String FIELD = "field";
        //boolean
        public static final String VALUE = "value";

        public enum Field {
            STARRED(0),
            PUBLISHED(1),
            UNREAD(2),
            NOTE(3),;

            private int apiInteger;

            Field(int value) {
                this.apiInteger = value;
            }

            public int asApiInteger() {
                return apiInteger;
            }
        }

        private Transaction() {
            // not instanciable
        }
    }

}
