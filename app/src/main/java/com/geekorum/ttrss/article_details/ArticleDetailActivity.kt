/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2021 by Frederic-Charles Barthelery.
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
import android.app.assist.AssistContent
import android.content.ContentUris
import android.os.Bundle
import android.webkit.WebViewClient
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import com.geekorum.ttrss.articles_list.ArticleListActivity
import com.geekorum.ttrss.session.SessionActivity
import com.geekorum.ttrss.ui.AppTheme
import com.google.accompanist.insets.ProvideWindowInsets
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * An activity representing a single Article detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [ArticleListActivity].
 */
@AndroidEntryPoint
class ArticleDetailActivity : SessionActivity() {

    private val articleDetailsViewModel: ArticleDetailsViewModel by viewModels()
    private lateinit var webViewClient: WebViewClient
    @Inject lateinit var okHttpClient: OkHttpClient
    @Inject lateinit var webFontProvider: WebFontProvider

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webViewClient = ArticleDetailsWebViewClient(okHttpClient, webFontProvider,
            openUrlInBrowser = articleDetailsViewModel::openUrlInBrowser,
            onPageFinishedCallback = { _, _ -> }
        )

        setUpEdgeToEdge()
        val articleUri = requireNotNull(intent.data)
        articleDetailsViewModel.init(ContentUris.parseId(articleUri))

        setContent {
            ProvideWindowInsets {
                AppTheme {
                    ArticleDetailsScreen(articleDetailsViewModel,
                        webViewClient = webViewClient,
                        onNavigateUpClick = {
                            onSupportNavigateUp()
                        })
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun setUpEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onProvideAssistContent(outContent: AssistContent) {
        super.onProvideAssistContent(outContent)
        outContent.webUri = articleDetailsViewModel.article.value?.link?.toUri()
    }
}
