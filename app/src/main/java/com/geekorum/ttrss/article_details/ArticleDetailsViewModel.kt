/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2023 by Frederic-Charles Barthelery.
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
import androidx.core.text.parseAsHtml
import androidx.lifecycle.*
import com.geekorum.ttrss.articles_list.ArticlesRepository
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.network.TtRssBrowserLauncher
import com.geekorum.ttrss.session.SessionActivityComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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

    val sessionActivityComponent = componentFactory.newComponent()
    private val articlesRepository: ArticlesRepository = sessionActivityComponent.articleRepository
    private val setFieldActionFactory = sessionActivityComponent.setArticleFieldActionFactory

    private val articleId = state.getLiveData<Long>(STATE_ARTICLE_ID)

    val article: LiveData<Article?> = articleId.switchMap {
        articlesRepository.getArticleById(it)
            .map(::prepareArticle)
            .onEach { article ->
                article?.let {
                    browserLauncher.mayLaunchUrl(article.link.toUri())
                }
            }
            .asLiveData()
    }

    val additionalArticles = article
        .asFlow()
        .map {
            it?.tags ?: ""
        }.distinctUntilChanged()
        .map(::getAdditionalArticlesForTags)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
