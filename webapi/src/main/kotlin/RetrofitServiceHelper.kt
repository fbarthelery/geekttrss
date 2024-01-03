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
package com.geekorum.ttrss.webapi

import com.geekorum.ttrss.webapi.model.ErrorResponsePayload
import com.geekorum.ttrss.webapi.model.ResponsePayload
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException

class RetrofitServiceHelper(
    private val tokenRetriever: TokenRetriever
) {

    @Throws(ApiCallException::class)
    suspend fun <T : ResponsePayload<*>> executeOrFail(failingMessage: String, block: suspend () -> T): T {
        try {
            val body = retryIfInvalidToken(block)
            body.checkStatus { failingMessage }
            return body
        } catch (e: IOException) {
            throw ApiCallException(failingMessage, e)
        } catch (e: HttpException) {
            throw ApiCallException(failingMessage, e)
        }
    }

    suspend fun <T : ResponseBody> getResponseBodyOrFail(failingMessage: String, block: suspend () -> T): T {
        try {
            val responseBody = block()
            val contentType = responseBody.contentType()
            if (contentType?.subtype?.contains("json") == true) {
                responseBody.use {
                    val serializer  = ErrorResponsePayload.serializer()
                    val responsePayload = Json.decodeFromString(serializer, responseBody.string())
                    responsePayload.checkStatus { failingMessage }
                }
            }
            return responseBody
        } catch (e: ApiCallException) {
            val error = e.errorCode
            if (error == ApiCallException.ApiError.LOGIN_FAILED || error == ApiCallException.ApiError.NOT_LOGGED_IN) {
                tokenRetriever.invalidateToken()
            }
            throw e
        } catch (e: IOException) {
            throw ApiCallException(failingMessage, e)
        } catch (e: HttpException) {
            throw ApiCallException(failingMessage, e)
        }
    }

    @Throws(IOException::class)
    suspend fun <T : ResponsePayload<*>> retryIfInvalidToken(block: suspend () -> T): T {
        val body = block()
        try {
            body.checkStatus()
        } catch (e: ApiCallException) {
            val error = e.errorCode
            if (error == ApiCallException.ApiError.LOGIN_FAILED || error == ApiCallException.ApiError.NOT_LOGGED_IN) {
                tokenRetriever.invalidateToken()
                return block()
            }
        }
        return body
    }

}
