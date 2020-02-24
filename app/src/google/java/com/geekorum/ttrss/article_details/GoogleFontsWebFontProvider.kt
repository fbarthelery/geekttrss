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

import dagger.Binds
import dagger.Module
import kotlin.collections.first
import kotlin.let

@Module
abstract class GoogleFontsWebFontProviderModule {

    @Binds
    internal abstract fun bindsWebFontProvider(webFontProvider: GoogleFontsWebFontProvider): WebFontProvider

}


internal class GoogleFontsWebFontProvider @javax.inject.Inject constructor(
    private val application: android.app.Application
) : WebFontProvider {
    companion object {
        const val AUTHORITY = "com.google.android.gms.fonts"
        const val PACKAGE = "com.google.android.gms"
    }

    override fun getFont(font: String): java.io.InputStream? {
        val request = when(font) {
            WebFontProvider.WEB_FONT_ARTICLE_BODY_URL -> androidx.core.provider.FontRequest(AUTHORITY, PACKAGE, "Lora", com.geekorum.ttrss.R.array.com_google_android_gms_fonts_certs)
            else -> null
        }
        val fontUri = request?.let { getFontUri(it) }
        return fontUri?.let { application.contentResolver.openInputStream(fontUri) }
    }

    private fun getFontUri(fontRequest: androidx.core.provider.FontRequest): android.net.Uri? {
        val fontFamilyResult = androidx.core.provider.FontsContractCompat.fetchFonts(application, null, fontRequest)
        return if (fontFamilyResult.statusCode == android.provider.FontsContract.FontFamilyResult.STATUS_OK) {
            fontFamilyResult.fonts.first().uri
        } else {
            timber.log.Timber.w("Unable to fetch font $fontRequest. Error ${fontFamilyResult.statusCode}")
            null
        }
    }
}
