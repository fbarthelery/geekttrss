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

import androidx.core.text.parseAsHtml
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.geekorum.geekdroid.accounts.isSyncActiveFlow
import com.geekorum.ttrss.articles_list.FeedsRepository
import com.geekorum.ttrss.background_job.BackgroundJobManager
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.session.SessionActivityComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class MagazineViewModel @Inject constructor (
    private val backgroundJobManager: BackgroundJobManager,
    private val feedsRepository: FeedsRepository,
    private val magazineSelectionStrategy: MagazineSelectionStrategy,
    componentFactory: SessionActivityComponent.Factory
): ViewModel() {
    private val component = componentFactory.newComponent()

    private val account = component.account

    private val articlesRepository = component.articleRepository
    private val setFieldActionFactory = component.setArticleFieldActionFactory

    private val getNewMagazineChannel = Channel<Unit>(Channel.CONFLATED)
    private val refreshMagazine = getNewMagazineChannel.receiveAsFlow()

    init {
        getNewMagazineChannel.trySend(Unit)
    }

    val isRefreshing = isSyncActiveFlow(account, ArticlesContract.AUTHORITY)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val articles: Flow<PagingData<ArticleWithFeed>> = refreshMagazine.flatMapLatest {
        Timber.i("Get new magazine articles")
        val articleIds = magazineSelectionStrategy.getArticlesIds()
        val config = PagingConfig(pageSize = 20)
        val pager = Pager(config) {
            articlesRepository.getArticlesById(articleIds)
        }
        pager.flow
    }.map(::prepareArticlePagingData)
        .cachedIn(viewModelScope)

    private suspend fun getRecentFeedIds(): List<Long> {
        return feedsRepository.allFeeds.map { feeds -> feeds.map { it.feed.id } }
            .firstOrNull()?.shuffled() ?: emptyList()
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
