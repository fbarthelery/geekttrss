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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.geekdroid.dagger.DaggerDelegateSavedStateVMFactory
import com.geekorum.geekdroid.views.doOnApplyWindowInsets
import com.geekorum.geekdroid.views.recyclerview.ItemSwiper
import com.geekorum.geekdroid.views.recyclerview.ScrollFromBottomAppearanceItemAnimator
import com.geekorum.ttrss.R
import com.geekorum.ttrss.core.BaseFragment
import com.geekorum.ttrss.core.activityViewModels
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.databinding.FragmentArticleListBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Display all the articles in a list.
 */
abstract class BaseArticlesListFragment(
    savedStateVmFactoryCreator: DaggerDelegateSavedStateVMFactory.Creator
) : BaseFragment(savedStateVmFactoryCreator) {

    private lateinit var binding: FragmentArticleListBinding
    private lateinit var adapter: SwipingArticlesListAdapter

    protected abstract val articlesViewModel: BaseArticlesViewModel
    private val activityViewModel: ActivityViewModel by activityViewModels()

    private val unreadSnackbar: Snackbar by lazy {
        Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG).apply {
            addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    articlesViewModel.commitSetUnreadActions()
                }
            })
            setAction(R.string.undo_set_articles_read_btn) { view ->
                articlesViewModel.undoSetUnreadActions()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArticleListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        setupRecyclerView(binding.articleList, binding.swipeRefreshContainer)
        return binding.root
    }

    private fun setupEdgeToEdge() {
        binding.articleList.doOnApplyWindowInsets { view, insets, padding ->
            view.updatePadding(bottom = padding.bottom + insets.systemWindowInsetBottom)
            insets
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupEdgeToEdge()
        articlesViewModel.articles.observe(this) { articles -> adapter!!.submitList(articles) }

        articlesViewModel.getPendingArticlesSetUnread().observe(viewLifecycleOwner) { nbArticles ->
            if (nbArticles > 0) {
                updateUnreadSnackbar(nbArticles)
            }
        }

        activityViewModel.refreshClickedEvent.observe(viewLifecycleOwner, EventObserver {
            articlesViewModel.refresh()
        })

        activityViewModel.mostRecentSortOrder.observe(viewLifecycleOwner) {
            articlesViewModel.setSortByMostRecentFirst(it)
        }

        activityViewModel.onlyUnreadArticles.observe(viewLifecycleOwner) {
            articlesViewModel.setNeedUnread(it)
        }

        binding.fragmentViewModel = articlesViewModel
    }

    private fun updateUnreadSnackbar(nbArticles: Int) {
        val text = resources.getQuantityString(R.plurals.undo_set_articles_read_text, nbArticles,
            nbArticles)
        unreadSnackbar.setText(text)
        unreadSnackbar.show()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView, swipeRefresh: SwipeRefreshLayout) {
        val eventHandler = ArticleEventHandler(requireActivity())
        adapter = SwipingArticlesListAdapter(layoutInflater, eventHandler)
        recyclerView.adapter = adapter
        recyclerView.setupCardSpacing()

        swipeRefresh.setOnRefreshListener {
            articlesViewModel.refresh()
            // leave the progress at least 1s, then refresh its value
            // So if the user trigger a refresh but no sync operation is launch (eg: because of no connectivity)
            // the SwipeRefreshLayout will come back to original status
            viewLifecycleOwner.lifecycleScope.launch {
                delay(1000)
                swipeRefresh.isRefreshing = articlesViewModel.isRefreshing.value!!
            }
        }
        recyclerView.itemAnimator = ScrollFromBottomAppearanceItemAnimator(recyclerView, DefaultItemAnimator())
        val changeReadSwiper = ChangeReadSwiper(requireContext())
        changeReadSwiper.attachToRecyclerView(recyclerView)
    }

    private inner class SwipingArticlesListAdapter(
        layoutInflater: LayoutInflater, eventHandler: CardEventHandler
    ) : ArticlesListAdapter(layoutInflater, eventHandler), ChangeReadDecoration.ArticleProvider {

        override fun getArticle(item: RecyclerView.ViewHolder): Article? {
            val position = item.adapterPosition
            return if (position != RecyclerView.NO_POSITION) {
                getItem(position)
            } else null
        }
    }

    private inner class ArticleEventHandler(context: Context) : CardEventHandler(context) {

        override fun onCardClicked(card: View, article: Article, position: Int) {
            activityViewModel.displayArticle(position, article)
        }

        override fun onStarChanged(article: Article, newValue: Boolean) {
            articlesViewModel.setArticleStarred(article.id, newValue)
        }

        override fun onShareClicked(article: Article) {
            startActivity(createShareIntent(requireActivity(), article))
        }

        override fun onMenuToggleReadSelected(article: Article) {
            articlesViewModel.setArticleUnread(article.id, !article.isTransientUnread)
        }

        override fun onOpenButtonClicked(button: View, article: Article) {
            activityViewModel.displayArticleInBrowser(context, article)
        }
    }


    private inner class ChangeReadSwiper(
        context: Context
    ) : ItemSwiper(ItemTouchHelper.START or ItemTouchHelper.END) {

        private val decoration: ChangeReadDecoration = ChangeReadDecoration(context, adapter)

        init {
            setOnSwipingListener(decoration)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            adapter.getArticle(viewHolder)?.let {
                articlesViewModel.setArticleUnread(it.id, !it.isTransientUnread)
            }
        }

        override fun attachToRecyclerView(recyclerView: RecyclerView) {
            super.attachToRecyclerView(recyclerView)
            recyclerView.addItemDecoration(decoration)
        }

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.3f
    }

}

class ArticlesListFragment @Inject constructor(
    savedStateVmFactoryCreator: DaggerDelegateSavedStateVMFactory.Creator
) : BaseArticlesListFragment(savedStateVmFactoryCreator) {
    override val articlesViewModel: BaseArticlesViewModel by viewModels<ArticlesListViewModel>()
}

class ArticlesListByTagFragment @Inject constructor(
    savedStateVmFactoryCreator: DaggerDelegateSavedStateVMFactory.Creator
) : BaseArticlesListFragment(savedStateVmFactoryCreator) {
    override val articlesViewModel: BaseArticlesViewModel by viewModels<ArticlesListByTagViewModel>()
}