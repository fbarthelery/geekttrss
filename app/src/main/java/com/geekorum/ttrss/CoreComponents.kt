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
import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geekorum.geekdroid.dagger.DaggerDelegateFragmentFactory
import com.geekorum.geekdroid.dagger.DaggerDelegateViewModelsFactory
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * An activity who get a dagger injected [ViewModelProvider.Factory]
 */
@SuppressLint("Registered")
open class ViewModelProviderActivity : DaggerAppCompatActivity() {
    @Inject
    lateinit var viewModelsFactory: DaggerDelegateViewModelsFactory
}

inline fun <reified VM : ViewModel> ViewModelProviderActivity.viewModels(): Lazy<VM> {
    return viewModels { viewModelsFactory }
}

inline fun <reified VM : ViewModel> BaseFragment.viewModels(): Lazy<VM> {
    return viewModels { viewModelsFactory }
}

inline fun <reified VM : ViewModel> BaseFragment.activityViewModels(): Lazy<VM> {
    return activityViewModels { viewModelsFactory }
}

/**
 * Common base Activity for the application.
 * As it supports Dagger injection, the Activity must have a corresponding [AndroidInjector]
 */
@SuppressLint("Registered")
open class BaseActivity : BatteryFriendlyActivity() {
    @Inject
    lateinit var daggerDelegateFragmentFactory: DaggerDelegateFragmentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        // need to be set after injection and before super.onCreate() because it recreates fragments
        // because of this we need to inject ourselves
        // not ideal because we inject 2 times, and create dependencies 2 times.
        // TODO get rid of DaggerAppCompatActivity
        AndroidInjection.inject(this)
        supportFragmentManager.fragmentFactory = daggerDelegateFragmentFactory
        super.onCreate(savedInstanceState)
    }
}

/**
 * Common base Fragment for the application.
 */
open class BaseFragment (
    val viewModelsFactory: ViewModelsFactory,
    val fragmentFactory: DaggerDelegateFragmentFactory
) : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        childFragmentManager.fragmentFactory = fragmentFactory
    }
}
