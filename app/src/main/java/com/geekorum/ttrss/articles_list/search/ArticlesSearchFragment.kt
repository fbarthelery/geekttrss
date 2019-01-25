/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2018 by Frederic-Charles Barthelery.
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
import androidx.core.app.ShareCompat
import androidx.lifecycle.Observer
import com.geekorum.ttrss.BaseFragment
import com.geekorum.ttrss.activityViewModels
import com.geekorum.ttrss.articles_list.ActivityViewModel
import com.geekorum.ttrss.articles_list.ArticlesListAdapter
import com.geekorum.ttrss.articles_list.CardEventHandler
import com.geekorum.ttrss.articles_list.setupCardSpacing
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.databinding.FragmentArticlesSearchBinding
import com.geekorum.ttrss.viewModels

/**
 * Display search results
 */
class ArticlesSearchFragment : BaseFragment() {

    lateinit var binding: FragmentArticlesSearchBinding
    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val searchViewModel: SearchViewModel by viewModels()
    private lateinit var adapter: ArticlesListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentArticlesSearchBinding.inflate(inflater, container, false)
        binding.setLifecycleOwner(this)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activityViewModel.searchQuery.observe(this, Observer {
            searchViewModel.setSearchQuery(it)
        })
        searchViewModel.articles.observe(this, Observer {
            adapter.submitList(it)
        })
    }

    inner class EventHandler : CardEventHandler(requireContext()) {
        override fun onCardClicked(card: View, article: Article, position: Int) {
            activityViewModel.displayArticle(position, article)
        }

        override fun onStarChanged(article: Article, newValue: Boolean) {
            searchViewModel.setArticleStarred(article.id, newValue)
        }

        override fun onShareClicked(article: Article) {
            val shareIntent = ShareCompat.IntentBuilder.from(requireActivity())
            shareIntent.setSubject(article.title)
                .setHtmlText(article.content)
                .setText(article.link)
                .setType("text/plain")
            startActivity(shareIntent.createChooserIntent())
        }

        override fun onMenuToggleReadSelected(article: Article) {
            searchViewModel.setArticleUnread(article.id, !article.isTransientUnread)
        }
    }

}
