/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.logging

import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import timber.log.Timber

/**
 * Log [Retrofit] method calls when installed as an OkHttp [Interceptor]
 */
class RetrofitInvocationLogger : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        request.tag(Invocation::class.java)?.let {
            val method = it.method()
            val args = it.arguments().takeUnless {
                method.name == "login"
            } ?: listOf("Anonymized arguments")
            Timber.tag("Retrofit")
                .d("calling ${method.declaringClass.simpleName}.${method.name} $args")
        }
        return chain.proceed(request)
    }
}
