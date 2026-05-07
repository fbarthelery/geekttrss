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
package com.geekorum.ttrss.articles_list

import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.geekorum.geekdroid.accounts.SyncInProgressLiveData
import com.geekorum.ttrss.background_job.BackgroundJobManager
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.providers.ArticlesContract
import com.geekorum.ttrss.session.SessionActivityComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = ArticlesListForCategoryViewModel.Factory::class)
class ArticlesListForCategoryViewModel @AssistedInject constructor(
    @Assisted val catId: Long,
    private val backgroundJobManager: BackgroundJobManager,
    componentFactory: SessionActivityComponent.Factory
) : BaseArticlesViewModel(componentFactory) {

    @AssistedFactory
    interface Factory {
        fun create(catId: Long): ArticlesListForCategoryViewModel
    }

    override val isMultiFeed: StateFlow<Boolean> = MutableStateFlow(true)

    private val account = component.account

    override val articles: Flow<PagingData<ArticleWithFeed>> =
        sortByMostRecentFirst.combine(needUnread) { mostRecentFirst, needUnread ->
            getArticleAccess(mostRecentFirst, needUnread)
        }.flatMapLatest { access ->
            Pager(PagingConfig(pageSize = 50)) {
                access.articlesForCategory(catId)
            }.flow
        }
        .map(::prepareArticlePagingData)
        .cachedIn(viewModelScope)

    override val isRefreshing: StateFlow<Boolean> =
        SyncInProgressLiveData(account, ArticlesContract.AUTHORITY).asFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    override fun refresh() {
        if (isRefreshing.value) return
        backgroundJobManager.refresh(account)
    }
}
