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
package com.geekorum.ttrss.accounts

import android.accounts.Account
import android.accounts.AccountManager
import com.geekorum.geekdroid.accounts.AccountTokenRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * An [AccountTokenRetriever] for Tinyrss.
 * It ensures that [AccountTokenRetriever.getToken] is run on an IO thread.
 */
internal class TinyrssAccountTokenRetriever(
    accountManager: AccountManager,
    account: Account
) : AccountTokenRetriever(
    accountManager,
    AccountAuthenticator.TTRSS_AUTH_TOKEN_SESSION_ID,
    account,
    true) {

    override fun getToken(): String = runBlocking(Dispatchers.IO) {
        super.getToken()
    }
}
