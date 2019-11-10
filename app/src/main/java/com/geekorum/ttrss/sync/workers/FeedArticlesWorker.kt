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
package com.geekorum.ttrss.sync.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.sync.ArticleAugmenter
import com.geekorum.ttrss.webapi.ApiCallException

/**
 * Abstract worker for workers who get articles from the server
 */
abstract class FeedArticlesWorker(
        context: Context,
        workerParams: WorkerParameters,
        protected val apiService: ApiService
) : CoroutineWorker(context, workerParams) {

    @Throws(ApiCallException::class)
    protected suspend fun getArticles(
            feedId: Long, sinceId: Long, offset: Int,
            showExcerpt: Boolean = true,
            showContent: Boolean = true,
            gradually: Boolean = false
    ): List<Article> {
        val articles = if (gradually) {
            apiService.getArticlesOrderByDateReverse(feedId,
                    sinceId, offset, showExcerpt, showContent)
        } else {
            apiService.getArticles(feedId,
                    sinceId, offset, showExcerpt, showContent)
        }

        if (showContent) {
            articles.forEach {
                augmentArticle(it)
            }
        }
        return articles
    }

    private fun augmentArticle(article: Article): Article {
        val augmenter = ArticleAugmenter(article)
        article.contentExcerpt = augmenter.getContentExcerpt()
        article.flavorImageUri = augmenter.getFlavorImageUri()
        return article
    }

}
