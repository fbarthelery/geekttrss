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
package com.geekorum.ttrss.articles_list

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.geekorum.geekdroid.accounts.AccountSelector
import com.geekorum.geekdroid.accounts.AccountsListViewModel
import com.geekorum.geekdroid.app.lifecycle.EmptyEvent
import com.geekorum.ttrss.accounts.AccountAuthenticator
import com.geekorum.ttrss.providers.ArticlesContract
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject
import com.geekorum.geekdroid.app.lifecycle.EmptyEvent.Companion.makeEmptyEvent as NoAccountSelectedEvent

/**
 * ViewModel to contains the differents Accounts from the application
 */
@HiltViewModel
class TtrssAccountViewModel @Inject constructor(
    accountManager: AccountManager,
    accountSelector: AccountSelector
): AccountsListViewModel(accountManager, accountSelector, AccountAuthenticator.TTRSS_ACCOUNT_TYPE) {

    val selectedAccountHost = selectedAccount.map { account ->
        if (account != null) {
            val url = accountManager.getUserData(account, AccountAuthenticator.USERDATA_URL)
            Uri.parse(url).host ?: ""
        } else ""
    }
    private val noAccountSelectedEventSource = MutableLiveData<EmptyEvent>()
    val noAccountSelectedEvent:LiveData<EmptyEvent> = noAccountSelectedEventSource

    fun startSelectAccountActivity(activity: Activity) {
        val callback = AccountManagerCallback<Bundle> { accountManagerFuture ->
            try {
                val result = accountManagerFuture.result
                val accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME, "")
                val accountType = result.getString(AccountManager.KEY_ACCOUNT_TYPE, "")
                selectAccount(Account(accountName, accountType))
            } catch (e: Exception) {
                Timber.w(e, "Unable to get auth token")
                noAccountSelectedEventSource.value = NoAccountSelectedEvent()
            }
        }
        accountManager.getAuthTokenByFeatures(AccountAuthenticator.TTRSS_ACCOUNT_TYPE,
                AccountAuthenticator.TTRSS_AUTH_TOKEN_SESSION_ID, null,
                activity, null, null,
                callback, null)
    }

    fun startAddAccountActivity(activity: Activity) {
        accountManager.addAccount(AccountAuthenticator.TTRSS_ACCOUNT_TYPE, null, null, null,
                activity, null, null)
    }

    fun startManageAccountActivity(context: Context) {
        val i = Intent(Settings.ACTION_SYNC_SETTINGS)
        i.putExtra(Settings.EXTRA_AUTHORITIES, arrayOf(ArticlesContract.AUTHORITY))
        context.startActivity(i)
    }

    fun isExistingAccount(account: Account?) = accountSelector.isExistingAccount(account)

}
