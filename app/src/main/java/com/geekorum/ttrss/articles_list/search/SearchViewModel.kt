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
package com.geekorum.ttrss.articles_list.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.geekorum.ttrss.articles_list.ArticlesRepository
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.session.SessionActivityComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private val ARG_QUERY = "query"

@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    componentFactory: SessionActivityComponent.Factory
) : ViewModel() {

    val sessionActivityComponent = componentFactory.newComponent()
    private val articlesRepository: ArticlesRepository = sessionActivityComponent.articleRepository
    private val setFieldActionFactory = sessionActivityComponent.setArticleFieldActionFactory

    val articles: Flow<PagingData<ArticleWithFeed>> = Pager(PagingConfig(pageSize = 50)) {
        val searchQuery = savedStateHandle.get<String>(ARG_QUERY)!!
        articlesRepository.searchArticles(searchQuery)
    }.flow
        .cachedIn(viewModelScope)

    fun setArticleStarred(articleId: Long, newValue: Boolean) {
        val action = setFieldActionFactory.createSetStarredAction(viewModelScope, articleId, newValue)
        action.execute()
    }

    fun setArticleUnread(articleId: Long, newValue: Boolean) {
        val action = setFieldActionFactory.createSetUnreadAction(viewModelScope, articleId, newValue)
        action.execute()
    }

}
