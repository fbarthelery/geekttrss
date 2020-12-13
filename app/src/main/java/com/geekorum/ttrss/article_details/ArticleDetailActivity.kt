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
package com.geekorum.ttrss.article_details

import android.annotation.SuppressLint
import android.content.ContentUris
import android.os.Bundle
import androidx.activity.viewModels
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import com.geekorum.geekdroid.views.doOnApplyWindowInsets
import com.geekorum.ttrss.R
import com.geekorum.ttrss.articles_list.ArticleListActivity
import com.geekorum.ttrss.databinding.ActivityArticleDetailBinding
import com.geekorum.ttrss.databinding.ToolbarArticleDetailsBinding
import com.geekorum.ttrss.session.SessionActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * An activity representing a single Article detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [ArticleListActivity].
 */
@AndroidEntryPoint
class ArticleDetailActivity : SessionActivity() {

    private lateinit var binding: ActivityArticleDetailBinding

    private val articleDetailsViewModel: ArticleDetailsViewModel by viewModels()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_article_detail)
        binding.lifecycleOwner = this
        binding.viewModel = articleDetailsViewModel
        setSupportActionBar(binding.detailToolbar)
        setUpBottomAppBar()
        setUpEdgeToEdge()

        // Show the Up button in the action bar.
        val actionBar = supportActionBar!!
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.setDisplayHomeAsUpEnabled(true)

        val articleUri = requireNotNull(intent.data)
        articleDetailsViewModel.init(ContentUris.parseId(articleUri))
    }

    @SuppressLint("RestrictedApi")
    private fun setUpEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val fabInitialBottomMargin = binding.fab.marginBottom
        binding.root.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            initialPadding.top += insets.top
            initialPadding.applyToView(view)
            // we don't want to apply bottom padding on the whole view group, so we only update fab margin
            binding.fab.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                bottomMargin = fabInitialBottomMargin + insets.bottom
            }
            windowInsets
        }
    }

    private fun setUpBottomAppBar() {
        val toolbarBinding = ToolbarArticleDetailsBinding.inflate(layoutInflater).apply {
            lifecycleOwner = this@ArticleDetailActivity
            viewModel = articleDetailsViewModel
        }
        binding.bottomAppBar.addView(toolbarBinding.root)
    }

}
