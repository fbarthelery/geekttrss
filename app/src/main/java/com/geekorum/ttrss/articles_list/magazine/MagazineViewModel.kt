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
package com.geekorum.ttrss.articles_list.magazine

import androidx.core.text.parseAsHtml
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.geekorum.geekdroid.accounts.SyncInProgressLiveData
import com.geekorum.ttrss.articles_list.FeedsRepository
import com.geekorum.ttrss.background_job.BackgroundJobManager
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.session.SessionActivityComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class MagazineViewModel @Inject constructor (
    private val state: SavedStateHandle,
    private val backgroundJobManager: BackgroundJobManager,
    private val feedsRepository: FeedsRepository,
    componentFactory: SessionActivityComponent.Factory
): ViewModel() {
    private val component = componentFactory.newComponent()

    private val account = component.account

    private val articlesRepository = component.articleRepository
    private val setFieldActionFactory = component.setArticleFieldActionFactory

    private val articleIds = flow {
        val ids = (getRecentArticlesIds() + getUnreadArticlesIds())
            .distinct()
            .take(20)
        emit(ids)
    }

    private val getNewMagazineChannel = Channel<Unit>(Channel.CONFLATED)
    private val refreshMagazine = getNewMagazineChannel.receiveAsFlow()

    init {
        getNewMagazineChannel.trySend(Unit)
    }

    val isRefreshing: LiveData<Boolean> = SyncInProgressLiveData(account, ArticlesContract.AUTHORITY)

    val articles: Flow<PagingData<ArticleWithFeed>> = refreshMagazine.flatMapLatest {
        Timber.i("Get new magazine articles")
        articleIds.flatMapLatest { ids ->
            val config = PagingConfig(pageSize = 20)
            val pager = Pager(config) {
                articlesRepository.getArticlesById(ids)
            }
            pager.flow
        }
    }.map(::prepareArticlePagingData)
        .cachedIn(viewModelScope)

    private val recentUnreadArticleIds = flow {
        val freshTimeSec = System.currentTimeMillis() / 1000 - 3600 * 36
        val articlesByFeed = getRecentFeedIds().map { feedId ->
            articlesRepository.getAllUnreadArticlesForFeedUpdatedAfterTimeRandomized(feedId, freshTimeSec)
        }.filter {
            it.isNotEmpty()
        }

        val maxArticlesByFeed = articlesByFeed.maxOfOrNull { it.size } ?: 0
        for (i in 0 until maxArticlesByFeed) {
            for (articles in articlesByFeed) {
                articles.getOrNull(i)?.let {
                    emit(it.id)
                }
            }
        }
    }

    private suspend fun getRecentArticlesIds(): List<Long> {
        return recentUnreadArticleIds.take(15).toList()
    }


    private suspend fun getRecentFeedIds(): List<Long> {
        return feedsRepository.allFeeds.map { feeds -> feeds.map { it.feed.id } }
            .firstOrNull()?.shuffled() ?: emptyList()
    }

    private suspend fun getUnreadArticlesIds(): List<Long> {
        return articlesRepository.getUnreadArticlesRandomized(10)
            .map { (article, _) -> article.id }
    }

    fun setArticleUnread(articleId: Long, newValue: Boolean) {
        val action = setFieldActionFactory.createSetUnreadAction(viewModelScope, articleId, newValue)
        action.execute()
    }

    fun setArticleStarred(articleId: Long, newValue: Boolean) {
        val action = setFieldActionFactory.createSetStarredAction(viewModelScope, articleId, newValue)
        action.execute()
    }

    fun refreshMagazine() = viewModelScope.launch {
        getNewMagazineChannel.send(Unit)
    }

    fun refreshFeeds() = viewModelScope.launch {
        backgroundJobManager.refresh(account)
    }

    private fun prepareArticlePagingData(pagingData: PagingData<ArticleWithFeed>) =
        pagingData.map { articleWithFeed ->
            articleWithFeed.copy(
                article = articleWithFeed.article.copy(
                    contentData = articleWithFeed.article.contentData.copy(
                        title = articleWithFeed.article.contentData.title.parseAsHtml().toString()
                    )
                )
            )
        }
}
