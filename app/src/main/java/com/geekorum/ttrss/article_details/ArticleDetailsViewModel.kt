/*
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
package com.geekorum.ttrss.article_details

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.geekorum.ttrss.articles_list.ArticlesRepository
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.network.TtRssBrowserLauncher
import javax.inject.Inject

/**
 * [ViewModel] for [ArticleDetailActivity] and [ArticleDetailFragment].
 */
class ArticleDetailsViewModel
@Inject constructor(
    private val articlesRepository: ArticlesRepository,
    private val browserLauncher: TtRssBrowserLauncher
) : ViewModel() {

    private val articleId = MutableLiveData<Long>()

    val article: LiveData<Article?> = articleId.switchMap { articlesRepository.getArticleById(it) }
        .map {
            if (it != null) {
                browserLauncher.mayLaunchUrl(it.link.toUri())
            }
            it
        }

    val articleContent: LiveData<String> = article.distinctUntilChanged().map { it?.content ?: "" }

    init {
        browserLauncher.warmUp()
    }

    fun init(articleId: Long) {
        this.articleId.value = articleId
    }

    override fun onCleared() {
        browserLauncher.shutdown()
    }

    fun openArticleInBrowser(context: Context, article: Article) {
        openUrlInBrowser(context, article.link.toUri())
    }

    fun openUrlInBrowser(context: Context, uri: Uri) = browserLauncher.launchUrl(context, uri)

    fun shareArticle(activity: Activity) {
        val shareIntent = ShareCompat.IntentBuilder.from(activity)
        article.value?.let {
            shareIntent.setSubject(it.title)
                .setHtmlText(it.content)
                .setText(it.link)
                .setType("text/plain")
            activity.startActivity(shareIntent.createChooserIntent())
        }
    }

    // for use from databinding layout
    fun shareArticle(context: Context) {
        val activity = context.findActivity()
        shareArticle(activity)
    }

    private fun Context.findActivity(): Activity {
        var context = this
        while (true) {
            context = when (context) {
                is Activity -> return context
                is ContextWrapper -> context.baseContext
                else -> throw IllegalArgumentException("Context is not an activity")
            }
        }
    }

    fun onStarChanged(newValue: Boolean) {
        article.value?.let {
            articlesRepository.setArticleStarred(it.id, newValue)
        }
    }

    fun toggleArticleRead() {
        article.value?.let {
            setArticleUnread(!it.isUnread)
        }
    }

    fun setArticleUnread(unread: Boolean) {
        article.value?.let {
            articlesRepository.setArticleUnread(it.id, unread)
        }
    }
}
