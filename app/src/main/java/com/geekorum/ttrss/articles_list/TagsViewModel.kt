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
package com.geekorum.ttrss.articles_list

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.geekorum.ttrss.session.SessionActivityComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * [ViewModel] for to display the list of tags
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TagsViewModel @ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle,
    componentFactory: SessionActivityComponent.Factory
) : ViewModel() {

    private val articlesRepository: ArticlesRepository = componentFactory.newComponent().articleRepository

    private val refreshTagsChannel = ConflatedBroadcastChannel(Any())
    val tags = refreshTagsChannel.asFlow().flatMapLatest {
        flowOf(articlesRepository.getMostUnreadTags(10))
    }.distinctUntilChanged()
        .asLiveData()

    fun refresh() {
        refreshTagsChannel.offer(Any())
    }

}

