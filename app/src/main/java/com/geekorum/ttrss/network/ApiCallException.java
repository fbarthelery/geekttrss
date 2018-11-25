/**
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2018 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.network;

/**
 * Exception raised when an Tiny Tiny Rss API call fail.
 */
public class ApiCallException extends Exception {

    public enum ApiError { NO_ERROR, API_DISABLED,
        API_UNKNOWN, LOGIN_FAILED, API_INCORRECT_USAGE, NOT_LOGGED_IN, API_UNKNOWN_METHOD }


    private ApiError errorCode;

    public ApiCallException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiCallException(ApiError errorCode, String message) {
        super(message + " error code " + errorCode);
        this.errorCode = errorCode;
    }


    public ApiError getErrorCode() {
        return errorCode;
    }
}
