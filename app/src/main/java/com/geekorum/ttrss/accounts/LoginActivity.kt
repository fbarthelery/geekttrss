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
package com.geekorum.ttrss.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.geekorum.geekdroid.accounts.AccountAuthenticatorAppCompatActivity
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.R
import com.geekorum.ttrss.databinding.ActivityLoginAccountBinding
import com.geekorum.ttrss.di.ViewModelsFactory
import dagger.android.AndroidInjection
import javax.inject.Inject

/**
 * A Login screen to a Tinytinyrss server.
 */
class LoginActivity : AccountAuthenticatorAppCompatActivity() {

    companion object {
        const val ACTION_ADD_ACCOUNT = "add_account"
        const val ACTION_CONFIRM_CREDENTIALS = "confirm_credentials"

        const val EXTRA_ACCOUNT = "account"
    }

    @Inject
    lateinit var accountManager: AndroidTinyrssAccountManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelsFactory

    private lateinit var binding: ActivityLoginAccountBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_account)
        binding.setLifecycleOwner(this)
        setSupportActionBar(binding.toolbar)
        val loginViewModel = ViewModelProviders.of(this, viewModelFactory)[LoginViewModel::class.java]
        val account = intent.getParcelableExtra<Account>(EXTRA_ACCOUNT)?.let {
            accountManager.fromAndroidAccount(it)
        }
        val action = requireNotNull(intent?.action) { "Invalid intent action passed to $this"}
        loginViewModel.initialize(action, account)
        binding.viewModel = loginViewModel

        loginViewModel.loginInProgress.observe(this, Observer { inProgress ->
            showProgress(inProgress!!)
        })
        loginViewModel.loginFailedEvent.observe(this, EventObserver { event ->
            Snackbar.make(binding.root, event.errorMsgId, Snackbar.LENGTH_SHORT).show()
        })

        loginViewModel.actionCompleteEvent.observe(this, EventObserver { event->
            when (event) {
                is LoginViewModel.ActionCompleteEvent.Failed -> handleActionFailed()
                is LoginViewModel.ActionCompleteEvent.Success -> handleActionSuccess(event)

            }
        })

        loginViewModel.fieldErrors.observe(this, Observer { errorStatus ->
            val error = checkNotNull(errorStatus)
            setFieldError(binding.form.url, error.invalidUrlMsgId)
            if (error.hasAttemptLogin) {
                setFieldError(binding.form.username, error.invalidNameMsgId)
                setFieldError(binding.form.password, error.invalidPasswordMsgId)
            }
        })
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

    private fun setFieldError(view: EditText, errorId: Int?) {
        val errorMsg = if (errorId != null) getString(errorId) else null
        view.error = errorMsg
    }


    private fun showProgress(show: Boolean) {
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
        binding.form.loginForm.visibility = if (show) View.GONE else View.VISIBLE
        binding.form.loginProgress.visibility = if (show) View.VISIBLE else View.GONE
    }


}
