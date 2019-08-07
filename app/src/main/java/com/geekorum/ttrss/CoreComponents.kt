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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geekorum.geekdroid.dagger.DaggerDelegateFragmentFactory
import com.geekorum.geekdroid.dagger.DaggerDelegateSavedStateVMFactory
import com.geekorum.geekdroid.dagger.DaggerDelegateViewModelsFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import javax.inject.Inject
import javax.inject.Provider

/**
 * An activity who get a dagger injected [ViewModelProvider.Factory]
 */
@SuppressLint("Registered")
open class ViewModelProviderActivity : AppCompatActivity() {

    val viewModelsFactory: ViewModelProvider.Factory by lazy {
        savedStateVmFactoryCreator.create(this, intent?.extras)
    }

    @Inject
    lateinit var savedStateVmFactoryCreator: DaggerDelegateSavedStateVMFactory.Creator

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

inline fun <reified VM : ViewModel> BaseDialogFragment.viewModels(): Lazy<VM> {
    return viewModels { viewModelsFactory }
}

inline fun <reified VM : ViewModel> BaseDialogFragment.activityViewModels(): Lazy<VM> {
    return activityViewModels { viewModelsFactory }
}

/**
 * As it supports Dagger injection, the Activity must have a corresponding [AndroidInjector]
 */
@SuppressLint("Registered")
open class InjectableBaseActivity : BatteryFriendlyActivity() {
    @Inject
    lateinit var daggerDelegateFragmentFactory: FragmentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        supportFragmentManager.fragmentFactory = daggerDelegateFragmentFactory
        super.onCreate(savedInstanceState)
    }

    /**
     * Inject required field into the activity.
     * Default method use Dagger [AndroidInjection]
     */
    protected open fun inject() = AndroidInjection.inject(this)
}

// BaseActivity is flavor dependant but extends InjectableBaseActivity

/**
 * Common base Fragment for the application.
 */
open class BaseFragment (
    private val savedStateVmFactoryCreator: DaggerDelegateSavedStateVMFactory.Creator,
    val fragmentFactory: FragmentFactory
) : Fragment() {

    val viewModelsFactory: ViewModelProvider.Factory by lazy {
        savedStateVmFactoryCreator.create(this, arguments)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        childFragmentManager.fragmentFactory = fragmentFactory
    }
}

open class BaseDialogFragment (
    private var savedStateVmFactoryCreator: DaggerDelegateSavedStateVMFactory.Creator,
    val fragmentFactory: FragmentFactory
) : DialogFragment() {

    val viewModelsFactory: ViewModelProvider.Factory by lazy {
        savedStateVmFactoryCreator.create(this, arguments)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        childFragmentManager.fragmentFactory = fragmentFactory
    }
}


@Module
abstract class CoreFactoriesModule {

    @Binds
    abstract fun bindsViewModelFactory(factory: DaggerDelegateViewModelsFactory): ViewModelProvider.Factory

    @Binds
    abstract fun bindsFragmentFactory(factory: DaggerDelegateFragmentFactory): FragmentFactory
}
