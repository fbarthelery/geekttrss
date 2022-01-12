/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import com.geekorum.geekdroid.network.OkHttpWebViewClient
import com.geekorum.ttrss.R
import okhttp3.OkHttpClient
import timber.log.Timber

/**
 * WebViewClient for ArticleDetails webview.
 * It favors opening link in their default application and fallback to a browser if none.
 * It also use  WebFontProvider to load fonts
 */
class ArticleDetailsWebViewClient constructor(
    okHttpClient: OkHttpClient,
    private val webFontProvider: WebFontProvider,
    private val openUrlInBrowser: (Context, Uri) -> Unit,
    private val onPageFinishedCallback: (WebView?, String?) -> Unit,
) : OkHttpWebViewClient(okHttpClient) {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val context = view.context
        try {
            openInNonBrowserApp(context, request.url)
        } catch (e: ActivityNotFoundException) {
            // Only browser apps are available, or a browser is the default.
            // So you can open the URL directly in your app, for example in a
            // Custom Tab.
            openUrlInBrowser(context, request.url)
        }
        return true
    }

    private fun openInNonBrowserApp(context: Context, url: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            openInNonBrowserAppAfter30(context, url)
        } else {
            openInNonBrowserAppBefore30(context, url)
        }
    }

    private fun openInNonBrowserAppBefore30(context: Context, url: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, url)
        val activities =
            context.packageManager.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER)
                .filterNot {
                    it.filter.isWebBrowserApp()
                }
        if (activities.isNotEmpty()) {
            context.startActivity(intent)
        } else {
            throw ActivityNotFoundException()
        }
    }

    private fun openInNonBrowserAppAfter30(context: Context, url: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, url).apply {
            // The URL should either launch directly in a non-browser app (if it's
            // the default), or in the disambiguation dialog.
            addCategory(Intent.CATEGORY_BROWSABLE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
        }
        context.startActivity(intent)
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val url = request.url.toString()
        if (webFontProvider.isWebFontUrl(url)) {
            Timber.d("Intercept url for webfont $url")
            return webFontProvider.getFont(url)?.let {
                WebResourceResponse("application/octet-stream", null, it).apply {
                    responseHeaders = mapOf("Access-Control-Allow-Origin"  to "*")
                }
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageFinishedCallback(view, url)
    }

    private fun IntentFilter.isWebBrowserApp(): Boolean {
        // from hidden method IntentFilter.handleAllWebDataURI()
        return hasCategory(Intent.CATEGORY_APP_BROWSER) || handlesWebUris() && countDataAuthorities() == 0
    }

    private fun IntentFilter.handlesWebUris(): Boolean {
        // adapted from hidden method IntentFilter.handleWebUris()
        val nbDataSchemes = countDataSchemes()
        // Require ACTION_VIEW, CATEGORY_BROWSEABLE, and at least one scheme
        if (!hasAction(Intent.ACTION_VIEW)
            || !hasCategory(Intent.CATEGORY_BROWSABLE)
            || nbDataSchemes == 0
        ) {
            return false
        }

        // Now allow only the schemes "http" and "https"
        for (i in 0 until nbDataSchemes) {
            val scheme = getDataScheme(i)
            val isWebScheme = "http" == scheme || "https" == scheme
            if (isWebScheme) {
                return true
            }
        }
        return false
    }

}

data class WebViewColors(
    @field:ColorInt
    val backgroundColor: Int,
    @field:ColorInt
    internal val textColor: Int,
    @field:ColorInt
    internal val linkColor: Int
) {

    companion object {
        fun fromTheme(theme: Resources.Theme): WebViewColors {
            val typedValue = TypedValue()
            theme.resolveAttribute(R.attr.articleBackground, typedValue, true)
            val backgroundColor = toColorInt(typedValue, theme)
            theme.resolveAttribute(R.attr.articleTextColor, typedValue, true)
            val textColor = toColorInt(typedValue, theme)
            theme.resolveAttribute(R.attr.linkColor, typedValue, true)
            val linkColor = toColorInt(typedValue, theme)
            return WebViewColors(backgroundColor, textColor, linkColor)
        }

        @ColorInt
        private fun toColorInt(typedValue: TypedValue, theme: Resources.Theme ): Int {
            return when (typedValue.type) {
                in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT -> typedValue.data
                TypedValue.TYPE_REFERENCE -> ResourcesCompat.getColor(theme.resources, typedValue.data, theme)
                TypedValue.TYPE_STRING -> ResourcesCompat.getColor(theme.resources, typedValue.resourceId, theme)
                TypedValue.TYPE_ATTRIBUTE -> {
                    theme.resolveAttribute(typedValue.data, typedValue, true)
                    toColorInt(typedValue, theme)
                }
                else -> throw IllegalArgumentException("Theme attribute expected to be a color")
            }
        }

    }
}
