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
package com.geekorum.ttrss.accounts

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geekorum.geekdroid.dagger.ViewModelKey
import com.geekorum.ttrss.R
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import io.mockk.mockk
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28]) // for now we don't do robolectric on sdk 29 (needs to run on java9)
class LoginActivityTest {

    @Test
    @Config(qualifiers = "w800dp")
    fun testThatWeCanStartTheActivityOnW800Dp() {
        val intent = Intent(LoginActivity.ACTION_ADD_ACCOUNT).apply {
            setClass(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
        }
        launchActivity<LoginActivity>(intent).use {
            onView(withId(R.id.email_sign_in_button))
                .check(matches(not(isEnabled())))
        }
    }

    @Test
    fun testThatWeCanStartTheActivity() {
        val intent = Intent(LoginActivity.ACTION_ADD_ACCOUNT).apply {
            setClass(ApplicationProvider.getApplicationContext(), LoginActivity::class.java)
        }
        launchActivity<LoginActivity>(intent).use {
            onView(withId(R.id.email_sign_in_button))
                .check(matches(not(isEnabled())))
        }
    }

}

@Module
internal abstract class LoginActivityTestModule {

    @ContributesAndroidInjector(modules = [MocksModule::class])
    internal abstract fun contributesLoginActivityInjector(): LoginActivity

}

@Module
internal class MocksModule {
    @Provides
    fun providesAndroidTinyrssAccountManager(): AndroidTinyrssAccountManager = mockk()

    @Provides
    fun providesTinyrssAccountManager(androidTinyrssAccountManager: AndroidTinyrssAccountManager): TinyrssAccountManager {
        return androidTinyrssAccountManager
    }

    @Provides
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    fun getLoginViewModel(accountManager: TinyrssAccountManager): ViewModel {
        return LoginViewModel(accountManager, mockk(relaxed = true), mockk(relaxed = true))
    }

}
