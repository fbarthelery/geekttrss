package com.geekorum.ttrss.articles_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.geekorum.geekdroid.dagger.ViewModelAssistedFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * [ViewModel] for to display the list of tags
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TagsViewModel @AssistedInject constructor(
    @Assisted private val state: SavedStateHandle,
    private val articlesRepository: ArticlesRepository
) : ViewModel() {

    private val refreshTagsChannel = ConflatedBroadcastChannel(Any())
    val tags = refreshTagsChannel.asFlow().flatMapLatest {
        flowOf(articlesRepository.getMostUnreadTags(10))
    }.distinctUntilChanged()
        .asLiveData()

    fun refresh() {
        refreshTagsChannel.offer(Any())
    }

    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<TagsViewModel> {
        override fun create(state: SavedStateHandle): TagsViewModel
    }
}

