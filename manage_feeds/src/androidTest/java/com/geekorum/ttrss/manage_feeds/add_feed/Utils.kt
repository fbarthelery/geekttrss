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
package com.geekorum.ttrss.manage_feeds.add_feed

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.testing.manifest.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.geekorum.ttrss.manage_feeds.ViewModelProvidedActivity

/* Copied from androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY */
const val THEME_EXTRAS_BUNDLE_KEY = "androidx.fragment.app.testing.FragmentScenario" +
    ".EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY"

/**
 * launchFragmentInContainer from the androidx.fragment:fragment-testing library
 * is NOT possible to use right now as it uses a hardcoded Activity under the hood
 * (i.e. [EmptyFragmentActivity]) which is not annotated with @AndroidEntryPoint and
 * is AndroidEntryPoint does not work on feature modules.
 *
 * As a workaround, use this function that is equivalent and launch fragment in the [ViewModelProvidedActivity]
 * and set it to use [viewModelProviderFactory]. It requires you to add
 * [ViewModelProvidedActivity] in the debug folder and include it in the debug AndroidManifest.xml
 * file.
 */
internal inline fun <reified F : Fragment> launchFragmentInViewModelProvidedActivity(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.FragmentScenarioEmptyFragmentActivityTheme,
    viewModelProviderFactory: ViewModelProvider.Factory,
    crossinline instantiate: () -> F
): FragmentScenario<F, ViewModelProvidedActivity> {
    val startActivityIntent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext<Application>(),
            ViewModelProvidedActivity::class.java
        )
    ).putExtra(THEME_EXTRAS_BUNDLE_KEY, themeResId)

    val scenario = FragmentScenario(F::class.java,
        ActivityScenario.launch<ViewModelProvidedActivity>(startActivityIntent))

    scenario.activityScenario.onActivity { activity ->
        activity.viewModelProviderFactory = viewModelProviderFactory

        val fragment: Fragment = instantiate()
        fragment.arguments = fragmentArgs
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, FRAGMENT_TAG)
            .commitNow()
    }
    return scenario
}


private const val FRAGMENT_TAG = "FragmentScenario_Fragment_Tag"

internal class FragmentScenario<F: Fragment?, A: FragmentActivity>(
    private val fragmentClass: Class<F>,
    internal val activityScenario: ActivityScenario<A>
) {

    fun onFragment(action: (F) -> Unit): FragmentScenario<F, A> {
        activityScenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentByTag(
                    FRAGMENT_TAG)
                checkNotNull(fragment) { "The fragment has been removed from FragmentManager already." }
                check(fragmentClass.isInstance(fragment))
                action(fragmentClass.cast(fragment)!!)
            }
        return this
    }
}

internal inline fun <reified VM: ViewModel> createViewModelFactoryFor(viewModel: VM): ViewModelProvider.Factory = object : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass == VM::class.java) {
            return viewModel as T
        }
        error("Not implemented")
    }
}
