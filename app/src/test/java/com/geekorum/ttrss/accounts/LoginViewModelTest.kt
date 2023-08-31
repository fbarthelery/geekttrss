/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2023 by Frederic-Charles Barthelery.
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
import com.geekorum.ttrss.webapi.model.ErrorContent
import com.geekorum.ttrss.webapi.model.LoginResponsePayload
import com.geekorum.ttrss.webapi.model.ResponsePayload
import com.google.common.truth.Truth.assertThat
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val archRule = InstantTaskExecutorRule()

    private val testCoroutineDispatcher = StandardTestDispatcher()

    lateinit var accountManager: TinyrssAccountManager

    lateinit var tinyRssApi: TinyRssApi

    private lateinit var viewModel: LoginViewModel

    private lateinit var networkBuilder: AuthenticatorNetworkComponent.Builder

    private val successLoginResponse = LoginResponsePayload(
        status = ResponsePayload.API_STATUS_OK,
        content = LoginResponsePayload.Content("session_id"))

    private val failedLoginResponse = LoginResponsePayload(
        status = ResponsePayload.API_STATUS_ERR,
        content = ErrorContent(error = Error.LOGIN_ERROR))


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
    }

    @Test
    fun testEmptyPasswordSetsErrorMessageAfterOneLoginAttempt() = runTest {
        viewModel.loginFormUiState.password = ""
        assertThat(viewModel.loginFormUiState.passwordFieldErrorMsg).isNull()

        viewModel.loginFormUiState.hasAttemptLogin = true
        viewModel.loginFormUiState.password = ""

        assertThat(viewModel.loginFormUiState.passwordFieldErrorMsg).isEqualTo(R.string.error_field_required)
    }

    @Test
    fun testCheckEmptyUsernameSendEventWhenNonEmpty() = runTest {
        viewModel.loginFormUiState.username = ""
        assertThat(viewModel.loginFormUiState.usernameFieldErrorMsg).isNull()

        viewModel.loginFormUiState.hasAttemptLogin = true
        viewModel.loginFormUiState.username = ""

        assertThat(viewModel.loginFormUiState.usernameFieldErrorMsg).isEqualTo(R.string.error_field_required)
    }

    @Test
    fun checkLoginOperationsUpdateLoginInProgressState() = runTest {
        assertThat(viewModel.loginInProgress).isFalse()

        viewModel.startLoginOperation()
        assertThat(viewModel.loginInProgress).isTrue()

        viewModel.endLoginOperation()
        assertThat(viewModel.loginInProgress).isFalse()
    }

    @Test
    fun checkDoLoginWithSuccessSendCompleteEvent() = runTest {
        every { accountManager.addAccount(any(), any()) } returns true
        every { accountManager.updateServerInformation(any(), any()) } just Runs
        every { accountManager.initializeAccountSync(any()) } just Runs
        coEvery { tinyRssApi.login(any()) } returns successLoginResponse

        viewModel.initialize(LoginActivity.ACTION_ADD_ACCOUNT)
        viewModel.loginFormUiState.apply {
            username = "fred"
            password = "password"
            serverUrl = "http://localhost"
        }

        viewModel.doLogin()

        val completeEvent = viewModel.actionCompleteEvent.asFlow().first()
        val content = completeEvent.peekContent() as LoginViewModel.ActionCompleteEvent.Success

        val expectedAccount = Account(viewModel.loginFormUiState.username, viewModel.loginFormUiState.serverUrl)
        assertThat(content.account).isEqualTo(expectedAccount)
    }

    @Test
    fun checkDoLoginWithFailureSetsSnackbarMessage(): Unit = runTest {
        coEvery { tinyRssApi.login(any()) } returns failedLoginResponse

        viewModel.initialize(LoginActivity.ACTION_ADD_ACCOUNT)
        viewModel.loginFormUiState.apply {
            username = "fred"
            password = "password"
            serverUrl = "http://localhost"
        }

        viewModel.doLogin()

        assertThat(viewModel.snackbarErrorMessageId).isNotNull()
    }
}


@Module
@DisableInstallInCheck
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
