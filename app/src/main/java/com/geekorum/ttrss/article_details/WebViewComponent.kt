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

import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun ArticleContentWebView(
    baseUrl: String,
    content: String,
    modifier: Modifier = Modifier,
    webViewClient: WebViewClient? = null
) {
    val webview = rememberWebViewWithLifecycle()
    AndroidView(
        modifier = modifier,
        factory = {
            webview.apply {
                with(settings) {
                    setSupportZoom(false)
                    javaScriptEnabled = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    mediaPlaybackRequiresUserGesture = false
                }
                if (webViewClient != null) {
                    this.webViewClient = webViewClient
                }
            }
        },
        update = {
            it.loadDataWithBaseURL(baseUrl, content, "text/html", "utf-8", null)
        }
    )
}


@Composable
fun rememberWebViewWithLifecycle(): WebView {
    val context = LocalContext.current
    val webview = remember {
        WebView(context)
    }

    // Makes MapView follow the lifecycle of this composable
    val lifecycleObserver = rememberWebViewLifecycleObserver(webview)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return webview
}

@Composable
private fun rememberWebViewLifecycleObserver(webview: WebView): LifecycleEventObserver =
    remember(webview) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE,
                Lifecycle.Event.ON_START,
                Lifecycle.Event.ON_STOP,
                Lifecycle.Event.ON_DESTROY -> Unit
                Lifecycle.Event.ON_RESUME -> webview.onResume()
                Lifecycle.Event.ON_PAUSE -> webview.onPause()
                else -> throw IllegalStateException()
            }
        }
    }

