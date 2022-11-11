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
package com.geekorum.ttrss.data

import android.database.sqlite.SQLiteDatabase
import androidx.core.content.contentValuesOf
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geekorum.ttrss.data.ArticlesDatabase.Tables
import com.geekorum.ttrss.providers.ArticlesContract
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ArticleFullTextSearchTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var database: ArticlesDatabase

    @BeforeTest
    fun beforeTest() {
        Dispatchers.setMain(testDispatcher)
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
            ArticlesDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        createSomeArticles(database.openHelper.writableDatabase)
    }

    @AfterTest
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun testAnFTSSearchPagedList() = runTest {
        val expected = ArticleWithFeed(
            article = Article(
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
        ),
        feed = Feed(id = 1,
            url = "feed url",
            title = "feed title",
            catId = 0,
            displayTitle = "display title",
            lastTimeUpdate = 0,
            unreadCount = 2,
            isSubscribed = true,
            feedIconUrl = "http://exemple.com/1.ico"
        ))

        val articleDao = database.articleDao()
        val differ = AsyncPagingDataDiffer(ARTICLE_DIFF_CALLBACK, NoOpUpdateCallback)
        val pager = Pager(PagingConfig(10)) {
            articleDao.searchArticles("linux")
        }

        val submitJob = launch {
            pager.flow.collectLatest { data ->
                differ.submitData(data)
            }
        }
        // wait for the load: initial Loading, notLoading
        differ.loadStateFlow.takeWhile { it.refresh !is LoadState.NotLoading }.collect()
        submitJob.cancel()

        assertThat(differ.snapshot()).containsExactly(expected)
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

private val ARTICLE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<ArticleWithFeed>() {
    override fun areItemsTheSame(oldItem: ArticleWithFeed, newItem: ArticleWithFeed): Boolean {
        return oldItem.article.id == newItem.article.id
    }

    override fun areContentsTheSame(oldItem: ArticleWithFeed, newItem: ArticleWithFeed): Boolean {
        return oldItem == newItem
    }
}

private val NoOpUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {
    }

    override fun onRemoved(position: Int, count: Int) {
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
    }
}
