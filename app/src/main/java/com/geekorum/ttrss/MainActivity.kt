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
package com.geekorum.ttrss

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.articles_list.ArticleListActivity
import com.geekorum.ttrss.articles_list.TtrssAccountViewModel
import com.geekorum.ttrss.di.ViewModelsFactory
import dagger.android.AndroidInjection
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject lateinit var viewModelsFactory: ViewModelsFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        val accountViewModel = ViewModelProviders.of(this, viewModelsFactory).get(TtrssAccountViewModel::class.java)
        accountViewModel.selectedAccount.observe(this, Observer { account ->
            if (account != null) {
                val intent = Intent(this, ArticleListActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                accountViewModel.startSelectAccountActivity(this)
            }
        })
        accountViewModel.noAccountSelectedEvent.observe(this, EventObserver { finish() })
    }

}
