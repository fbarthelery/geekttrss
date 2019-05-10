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
package com.geekorum.ttrss

import android.annotation.SuppressLint
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geekorum.ttrss.di.ViewModelsFactory
import dagger.android.AndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * An activity who get a dagger injected [ViewModelProvider.Factory]
 */
@SuppressLint("Registered")
open class ViewModelProviderActivity : DaggerAppCompatActivity() {
    @Inject
    lateinit var viewModelsFactory: ViewModelsFactory
}

inline fun <reified VM : ViewModel> ViewModelProviderActivity.viewModels(): Lazy<VM> {
    return viewModels { viewModelsFactory }
}


open class ViewModelProviderFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelsFactory: ViewModelsFactory
}

inline fun <reified VM : ViewModel> ViewModelProviderFragment.viewModels(): Lazy<VM> {
    return viewModels { viewModelsFactory }
}

inline fun <reified VM : ViewModel> ViewModelProviderFragment.activityViewModels(): Lazy<VM> {
    return activityViewModels { viewModelsFactory }
}

/**
 * Common base Activity for the application.
 * As it supports Dagger injection, the Activity must have a corresponding [AndroidInjector]
 */
@SuppressLint("Registered")
open class BaseActivity : BatteryFriendlyActivity()

/**
 * Common base Fragment for the application.
 * As it supports Dagger injection, the Fragment must have a corresponding [AndroidInjector]
 */
open class BaseFragment : ViewModelProviderFragment()
