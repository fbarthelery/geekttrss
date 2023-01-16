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
package com.geekorum.ttrss.accounts

import android.accounts.Account
import android.accounts.AccountManager
import com.geekorum.geekdroid.accounts.AccountTokenRetriever
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.webapi.TokenRetriever
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * An [AccountTokenRetriever] for Tinyrss.
 * It ensures that [AccountTokenRetriever.getToken] is run on an IO thread.
 */
internal class TinyrssAccountTokenRetriever(
    private val dispatchers: CoroutineDispatchersProvider,
    accountManager: AccountManager,
    account: Account
) : TokenRetriever, AccountTokenRetriever(
    accountManager,
    AccountAuthenticator.TTRSS_AUTH_TOKEN_SESSION_ID,
    account,
    true
) {

    @OptIn(ObsoleteCoroutinesApi::class, DelicateCoroutinesApi::class, ExperimentalTime::class)
    private val mailbox = GlobalScope.actor<Message>{
        var lastMsg: Message? = null
        var lastMsgTimeMark: TimeMark? = null
        for (msg in channel) {
            when {
                msg is Message.GetToken -> {
                    val result = runCatching {
                        getTokenInternal()
                    }
                    msg.token.completeWith(result)
                }
                msg is Message.InvalidateToken && lastMsg == msg
                -> {
                    val invalidate = (lastMsgTimeMark == null || lastMsgTimeMark.elapsedNow() >= 6.seconds)
                    if (invalidate) {
                        invalidateTokenInternal()
                        lastMsgTimeMark = TimeSource.Monotonic.markNow()
                    }
                }
            }
            lastMsg = msg
        }
    }

    override suspend fun getToken(): String {
        val response = CompletableDeferred<String>()
        val getMsg = Message.GetToken(response)
        mailbox.send(getMsg)
        return response.await()
    }

    override suspend fun invalidateToken() {
        mailbox.send(Message.InvalidateToken)
    }


    private suspend fun getTokenInternal(): String = withContext(dispatchers.io) {
        try {
            super.getToken()
        } catch (e: com.geekorum.geekdroid.network.TokenRetriever.RetrieverException) {
            throw TokenRetriever.RetrieverException("unable to retrieve token from account", e)
        }
    }

    private suspend fun invalidateTokenInternal() {
        super.invalidateToken()
    }

    private sealed class Message {
        class GetToken(val token: CompletableDeferred<String>) : Message()
        object InvalidateToken: Message()
    }
}
