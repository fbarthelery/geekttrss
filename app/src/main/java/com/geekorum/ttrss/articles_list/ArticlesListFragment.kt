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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.ui.AppTheme
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

/**
 * Display all the articles in a list.
 */
abstract class BaseArticlesListFragment : Fragment() {

    protected abstract val articlesViewModel: BaseArticlesViewModel
    private val activityViewModel: ActivityViewModel by activityViewModels()

    private val unreadSnackbar: Snackbar by lazy {
        Snackbar.make(requireView(), "", Snackbar.LENGTH_LONG).apply {
            addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    articlesViewModel.commitSetUnreadActions()
                }
            })
            setAction(R.string.undo_set_articles_read_btn) {
                articlesViewModel.undoSetUnreadActions()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ProvideWindowInsets {
                    AppTheme {
                        val appBarHeightDp = with(LocalDensity.current) {
                            activityViewModel.appBarHeight.toDp()
                        }
                        Surface(Modifier.fillMaxSize()) {
                            ArticleCardList(
                                viewModel = articlesViewModel,
                                onCardClick = activityViewModel::displayArticle,
                                onShareClick = ::onShareClicked,
                                onOpenInBrowserClick = {
                                    activityViewModel.displayArticleInBrowser(requireContext(), it)
                                },
                                additionalContentPaddingBottom = appBarHeightDp,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    }

    private fun updateUnreadSnackbar(nbArticles: Int) {
        val text = resources.getQuantityString(R.plurals.undo_set_articles_read_text, nbArticles,
            nbArticles)
        unreadSnackbar.setText(text)
        unreadSnackbar.show()
    }

    private fun onShareClicked(article: Article) {
        startActivity(createShareIntent(requireActivity(), article))
    }

    private fun createShareIntent(activity: Activity, article: Article): Intent {
        val shareIntent = ShareCompat.IntentBuilder(activity)
        shareIntent.setSubject(article.title)
            .setHtmlText(article.content)
            .setText(article.link)
            .setType("text/plain")
        return shareIntent.createChooserIntent()
    }

}

@AndroidEntryPoint
class ArticlesListFragment : BaseArticlesListFragment() {
    override val articlesViewModel: BaseArticlesViewModel by viewModels<ArticlesListViewModel>()
}

@AndroidEntryPoint
class ArticlesListByTagFragment : BaseArticlesListFragment() {
    override val articlesViewModel: BaseArticlesViewModel by viewModels<ArticlesListByTagViewModel>()
}
