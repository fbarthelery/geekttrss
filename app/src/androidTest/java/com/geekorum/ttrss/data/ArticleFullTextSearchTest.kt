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
package com.geekorum.ttrss.data

import android.database.sqlite.SQLiteDatabase
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.contentValuesOf
import androidx.lifecycle.Observer
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geekorum.ttrss.data.ArticlesDatabase.Tables
import com.geekorum.ttrss.providers.ArticlesContract
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class ArticleFullTextSearchTest {

    @get:Rule
    val roomRule = InstantTaskExecutorRule()

    private lateinit var database: ArticlesDatabase

    @BeforeTest
    fun beforeTest() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
            ArticlesDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        createSomeArticles(database.openHelper.writableDatabase)
    }

    @Test
    fun testAnFTSSearchPagedList() {
        val expected = Article(
            id = 1,
            contentData = ArticleContentIndexed(
                title = "Comment Linux est devenu un enjeu strategique pour la silicon valley",
                content = """L’emblématique système d’exploitation libre est devenu un outil-clé
                    |de tous les grands groupes du Web, comme l’illustre le rachat de Red Hat. […]""".trimMargin(),
                tags = "article tags",
                author = "article author"
            ),
            score = 0,
            isPublished = true,
            lastTimeUpdate = 0,
            isUnread = true,
            isTransientUnread = true,
            isStarred = true,
            isUpdated = true,
            feedId = 1,
            link = "article links",
            flavorImageUri = "article flavor image uri",
            contentExcerpt = "a content excerpt"
        )

        val articleDao = database.articleDao()
        val factory = articleDao.searchArticles("linux")
        val livedata = LivePagedListBuilder(factory, 10).build()

        val observer = Observer<PagedList<Article>> {
            assertThat(it).containsExactly(expected)
        }
        livedata.observeForever(observer)
        livedata.removeObserver(observer)
    }


    private fun createSomeArticles(db: SupportSQLiteDatabase) {
        var values = contentValuesOf(
            ArticlesContract.Category._ID to 0,
            ArticlesContract.Category.TITLE to "category",
            ArticlesContract.Category.UNREAD_COUNT to 2
        )
        db.insert(Tables.CATEGORIES, SQLiteDatabase.CONFLICT_NONE, values)

        values = contentValuesOf(
            ArticlesContract.Feed.TITLE to "feed title",
            ArticlesContract.Feed.URL to "feed url",
            ArticlesContract.Feed.CAT_ID to 0,
            ArticlesContract.Feed.UNREAD_COUNT to 2,
            ArticlesContract.Feed.LAST_TIME_UPDATE to 0,
            ArticlesContract.Feed.DISPLAY_TITLE to "display title",
            ArticlesContract.Feed.IS_SUBSCRIBED to 1,
            ArticlesContract.Feed.ICON_URL to "http://exemple.com/1.ico"
        )
        db.insert(Tables.FEEDS, SQLiteDatabase.CONFLICT_NONE, values)

        values = contentValuesOf(
            ArticlesContract.Article._ID to 0,
            ArticlesContract.Article.TITLE to "article title",
            ArticlesContract.Article.CONTENT to "a content",
            ArticlesContract.Article.SCORE to 0,
            ArticlesContract.Article.PUBLISHED to 1,
            ArticlesContract.Article.LAST_TIME_UPDATE to 0,
            ArticlesContract.Article.UNREAD to 1,
            ArticlesContract.Article.TRANSIENT_UNREAD to 1,
            ArticlesContract.Article.STARRED to 1,
            ArticlesContract.Article.IS_UPDATED to 1,
            ArticlesContract.Article.FEED_ID to 1,
            ArticlesContract.Article.LINK to "article links",
            ArticlesContract.Article.TAGS to "article tags",
            ArticlesContract.Article.AUTHOR to "article author",
            ArticlesContract.Article.FLAVOR_IMAGE_URI to "article flavor image uri",
            ArticlesContract.Article.CONTENT_EXCERPT to "a content excerpt"
        )
        db.insert(Tables.ARTICLES, SQLiteDatabase.CONFLICT_NONE, values)

        values = contentValuesOf(
            ArticlesContract.Article._ID to 1,
            ArticlesContract.Article.TITLE to "Comment Linux est devenu un enjeu strategique pour la silicon valley",
            ArticlesContract.Article.CONTENT to """L’emblématique système d’exploitation libre est devenu un outil-clé
                    |de tous les grands groupes du Web, comme l’illustre le rachat de Red Hat. […]""".trimMargin(),
            ArticlesContract.Article.SCORE to 0,
            ArticlesContract.Article.PUBLISHED to 1,
            ArticlesContract.Article.LAST_TIME_UPDATE to 0,
            ArticlesContract.Article.UNREAD to 1,
            ArticlesContract.Article.TRANSIENT_UNREAD to 1,
            ArticlesContract.Article.STARRED to 1,
            ArticlesContract.Article.IS_UPDATED to 1,
            ArticlesContract.Article.FEED_ID to 1,
            ArticlesContract.Article.LINK to "article links",
            ArticlesContract.Article.TAGS to "article tags",
            ArticlesContract.Article.AUTHOR to "article author",
            ArticlesContract.Article.FLAVOR_IMAGE_URI to "article flavor image uri",
            ArticlesContract.Article.CONTENT_EXCERPT to "a content excerpt"
        )
        db.insert(Tables.ARTICLES, SQLiteDatabase.CONFLICT_NONE, values)
    }
}
