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

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import com.geekorum.ttrss.BuildConfig
import com.geekorum.ttrss.R
import com.geekorum.ttrss.webapi.ApiCallException
import com.geekorum.ttrss.webapi.checkStatus
import com.geekorum.ttrss.webapi.model.LoginRequestPayload
import com.geekorum.ttrss.webapi.model.LoginResponsePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber

import java.io.IOException
import java.util.concurrent.ExecutionException

import javax.inject.Inject

class AccountAuthenticator @Inject
internal constructor(
    private val context: Context,
    private val accountManager: AndroidTinyrssAccountManager,
    private val authenticatorBuilder: AuthenticatorNetworkComponent.Builder
) : AbstractAccountAuthenticator(context) {

    override fun editProperties(
        response: AccountAuthenticatorResponse, accountType: String
    ): Bundle = Bundle()

    override fun addAccount(
        response: AccountAuthenticatorResponse, accountType: String, authTokenType: String?,
        requiredFeatures: Array<String>?, options: Bundle?
    ): Bundle? {
        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            action = LoginActivity.ACTION_ADD_ACCOUNT
        }
        return bundleOf(AccountManager.KEY_INTENT to intent)
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse, account: Account, options: Bundle?
    ): Bundle {
        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            action = LoginActivity.ACTION_CONFIRM_CREDENTIALS
            putExtra(LoginActivity.EXTRA_ACCOUNT, account)
        }
        return bundleOf(AccountManager.KEY_INTENT to intent)
    }

    @SuppressLint("MissingPermission")
    override fun getAuthToken(
        response: AccountAuthenticatorResponse, account: Account,
        authTokenType: String, options: Bundle?
    ): Bundle {
        val ttRssAccount = accountManager.fromAndroidAccount(account)
        val password = try {
            accountManager.getPassword(ttRssAccount)
        } catch (e: Exception) {
            Timber.w(e, "Unable to get encrypted password")
            return getRevalidateCredentialResponse(account)
        }

        val serverInformation = accountManager.getServerInformation(ttRssAccount)
        try {
            val sessionId = runBlocking {
                val responsePayload = login(ttRssAccount.username, password, serverInformation)
                responsePayload.checkStatus()
                responsePayload.sessionId
            }
            return bundleOf(
                AccountManager.KEY_ACCOUNT_NAME to account.name,
                AccountManager.KEY_ACCOUNT_TYPE to account.type,
                AccountManager.KEY_AUTHTOKEN to sessionId
            )
        } catch (e: ApiCallException) {
            if (e.errorCode === ApiCallException.ApiError.LOGIN_FAILED) {
                Timber.w("Login failed: Invalid credentials")
                return getRevalidateCredentialResponse(account)
            }
        } catch (e: InterruptedException) {
            val priority = if (e.cause is IOException) Log.WARN else Log.ERROR
            Timber.log(priority, e,"Unable to login")
        } catch (e: ExecutionException) {
            val priority = if (e.cause is IOException) Log.WARN else Log.ERROR
            Timber.log(priority, e,"Unable to login")
        }
        // if we got there an error happened, probably network
        return bundleOf(
            AccountManager.KEY_ERROR_CODE to AccountManager.ERROR_CODE_NETWORK_ERROR,
            AccountManager.KEY_ERROR_MESSAGE to "Unable to login")
    }

    private fun getRevalidateCredentialResponse(account: Account): Bundle {
        val intent = Intent(context, LoginActivity::class.java).apply {
            action = LoginActivity.ACTION_CONFIRM_CREDENTIALS
            putExtra(LoginActivity.EXTRA_ACCOUNT, account)
        }
        return bundleOf(AccountManager.KEY_INTENT to intent)
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    private suspend fun login(
        user: String, password: String?, serverInformation: ServerInformation
    ): LoginResponsePayload = withContext(Dispatchers.IO) {
        val urlModule = TinyRssServerInformationModule(serverInformation)
        val authenticatorNetworkComponent = authenticatorBuilder
            .tinyRssServerInformationModule(urlModule)
            .build()
        val api = authenticatorNetworkComponent.getTinyRssApi()
        val payload = LoginRequestPayload(user, password ?: "")
        api.login(payload)
    }

    override fun getAuthTokenLabel(authTokenType: String): String {
        return context.resources.getString(R.string.ttrss_account)
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse, account: Account,
        authTokenType: String?, options: Bundle?
    ): Bundle {
        // let's just say it is not supported yet
        return makeNotSupportedResponse()
    }

    private fun makeNotSupportedResponse(): Bundle {
        return bundleOf(
            AccountManager.KEY_ERROR_CODE to ERROR_CODE_NOT_SUPPORTED,
            AccountManager.KEY_ERROR_MESSAGE to "Not supported"
        )
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse, account: Account, features: Array<String>
    ): Bundle {
        // let's say no for now unless features is empty
        val supported = features.isEmpty()
        return bundleOf(AccountManager.KEY_BOOLEAN_RESULT to supported)
    }

    companion object {
        const val TTRSS_ACCOUNT_TYPE = BuildConfig.APPLICATION_ID
        const val TTRSS_AUTH_TOKEN_SESSION_ID = "session_id"
        const val ERROR_CODE_AUTHENTICATOR_FAILURE = 500
        const val ERROR_CODE_NOT_SUPPORTED = 501
        const val USERDATA_URL = "url"
        const val USERDATA_BASIC_HTTP_AUTH_USERNAME = "basic_http_auth_username"
        const val USERDATA_BASIC_HTTP_AUTH_PASSWORD = "basic_http_auth_password"
    }
}
