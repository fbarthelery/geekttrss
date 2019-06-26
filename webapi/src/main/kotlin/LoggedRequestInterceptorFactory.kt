/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2019 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.webapi

import com.geekorum.ttrss.webapi.model.LoggedRequestPayload
import okhttp3.RequestBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.util.logging.Level
import java.util.logging.Logger

/**
 * This [Converter.Factory] intercept all RequestBody conversion for [LoggedRequestPayload] and its subclasses
 * and set the [LoggedRequestPayload.sessionId] .
 */
// Don't @Inject it otherwise it can't be an Optional binding
class LoggedRequestInterceptorFactory(
    private val tokenRetriever: TokenRetriever
) : Converter.Factory() {



    override fun requestBodyConverter(
        type: Type, parameterAnnotations: Array<out Annotation>, methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        return when (type) {
            is Class<*> -> {
                if (LoggedRequestPayload::class.java.isAssignableFrom(type)) {
                    return Converter<LoggedRequestPayload, RequestBody> {
                        val delegate: Converter<LoggedRequestPayload, RequestBody> =
                            retrofit.nextRequestBodyConverter(this, type, parameterAnnotations, methodAnnotations)
                        try {
                            it.sessionId = tokenRetriever.getToken()
                        } catch (e: TokenRetriever.RetrieverException) {
                            logger.log(Level.CONFIG, "unable to retrieve token", e)
                        }
                        delegate.convert(it)
                    }
                }
                return null
            }
            else -> null
        }
    }

    companion object {
        private val logger by lazy {
            Logger.getLogger(LoggedRequestInterceptorFactory::class.java.name)
        }
    }
}
