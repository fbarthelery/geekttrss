/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2023 by Frederic-Charles Barthelery.
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
import android.app.Application
import com.geekorum.ttrss.R
import java.io.InputStream
import javax.inject.Inject

/**
 * Provides font for [ArticleDetailFragment] WebView.
 */
interface WebFontProvider {

    companion object {
        const val WEB_FONT_ARTICLE_BODY_URL = "webfont://article_body"
    }

    fun isWebFontUrl(url: String) = when (url) {
        WEB_FONT_ARTICLE_BODY_URL -> true
        else -> false
    }

    fun getFont(font: String): InputStream?

}

class ResourcesWebFontProvider @Inject constructor(
    private val application: Application
) : WebFontProvider{

    @SuppressLint("ResourceType") // we explicitly want to load font resource file
    override fun getFont(font: String): InputStream? {
        return when(font) {
            WebFontProvider.WEB_FONT_ARTICLE_BODY_URL -> application.resources.openRawResource(R.font.lora)
            else -> null
        }
    }
}

