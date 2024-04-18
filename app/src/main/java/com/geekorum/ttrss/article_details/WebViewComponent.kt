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

import android.webkit.WebSettings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.geekorum.ttrss.ui.components.web.AccompanistWebViewClient
import com.geekorum.ttrss.ui.components.web.WebView
import com.geekorum.ttrss.ui.components.web.rememberWebViewStateWithHTMLData

@Composable
fun ArticleContentWebView(
    baseUrl: String,
    content: String,
    webViewClient: AccompanistWebViewClient,
    modifier: Modifier = Modifier
) {
    val state = rememberWebViewStateWithHTMLData(data = content, baseUrl = baseUrl)

    WebView(state = state,
        modifier = modifier,
        captureBackPresses = false,
        client = webViewClient,
        onCreated = {
            // webview doesn't clip its outline and thus its content overflow
            // over other content in the ComposeView
            // See  https://issuetracker.google.com/issues/242463987
            it.clipToOutline = true
            with(it.settings) {
                setSupportZoom(false)
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                mediaPlaybackRequiresUserGesture = false
            }
        }
    )
}
