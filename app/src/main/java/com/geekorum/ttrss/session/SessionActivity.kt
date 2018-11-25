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
package com.geekorum.ttrss.session

import android.accounts.Account
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.geekorum.ttrss.MainActivity
import com.geekorum.ttrss.articles_list.TtrssAccountViewModel
import com.geekorum.ttrss.di.ViewModelsFactory
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

/**
 * Base Activity for a Session that starts with a Logged Account interacting with the backend.
 *
 * It is the responsibility of subclasses to observe the current account and discard any
 * account dependencies when it changes. The TtrssAccountViewModel can be used to observe the current account.
 */
abstract class SessionActivity : AppCompatActivity(),
        HasSupportFragmentInjector {

    @Inject lateinit var supportFragmentInjector :DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = supportFragmentInjector

    @Inject lateinit var viewModelFactory: ViewModelsFactory

    private lateinit var accountViewModel: TtrssAccountViewModel

    var account: Account? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        accountViewModel = ViewModelProviders.of(this, viewModelFactory).get(TtrssAccountViewModel::class.java)
        account = savedInstanceState?.getParcelable(SAVED_ACCOUNT)
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
