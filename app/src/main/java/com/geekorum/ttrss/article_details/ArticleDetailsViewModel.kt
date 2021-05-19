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
package com.geekorum.ttrss.article_details

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.geekorum.ttrss.articles_list.ArticlesRepository
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.network.TtRssBrowserLauncher
import com.geekorum.ttrss.session.SessionActivityComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val STATE_ARTICLE_ID = "article_id"

/**
 * [ViewModel] for [ArticleDetailActivity] and [ArticleDetailFragment].
 */
@HiltViewModel
class ArticleDetailsViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val browserLauncher: TtRssBrowserLauncher,
    componentFactory: SessionActivityComponent.Factory
) : ViewModel() {

    private val articlesRepository: ArticlesRepository = componentFactory.newComponent().articleRepository

    private val articleId = state.getLiveData<Long>(STATE_ARTICLE_ID)

    val article: LiveData<Article?> = articleId.switchMap {
        articlesRepository.getArticleById(it)
            .onEach { article ->
                article?.let {
                    browserLauncher.mayLaunchUrl(article.link.toUri())
                }
            }
            .asLiveData()
    }

    val articleContent: LiveData<String> = article.map { it?.content ?: "" }.distinctUntilChanged()

    init {
        browserLauncher.warmUp()
    }

    fun init(articleId: Long) {
        state[STATE_ARTICLE_ID] = articleId
    }

    override fun onCleared() {
        browserLauncher.shutdown()
    }

    fun openArticleInBrowser(context: Context, article: Article) {
        openUrlInBrowser(context, article.link.toUri())
    }

    fun openUrlInBrowser(context: Context, uri: Uri) = browserLauncher.launchUrl(context, uri)

    fun shareArticle(activity: Activity) {
        val shareIntent = ShareCompat.IntentBuilder(activity)
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
