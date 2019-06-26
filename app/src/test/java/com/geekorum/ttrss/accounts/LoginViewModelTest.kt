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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geekorum.geekdroid.app.lifecycle.Event
import com.geekorum.ttrss.R
import com.geekorum.ttrss.webapi.TinyRssApi
import com.geekorum.ttrss.webapi.model.Error
import com.geekorum.ttrss.webapi.model.LoginResponsePayload
import com.geekorum.ttrss.webapi.model.ResponsePayload
import dagger.Component
import dagger.Module
import dagger.Provides
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.HttpUrl
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.Executors


class LoginViewModelTest {

    @get:Rule
    val archRule = InstantTaskExecutorRule()

    private val mainThreadSurrogate =  Executors.newSingleThreadExecutor {
        Thread(it, "UI Thread")
    }.asCoroutineDispatcher()

    lateinit var accountManager: TinyrssAccountManager

    lateinit var tinyRssApi: TinyRssApi

    private lateinit var viewModel: LoginViewModel

    private lateinit var networkBuilder: AuthenticatorNetworkComponent.Builder

    private val successLoginResponse = LoginResponsePayload(
        status = ResponsePayload.API_STATUS_OK,
        content = LoginResponsePayload.Content("session_id"))

    private val failedLoginResponse = LoginResponsePayload(
        status = ResponsePayload.API_STATUS_ERR,
        content = LoginResponsePayload.Content(error = Error.LOGIN_ERROR))


    @Before
    fun setup() {
        Dispatchers.setMain(mainThreadSurrogate)
        tinyRssApi = mockk()
        accountManager = mockk()
        networkBuilder = DaggerTestAuthenticatorNetworkComponent.builder()
            .fakeTinyrssApiModule(FakeTinyrssApiModule(tinyRssApi))

        viewModel = LoginViewModel(accountManager, networkBuilder)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun testCheckEmptyPasswordSendEventWhenNonEmpty() {
        val observer: Observer<LoginViewModel.FieldErrorStatus> = mockObserver()
        viewModel.fieldErrors.observeForever(observer)
        viewModel.checkNonEmptyPassword("")
        verify { observer.onChanged(match { it.invalidPasswordMsgId == R.string.error_field_required }) }
    }

    @Test
    fun testCheckEmptyUsernameSendEventWhenNonEmpty() {
        val observer: Observer<LoginViewModel.FieldErrorStatus> = mockObserver()
        viewModel.fieldErrors.observeForever(observer)
        viewModel.checkNonEmptyUsername("")
        verify { observer.onChanged(match { it.invalidNameMsgId == R.string.error_field_required }) }
    }

    @Test
    fun checkDoLoginSendProgressEvent() {
        val observer: Observer<Boolean> = mockObserver()
        coEvery { tinyRssApi.login(any()).await() } returns successLoginResponse
        every { accountManager.addAccount(any(), any()) } returns false

        viewModel.initialize(LoginActivity.ACTION_ADD_ACCOUNT)
        viewModel.loginInProgress.observeForever(observer)
        viewModel.httpUrl = HttpUrl.parse("http://localhost")

        runBlocking {
            viewModel.doLogin()
        }

        verifySequence {
            observer.onChanged(true)
            observer.onChanged(false)
        }
    }

    @Test
    fun checkDoLoginWithSuccessSendCompleteEvent() {
        val observer: Observer<Event<LoginViewModel.ActionCompleteEvent>> = mockObserver()
        every { accountManager.addAccount(any(), any()) } returns true
        every { accountManager.initializeAccountSync(any()) } just Runs
        coEvery { tinyRssApi.login(any()).await() } returns successLoginResponse

        viewModel.initialize(LoginActivity.ACTION_ADD_ACCOUNT)
        viewModel.actionCompleteEvent.observeForever(observer)
        viewModel.username = "fred"
        viewModel.password = "password"
        viewModel.httpUrl = HttpUrl.parse("http://localhost")

        runBlocking {
            viewModel.doLogin()
            viewModel.waitForChildrenCoroutines()
        }
        verify {
            observer.onChanged(match {
                val content = it.peekContent() as LoginViewModel.ActionCompleteEvent.Success
                content.account == Account(viewModel.username, viewModel.httpUrl.toString())
            })
        }
    }

    @Test
    fun checkDoLoginWithFailSendLoginFailedEvent() {
        val observer: Observer<Event<LoginViewModel.LoginFailedError>> = mockObserver()
        coEvery { tinyRssApi.login(any()).await() } returns failedLoginResponse

        viewModel.initialize(LoginActivity.ACTION_ADD_ACCOUNT)
        viewModel.loginFailedEvent.observeForever(observer)
        viewModel.username = "fred"
        viewModel.password = "password"
        viewModel.httpUrl = HttpUrl.parse("http://localhost")

        runBlocking {
            viewModel.doLogin()
        }
        verify { observer.onChanged(any()) }
    }


    private inline fun <reified T : Observer<K>, reified K : Any> mockObserver(): T {
        val observer: T = mockk()
        every { observer.onChanged(any()) } just Runs
        return observer
    }
}

private suspend fun ViewModel.waitForChildrenCoroutines() {
    viewModelScope.coroutineContext[Job]!!.children.forEach { it.join() }
}


@Module
class FakeTinyrssApiModule(val tinyRssApi: TinyRssApi) {
    @Provides
    fun providesTinyRssApi(): TinyRssApi {
        return tinyRssApi
    }
}

@Component(modules = [TinyRssServerInformationModule::class, FakeTinyrssApiModule::class])
internal interface TestAuthenticatorNetworkComponent : AuthenticatorNetworkComponent {

    @Component.Builder
    interface Builder : AuthenticatorNetworkComponent.Builder {
        fun fakeTinyrssApiModule(fakeTinyrssApiModule: FakeTinyrssApiModule): Builder
    }

}
