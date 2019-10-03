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
package com.geekorum.ttrss.articles_list

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.observe
import com.geekorum.ttrss.R

/**
 * Presenter for the Account Header view in NavigationView
 */
class AccountHeaderPresenter(
    private val headerView: View,
    private val lifecycleOwner: LifecycleOwner,
    private val accountViewModel: TtrssAccountViewModel
) {
    init {
        setUpViewModels()
    }

    private fun setUpViewModels() {
        accountViewModel.selectedAccount.observe(lifecycleOwner) { account ->
            val login = headerView.findViewById<TextView>(R.id.drawer_header_login)
            login.text = account.name
        }

        accountViewModel.selectedAccountHost.observe(lifecycleOwner) { host ->
            val server = headerView.findViewById<TextView>(R.id.drawer_header_server)
            server.text = host
        }
    }
}
