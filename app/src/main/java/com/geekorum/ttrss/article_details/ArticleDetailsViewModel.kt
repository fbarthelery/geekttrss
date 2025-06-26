/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
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
import androidx.core.net.toUri
import androidx.core.text.parseAsHtml
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geekorum.ttrss.articles_list.ArticlesRepository
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.network.TtRssBrowserLauncher
import com.geekorum.ttrss.session.SessionActivityComponent
import com.geekorum.ttrss.share.createShareArticleIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STATE_ARTICLE_ID = "article_id"

/**
 * [ViewModel] for [ArticleDetailActivity] and [ArticleDetailFragment].
 */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ArticleDetailsViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val browserLauncher: TtRssBrowserLauncher,
    componentFactory: SessionActivityComponent.Factory
) : ViewModel() {

    val sessionActivityComponent = componentFactory.newComponent()
    private val articlesRepository: ArticlesRepository = sessionActivityComponent.articleRepository
    private val setFieldActionFactory = sessionActivityComponent.setArticleFieldActionFactory

    private val articleId = state.getStateFlow<Long>(STATE_ARTICLE_ID, 0L)

    val article = articleId.flatMapLatest {
        articlesRepository.getArticleById(it)
            .map(::prepareArticle)
            .onEach { article ->
                article?.let {
                    browserLauncher.mayLaunchUrl(article.link.toUri())
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val additionalArticles = article
        .map {
            it?.tags ?: ""
        }.distinctUntilChanged()
        .map(::getAdditionalArticlesForTags)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val browserIcon = browserLauncher.browserIcon.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        browserLauncher.warmUp()
    }

    fun init(articleId: Long) {
        state[STATE_ARTICLE_ID] = articleId
    }

    private fun prepareArticle(article: Article?) = article?.copy(
        contentData = article.contentData.copy(
            title = article.contentData.title.parseAsHtml().toString()
        )
    )

    private suspend fun getAdditionalArticlesForTags(tags: String): List<ArticleWithTag> {
        val randomizedTags = tags.split(",")
            .map(String::trim)
            .filter(String::isNotEmpty)
            .shuffled()

        val articles = randomizedTags.map { articlesRepository.getUnreadArticlesForTag(it)
            .filterNot { it.article.id == articleId.value }
            .toMutableList() }
        val articlesPerTag = randomizedTags.zip(articles)
        val result = sequence {
            while (articlesPerTag.any { (_, v) -> v.isNotEmpty() }) {
                for ((tag, values) in articlesPerTag) {
                    val article = values.removeFirstOrNull()?.article
                    if (article != null) {
                        val articleWithCorrectTitle = article.copy(
                            contentData = article.contentData.copy(
                                title = article.contentData.title.parseAsHtml().toString()
                            )
                        )
                        yield(ArticleWithTag(articleWithCorrectTitle, tag))
                    }
                }
            }
        }
            .distinctBy { it.article }
            .take(3)
        return result.toList()
    }

    override fun onCleared() {
        browserLauncher.shutdown()
    }

    fun openArticleInBrowser(context: Context, article: Article) {
        openUrlInBrowser(context, article.link.toUri())
    }

    fun openUrlInBrowser(context: Context, uri: Uri) = browserLauncher.launchUrl(context, uri)

    fun shareArticle(activity: Activity) {
        article.value?.let {
            activity.startActivity(createShareArticleIntent(activity, it))
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
            val action = setFieldActionFactory.createSetStarredAction(viewModelScope, it.id, newValue)
            action.execute()
        }
    }

    fun toggleArticleRead() {
        article.value?.let {
            setArticleUnread(!it.isUnread)
        }
    }

    fun setArticleUnread(unread: Boolean) {
        article.value?.let {
            val action = setFieldActionFactory.createSetUnreadAction(viewModelScope, it.id, unread)
            action.execute()
        }
    }

}

data class ArticleWithTag(
    val article: Article,
    val tag: String
)
