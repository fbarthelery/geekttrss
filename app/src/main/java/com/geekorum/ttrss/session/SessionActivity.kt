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
package com.geekorum.ttrss.session

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.os.BundleCompat
import com.geekorum.ttrss.core.BaseActivity
import com.geekorum.ttrss.MainActivity
import com.geekorum.ttrss.articles_list.TtrssAccountViewModel

/**
 * Base Activity for a Session that starts with a Logged Account interacting with the backend.
 *
 * It is the responsibility of subclasses to observe the current account and discard any
 * account dependencies when it changes. The TtrssAccountViewModel can be used to observe the current account.
 */
abstract class SessionActivity : BaseActivity() {

    private val accountViewModel: TtrssAccountViewModel by viewModels()

    var account: Account? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        account = savedInstanceState?.let { BundleCompat.getParcelable(savedInstanceState, SAVED_ACCOUNT, Account::class.java) }
        if (account == null || !accountViewModel.isExistingAccount(account)) {
            account = accountViewModel.selectedAccount.value
        }
        if (account == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(SAVED_ACCOUNT, account)
    }

    companion object {
        private const val SAVED_ACCOUNT = "account"
    }
}
