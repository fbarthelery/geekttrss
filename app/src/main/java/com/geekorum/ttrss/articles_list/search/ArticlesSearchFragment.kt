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
package com.geekorum.ttrss.articles_list.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.geekorum.ttrss.articles_list.ActivityViewModel
import com.geekorum.ttrss.articles_list.ArticlesListAdapter
import com.geekorum.ttrss.articles_list.CardEventHandler
import com.geekorum.ttrss.articles_list.setupCardSpacing
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.databinding.FragmentArticlesSearchBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Display search results
 */
@AndroidEntryPoint
class ArticlesSearchFragment : Fragment() {

    lateinit var binding: FragmentArticlesSearchBinding
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val searchViewModel: SearchViewModel by viewModels()
    private lateinit var adapter: ArticlesListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentArticlesSearchBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        setupRecyclerView()
        return binding.root
    }


    private fun setupRecyclerView() {
        binding.articleList.setupCardSpacing()
        val eventHandler = EventHandler()
        adapter = ArticlesListAdapter(layoutInflater, eventHandler).also {
            binding.articleList.adapter = it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityViewModel.searchQuery.observe(viewLifecycleOwner) {
            searchViewModel.setSearchQuery(it)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.articles.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    inner class EventHandler : CardEventHandler(requireContext()) {

        override fun onOpenButtonClicked(button: View, article: Article) {
            activityViewModel.displayArticleInBrowser(context, article)
        }

        override fun onCardClicked(card: View, article: Article, position: Int) {
            activityViewModel.displayArticle(position, article)
        }

        override fun onStarChanged(article: Article, newValue: Boolean) {
            searchViewModel.setArticleStarred(article.id, newValue)
        }

        override fun onShareClicked(article: Article) {
            startActivity(createShareIntent(requireActivity(), article))
        }

        override fun onMenuToggleReadSelected(article: Article) {
            searchViewModel.setArticleUnread(article.id, !article.isTransientUnread)
        }
    }

}
