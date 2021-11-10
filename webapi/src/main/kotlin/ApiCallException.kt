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
package com.geekorum.ttrss.webapi

import com.geekorum.ttrss.webapi.model.ResponsePayload
import com.geekorum.ttrss.webapi.model.ResponsePayload.Companion.API_STATUS_OK
import com.geekorum.ttrss.webapi.model.Error

/**
 * Exception raised when an Tiny Tiny Rss API call fail.
 */
class ApiCallException : Exception {

    val errorCode: ApiError?

    enum class ApiError {
        NO_ERROR,
        API_DISABLED,
        API_UNKNOWN,
        LOGIN_FAILED,
        API_INCORRECT_USAGE,
        NOT_LOGGED_IN,
        API_FEED_NOT_FOUND,
        API_UNKNOWN_METHOD
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
        errorCode = null
    }

    constructor(errorCode: ApiError, message: String) : super("$message error code $errorCode") {
        this.errorCode = errorCode
    }
}

@get:JvmName("getErrorFromPayload")
val ResponsePayload<*>.error: ApiCallException.ApiError?
    get() = when (content.error) {
        null, Error.NO_ERROR -> null
        Error.LOGIN_ERROR -> ApiCallException.ApiError.LOGIN_FAILED
        Error.API_DISABLED -> ApiCallException.ApiError.API_DISABLED
        Error.NOT_LOGGED_IN -> ApiCallException.ApiError.NOT_LOGGED_IN
        Error.INCORRECT_USAGE -> ApiCallException.ApiError.API_INCORRECT_USAGE
        Error.FEED_NOT_FOUND -> ApiCallException.ApiError.API_FEED_NOT_FOUND
        Error.UNKNOWN_METHOD -> ApiCallException.ApiError.API_UNKNOWN_METHOD
        Error.API_UNKNOWN -> ApiCallException.ApiError.API_UNKNOWN
    }

/**
 * Throws an [ApiCallException] with the result of calling [lazyMessage]  if [ResponsePayload.status] is not OK.
 */
@JvmOverloads
@Throws(ApiCallException::class)
fun ResponsePayload<*>.checkStatus(lazyMessage: () -> String = { "Api call failed" }) {
    if (status != API_STATUS_OK) {
        val message = lazyMessage()
        throw ApiCallException(error ?: ApiCallException.ApiError.API_UNKNOWN, message)
    }
}
