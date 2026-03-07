/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.articles_list.magazine

import com.geekorum.ttrss.articles_list.ArticlesRepository
import com.geekorum.ttrss.articles_list.FeedsRepository
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.Feed
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Strategy used to select what articles to display in the [MagazineScreen]
 */
interface MagazineSelectionStrategy {
    suspend fun getArticlesIds(): List<Long>
}


/**
 * Use RANDOM() in SQL queries in Room to get some random unread articles
 */
//TODO once room issue with RANDOM() is fixed return to previous implementation
// see  https://issuetracker.google.com/issues/413924560
class RandomRoomMagazineSelectionStrategy @Inject constructor(
    private val clock: Clock,
    private val feedsRepository: FeedsRepository,
    private val articlesRepository: ArticlesRepository,
): MagazineSelectionStrategy{

    override suspend fun getArticlesIds(): List<Long> {
        val ids = (getRecentUnreadArticlesIds() + getUnreadArticlesIds())
            .distinct()
            .take(20)
        return ids
    }

    private suspend fun getRecentUnreadArticlesIds(): List<Long> {
        val freshTimeLowLimit = clock.now() - 36.hours
        val articlesByFeed = getRecentFeedIds().map { feedId ->
            articlesRepository.getAllUnreadArticlesForFeedUpdatedAfterTimeRandomized(feedId, freshTimeLowLimit.epochSeconds)
        }.filter {
            it.isNotEmpty()
        }

        val articlesIdsSequence = articlesIdGenerator(articlesByFeed)
        return articlesIdsSequence.take(15).toList()
    }

    private fun articlesIdGenerator(articlesByFeed: List<List<Article>>) = sequence {
        val feedIterator = articlesByFeed.map { it.iterator() }
        while (feedIterator.any { it.hasNext() }) {
            for (feed in feedIterator) {
                if (feed.hasNext()) {
                    yield(feed.next().id)
                }
            }
        }
    }

    private suspend fun getRecentFeedIds(): List<Long> {
        return feedsRepository.allFeeds.map { feeds ->
            feeds.filterNot { Feed.isVirtualFeed(it.feed.id) }
                .map { it.feed.id }
        }.firstOrNull()?.shuffled() ?: emptyList()
    }


    private suspend fun getUnreadArticlesIds(): List<Long> {
        return articlesRepository.getUnreadArticlesRandomized(10)
            .map { (article, _) -> article.id }
    }
}


/**
 * Randomize the articles in software to get some random unread articles
 *
 * This is a workaround for https://issuetracker.google.com/issues/413924560
 * who prevents us to use [RandomRoomMagazineSelectionStrategy]
 */
class SoftwareRandomMagazineSelectionStrategy @Inject constructor(
    private val clock: Clock,
    private val feedsRepository: FeedsRepository,
    private val articlesRepository: ArticlesRepository,
): MagazineSelectionStrategy{

    override suspend fun getArticlesIds(): List<Long> {
        val ids = (getRecentUnreadArticlesIds() + getUnreadArticlesIds())
            .distinct()
            .take(20)
        return ids
    }

    private suspend fun getRecentUnreadArticlesIds(): List<Long> {
        val freshTimeLowLimit = clock.now() - 36.hours
        val articlesByFeed = getRecentFeedIds().map { feedId ->
            articlesRepository.getAllUnreadArticlesForFeedUpdatedAfterTime(feedId, freshTimeLowLimit.epochSeconds)
        }.filter {
            it.isNotEmpty()
        }

        val articlesIdsSequence = articlesIdGenerator(articlesByFeed)
        return articlesIdsSequence.take(15).toList().also {
            Timber.i("selected ${it.size} recent unread")
        }
    }

    private fun articlesIdGenerator(articlesByFeed: List<List<Article>>) = sequence {
        val totalNbArticles = articlesByFeed.sumOf { it.size }
        val emittedIds = mutableSetOf<Long>()
        while (emittedIds.size < totalNbArticles) {
            for (articles in articlesByFeed) {
                val next = articles.random()
                if (next.id !in emittedIds) {
                    emittedIds.add(next.id)
                    yield(next.id)
                }
            }
        }
    }

    private suspend fun getRecentFeedIds(): List<Long> {
        return feedsRepository.allFeeds.map { feeds ->
            feeds.filterNot { Feed.isVirtualFeed(it.feed.id) }
                .map { it.feed.id }
        }.firstOrNull()?.shuffled() ?: emptyList()
    }


    private suspend fun getUnreadArticlesIds(): List<Long> {
        return articlesRepository.getUnreadArticles(100)
            .map { (article, _) -> article.id }
            .shuffled()
            .take(10).also {
                Timber.i("selected ${it.size} unread")
            }
    }
}



