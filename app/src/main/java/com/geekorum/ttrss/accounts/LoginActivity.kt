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
import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.content.IntentCompat
import com.geekorum.geekdroid.accounts.AccountAuthenticatorAppCompatActivity
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.ui.AppTheme3
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * A Login screen to a Tinytinyrss server.
 */
@AndroidEntryPoint
class LoginActivity : AccountAuthenticatorAppCompatActivity() {

    companion object {
        const val ACTION_ADD_ACCOUNT = "add_account"
        const val ACTION_CONFIRM_CREDENTIALS = "confirm_credentials"

        const val EXTRA_ACCOUNT = "account"
    }

    @Inject
    lateinit var accountManager: AndroidTinyrssAccountManager

    private val loginViewModel: LoginViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val account = IntentCompat.getParcelableExtra(intent, EXTRA_ACCOUNT, Account::class.java)?.let {
            accountManager.fromAndroidAccount(it)
        }
        val action = requireNotNull(intent?.action) { "Invalid intent action passed to $this"}
        loginViewModel.initialize(action, account)

        loginViewModel.actionCompleteEvent.observe(this, EventObserver { event->
            when (event) {
                is LoginViewModel.ActionCompleteEvent.Failed -> handleActionFailed()
                is LoginViewModel.ActionCompleteEvent.Success -> handleActionSuccess(event)
            }
        })

        enableEdgeToEdge()
        setContent {
            AppTheme3 {
                LoginScreen(windowSizeClass = calculateWindowSizeClass(this@LoginActivity),
                    viewModel = loginViewModel)
            }
        }
    }

    private fun handleActionSuccess(event: LoginViewModel.ActionCompleteEvent.Success) {
        accountAuthenticatorResult = Bundle().apply {
            when (intent.action) {
                ACTION_ADD_ACCOUNT -> {
                    putString(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.TTRSS_ACCOUNT_TYPE)
                    putString(AccountManager.KEY_ACCOUNT_NAME, event.account.username)
                }
                ACTION_CONFIRM_CREDENTIALS -> putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true)
            }
        }
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun handleActionFailed() {
        accountAuthenticatorResult = Bundle().apply {
            when (intent.action) {
                ACTION_ADD_ACCOUNT -> {
                    putInt(AccountManager.KEY_ERROR_CODE, AccountAuthenticator.ERROR_CODE_AUTHENTICATOR_FAILURE)
                    putString(AccountManager.KEY_ERROR_MESSAGE, "Unable to add account")
                }
                ACTION_CONFIRM_CREDENTIALS -> putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
            }
        }
        finish()
    }

}
