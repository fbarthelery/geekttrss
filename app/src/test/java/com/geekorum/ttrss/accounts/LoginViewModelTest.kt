/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2020 by Frederic-Charles Barthelery.
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
import androidx.lifecycle.asFlow
import com.geekorum.ttrss.R
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.webapi.TinyRssApi
import com.geekorum.ttrss.webapi.model.Error
import com.geekorum.ttrss.webapi.model.LoginResponsePayload
import com.geekorum.ttrss.webapi.model.ResponsePayload
import com.google.common.truth.Truth.assertThat
import dagger.Component
import dagger.Module
import dagger.Provides
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@UseExperimental(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val archRule = InstantTaskExecutorRule()

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

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
        Dispatchers.setMain(testCoroutineDispatcher)
        tinyRssApi = mockk()
        accountManager = mockk()
        networkBuilder = DaggerTestAuthenticatorNetworkComponent.builder()
            .fakeTinyrssApiModule(FakeTinyrssApiModule(tinyRssApi))

        val dispatchersProvider = CoroutineDispatchersProvider(testCoroutineDispatcher,
                testCoroutineDispatcher,
                testCoroutineDispatcher)
        viewModel = LoginViewModel(accountManager, networkBuilder, dispatchersProvider)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testCoroutineDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun testCheckEmptyPasswordSendEventWhenNonEmpty() = testCoroutineDispatcher.runBlockingTest {
        viewModel.checkNonEmptyPassword("")

        val error = viewModel.fieldErrors.asFlow().first()
        assertThat(error.invalidPasswordMsgId).isEqualTo(R.string.error_field_required)
    }

    @Test
    fun testCheckEmptyUsernameSendEventWhenNonEmpty() = testCoroutineDispatcher.runBlockingTest {
        viewModel.checkNonEmptyUsername("")
        val error = viewModel.fieldErrors.asFlow().first()
        assertThat(error.invalidNameMsgId).isEqualTo(R.string.error_field_required)
    }

    @Test
    fun checkDoLoginSendProgressEvent(): Unit = runBlockingTest {
        coEvery { tinyRssApi.login(any()) } returns successLoginResponse
        every { accountManager.addAccount(any(), any()) } returns false

        viewModel.initialize(LoginActivity.ACTION_ADD_ACCOUNT)
        viewModel.httpUrl = "http://localhost".toHttpUrl()

        val progressEvents = async {
            viewModel.loginInProgress.asFlow()
                    .take(2)
                    .toList()
        }

        viewModel.doLogin()

        assertThat(progressEvents.await()).containsExactly(true, false)
    }

    @Test
    fun checkDoLoginWithSuccessSendCompleteEvent() = testCoroutineDispatcher.runBlockingTest {
        every { accountManager.addAccount(any(), any()) } returns true
        every { accountManager.initializeAccountSync(any()) } just Runs
        coEvery { tinyRssApi.login(any()) } returns successLoginResponse

        viewModel.initialize(LoginActivity.ACTION_ADD_ACCOUNT)
        viewModel.username = "fred"
        viewModel.password = "password"
        viewModel.httpUrl = "http://localhost".toHttpUrl()

        viewModel.doLogin()

        val completeEvent = viewModel.actionCompleteEvent.asFlow().first()
        val content = completeEvent.peekContent() as LoginViewModel.ActionCompleteEvent.Success

        val expectedAccount = Account(viewModel.username, viewModel.httpUrl.toString())
        assertThat(content.account).isEqualTo(expectedAccount)
    }

    @Test
    fun checkDoLoginWithFailSendLoginFailedEvent(): Unit = testCoroutineDispatcher.runBlockingTest {
        coEvery { tinyRssApi.login(any()) } returns failedLoginResponse

        viewModel.initialize(LoginActivity.ACTION_ADD_ACCOUNT)
        viewModel.username = "fred"
        viewModel.password = "password"
        viewModel.httpUrl = "http://localhost".toHttpUrl()

        viewModel.doLogin()

        val failedEvent = viewModel.loginFailedEvent.asFlow().first()
        assertThat(failedEvent.peekContent()).isNotNull()
    }
}


@Module
class FakeTinyrssApiModule(
    private val tinyRssApi: TinyRssApi
) {
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
