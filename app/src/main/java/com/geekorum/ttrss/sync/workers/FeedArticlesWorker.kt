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
package com.geekorum.ttrss.sync.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.geekorum.ttrss.data.ArticleWithAttachments
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.sync.ArticleAugmenter
import com.geekorum.ttrss.webapi.ApiCallException

/**
 * Abstract worker for workers who get articles from the server
 */
abstract class FeedArticlesWorker(
        context: Context,
        workerParams: WorkerParameters,
        syncWorkerComponentBuilder: SyncWorkerComponent.Builder
) : BaseSyncWorker(context, workerParams, syncWorkerComponentBuilder) {

    protected val apiService: ApiService = syncWorkerComponent.apiService

    @Throws(ApiCallException::class)
    protected suspend fun getArticles(
            feedId: Long, sinceId: Long, offset: Int,
            showExcerpt: Boolean = true,
            showContent: Boolean = true,
            includeAttachments: Boolean = false,
            gradually: Boolean = false
    ): List<ArticleWithAttachments> {
        val articles = if (gradually) {
            apiService.getArticlesOrderByDateReverse(feedId,
                    sinceId, offset, showExcerpt, showContent, includeAttachments)
        } else {
            apiService.getArticles(feedId,
                    sinceId, offset, showExcerpt, showContent, includeAttachments)
        }

        if (showContent) {
            articles.forEach {
                augmentArticle(it)
            }
        }
        return articles
    }

    private fun augmentArticle(articleWithAttachments: ArticleWithAttachments): ArticleWithAttachments {
        with(articleWithAttachments.article) {
            val augmenter = ArticleAugmenter(this)
            contentExcerpt = augmenter.getContentExcerpt()
            flavorImageUri = augmenter.getFlavorImageUri()
        }
        return articleWithAttachments
    }

}
