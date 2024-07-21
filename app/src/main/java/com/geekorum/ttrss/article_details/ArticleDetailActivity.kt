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
package com.geekorum.ttrss.article_details

import android.app.assist.AssistContent
import android.content.ContentUris
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import com.geekorum.ttrss.R
import com.geekorum.ttrss.articles_list.ArticleListActivity
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.session.SessionActivity
import com.geekorum.ttrss.ui.AppTheme3
import com.geekorum.ttrss.ui.component1
import com.geekorum.ttrss.ui.component2
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ActivityComponent

/**
 * An activity representing a single Article detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [ArticleListActivity].
 */
@AndroidEntryPoint
class ArticleDetailActivity : SessionActivity() {

    private val articleDetailsViewModel: ArticleDetailsViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        val articleUri = requireNotNull(intent.data)
        articleDetailsViewModel.init(ContentUris.parseId(articleUri))

        setContent {
            AppTheme3 {
                val (widthSizeClass, heightSizeClass) = calculateWindowSizeClass(this)

                val webViewClientFactory =  remember {
                    EntryPointAccessors.fromActivity<ArticleDetailsEntryPoint>(this)
                        .articleDetailsWebViewClientFactory
                }
                val webViewClient = remember(articleDetailsViewModel) {
                    webViewClientFactory.create(openUrlInBrowser = articleDetailsViewModel::openUrlInBrowser,
                        onPageFinishedCallback = { _, _ ->})
                }
                ArticleDetailsScreen(articleDetailsViewModel,
                    widthSizeClass = widthSizeClass,
                    heightSizeClass = heightSizeClass,
                    webViewClient = webViewClient,
                    onNavigateUpClick = {
                        // if we are on application backstack we can just call onBackPressed or finish the activity
                        // onSupportNavigateUp() will recreate the previous activity which we want to avoid
                        val upIntent = supportParentActivityIntent
                        if (upIntent != null && supportShouldUpRecreateTask(upIntent)) {
                            onSupportNavigateUp()
                        } else {
                            onBackPressedDispatcher.onBackPressed()
                        }
                    },
                    onArticleClick = {
                        showArticle(it)
                    }
                )
            }
        }
    }

    private fun showArticle(article: Article) {
        finish()
        val intent = Intent(this, ArticleDetailActivity::class.java).apply {
            data = getString(R.string.article_details_data_pattern)
                .replace("{article_id}", article.id.toString())
                .toUri()
        }
        startActivity(intent)
    }

    override fun onProvideAssistContent(outContent: AssistContent) {
        super.onProvideAssistContent(outContent)
        outContent.webUri = articleDetailsViewModel.article.value?.link?.toUri()
    }
}

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ArticleDetailsEntryPoint {
    val articleDetailsWebViewClientFactory: ArticleDetailsWebViewClientFactory
}
