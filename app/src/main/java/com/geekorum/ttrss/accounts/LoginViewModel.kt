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

import android.security.NetworkSecurityPolicy
import android.view.inputmethod.EditorInfo
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geekorum.geekdroid.app.lifecycle.Event
import com.geekorum.ttrss.R
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.webapi.ApiCallException
import com.geekorum.ttrss.webapi.checkStatus
import com.geekorum.ttrss.webapi.model.LoginRequestPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel for LoginActivity
 */
@HiltViewModel
internal class LoginViewModel @Inject constructor(
    private val accountManager: TinyrssAccountManager,
    private val networkComponentBuilder: AuthenticatorNetworkComponent.Builder,
    private val dispatchers: CoroutineDispatchersProvider
) : ViewModel() {

    val loginFormUiState = VMMutableLoginFormUiState()

    private lateinit var action: String
    private var account: Account? = null

    var loginInProgress by mutableStateOf(false)
        private set

    var snackbarErrorMessageId by mutableStateOf<Int?>(null)
        private set

    val actionCompleteEvent = MutableLiveData<Event<ActionCompleteEvent>>()

    fun initialize(action: String, account: Account? = null) {
        check(action in listOf(LoginActivity.ACTION_ADD_ACCOUNT, LoginActivity.ACTION_CONFIRM_CREDENTIALS)) {
            "unknown action"
        }
        this.action = action
        this.account = account
        loginFormUiState.initialize(action, account)
    }

    fun clearSnackbarMessage() {
        snackbarErrorMessageId = null
    }

    @JvmOverloads
    fun confirmLogin(id: Int = EditorInfo.IME_NULL): Boolean {
        val handleAction = (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL)
        if (handleAction && loginFormUiState.areFieldsCorrect) {
            viewModelScope.launch {
                loginFormUiState.hasAttemptLogin = true
                doLogin()
            }
        }
        return handleAction
    }

    @VisibleForTesting
    internal suspend fun doLogin() {
        val serverUrl = requireNotNull(convertStringToHttpUrl(loginFormUiState.serverUrl))
        val serverInformation = with(loginFormUiState) {
            DataServerInformation(serverUrl.toString(), httpAuthUsername, httpAuthPassword)
        }
        val urlModule = TinyRssServerInformationModule(serverInformation)
        val networkComponent = networkComponentBuilder
            .tinyRssServerInformationModule(urlModule)
            .build()
        val tinyRssApi = networkComponent.getTinyRssApi()
        val loginPayload = with(loginFormUiState) { LoginRequestPayload(username, password) }

        try {
            startLoginOperation()
            val response = tinyRssApi.login(loginPayload)
            response.checkStatus { "Login failed" }
            onUserLoggedIn()
        } catch (e: ApiCallException) {
            val errorMsgId = when (e.errorCode) {
                ApiCallException.ApiError.LOGIN_FAILED,
                ApiCallException.ApiError.NOT_LOGGED_IN -> R.string.error_login_failed
                ApiCallException.ApiError.API_DISABLED -> R.string.error_api_disabled
                else -> R.string.error_unknown
            }
            snackbarErrorMessageId = errorMsgId
        } catch (e: HttpException) {
            val errorMsgId = when (e.code()) {
                401 -> R.string.error_http_unauthorized
                403 -> R.string.error_http_forbidden
                404 -> R.string.error_http_not_found
                else -> R.string.error_unknown
            }
            snackbarErrorMessageId = errorMsgId
        } catch (e: IOException) {
            val isCleartextTrafficPermitted = NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted
            val errorMsgId = if (!serverUrl.isHttps && !isCleartextTrafficPermitted) {
                R.string.error_cleartext_traffic_not_allowed
            } else {
                R.string.error_unknown
            }
            snackbarErrorMessageId = errorMsgId
        } catch (e: Exception) {
            Timber.e(e, "Failed to login with unexpected exception")
            snackbarErrorMessageId = R.string.error_unknown
        } finally {
            if (snackbarErrorMessageId in listOf(R.string.error_http_forbidden, R.string.error_http_unauthorized)) {
                loginFormUiState.useHttpAuthentication = true
            }
            endLoginOperation()
        }
    }

    @VisibleForTesting
    internal fun startLoginOperation() {
        loginInProgress = true
    }

    @VisibleForTesting
    internal fun endLoginOperation() {
        loginInProgress = false
    }


    private fun onUserLoggedIn() {
        when (action) {
            LoginActivity.ACTION_CONFIRM_CREDENTIALS -> onConfirmCredentialsSuccess()
            LoginActivity.ACTION_ADD_ACCOUNT -> onAddAccountSuccess()
        }
    }

    private fun onConfirmCredentialsSuccess() {
        accountManager.updatePassword(account!!, loginFormUiState.password)
        actionCompleteEvent.value = ActionCompleteSuccessEvent(account!!)
    }

    private fun onAddAccountSuccess() = viewModelScope.launch {
        val result = addAccount()
        actionCompleteEvent.value = if (result != null) {
            ActionCompleteSuccessEvent(result)
        } else {
            ActionCompleteFailedEvent()
        }
    }

    private suspend fun addAccount(): Account? {
        return withContext(dispatchers.io) {
            val account = Account(loginFormUiState.username, loginFormUiState.serverUrl)
            val success = accountManager.addAccount(account, loginFormUiState.password)
            if (success) {
                val serverInformation = with(loginFormUiState) {
                    DataServerInformation(account.url, httpAuthUsername, httpAuthPassword)
                }
                accountManager.updateServerInformation(account, serverInformation)
                accountManager.initializeAccountSync(account)
                return@withContext account
            }
            return@withContext null
        }
    }

    private data class DataServerInformation(
        override val apiUrl: String,
        override val basicHttpAuthUsername: String? = null,
        override val basicHttpAuthPassword: String? = null
    ) : ServerInformation()

    sealed class ActionCompleteEvent {
        class Success(val account: Account) : ActionCompleteEvent()
        object Failed : ActionCompleteEvent()
    }

    private fun ActionCompleteSuccessEvent(account: Account) = Event(ActionCompleteEvent.Success(account))
    private fun ActionCompleteFailedEvent() = Event(ActionCompleteEvent.Failed)

    inner class VMMutableLoginFormUiState : LoginFormUiState{
        private var _serverUrl: String by mutableStateOf("")
        override var serverUrl: String
            get() = _serverUrl
            set(value) {
                _serverUrl = value
                hasEditUrl = true
                updateServerUrlError()
            }

        private var _username: String by mutableStateOf("")
        override var username: String
            get() = _username
            set(value) {
                _username = value
                hasEditUsername = true
                updateUsernameError()
            }

        private var _password: String by mutableStateOf("")
        override var password: String
            get() = _password
            set(value) {
                _password = value
                hasEditPassword = true
                updatePasswordError()
            }

        override var useHttpAuthentication: Boolean by mutableStateOf(false)
        override var httpAuthUsername: String by mutableStateOf("")
        override var httpAuthPassword: String by mutableStateOf("")
        override var canChangeUsernameOrUrl: Boolean by mutableStateOf(true)
        override var serverUrlFieldErrorMsg: Int? by mutableStateOf(null)
        override var usernameFieldErrorMsg: Int? by mutableStateOf(null)
        override var passwordFieldErrorMsg: Int? by mutableStateOf(null)
        override val loginButtonEnabled: Boolean
            get() {
                val hasEditAllFields = hasEditUrl && hasEditUsername && hasEditPassword
                val editionDone =
                    (hasEditAllFields || (!canChangeUsernameOrUrl && hasEditPassword))
                return editionDone && areFieldsCorrect
            }

        private val hasValidServerUrl: Boolean
            get() {
                val url = convertStringToHttpUrl(serverUrl)
                return when {
                    url == null -> false
                    url.pathSegments.last().isNotEmpty() -> false
                    else -> true
                }
            }

        private val hasValidUsername: Boolean
            get() = username.isNotEmpty()

        private val hasValidPassword: Boolean
            get() = password.isNotEmpty()

        val areFieldsCorrect: Boolean
            get() = hasValidServerUrl && hasValidPassword && hasValidUsername

        var hasAttemptLogin by mutableStateOf(false)

        private var hasEditUrl: Boolean by mutableStateOf(false)
        private var hasEditUsername: Boolean by mutableStateOf(false)
        private var hasEditPassword: Boolean by mutableStateOf(false)

        private fun updateUsernameError() {
            val invalidNameMsgId = when {
                username.isEmpty() -> R.string.error_field_required
                else -> null
            }
            if (loginFormUiState.hasAttemptLogin) {
                loginFormUiState.usernameFieldErrorMsg = invalidNameMsgId
            }
        }

        private fun updatePasswordError() {
            val invalidPasswordMsgId = when {
                password.isEmpty() -> R.string.error_field_required
                else -> null
            }

            if (loginFormUiState.hasAttemptLogin) {
                loginFormUiState.passwordFieldErrorMsg = invalidPasswordMsgId
            }
        }

        private fun updateServerUrlError() {
            val url = convertStringToHttpUrl(serverUrl)
            val invalidUrlMsgId = when {
                serverUrl.isEmpty() -> R.string.error_field_required
                url == null -> R.string.error_invalid_http_url
                url.pathSegments.last().isNotEmpty() -> R.string.error_http_url_must_end_wish_slash
                else -> null
            }
            if (hasEditUrl) {
                loginFormUiState.serverUrlFieldErrorMsg = invalidUrlMsgId
            }
        }

        fun initialize(action: String, account: Account? = null) {
            canChangeUsernameOrUrl = action != LoginActivity.ACTION_CONFIRM_CREDENTIALS
            // use private properties to bypass setters checks
            _username = account?.username ?: ""
            _serverUrl = account?.url ?: "https://"
        }
    }

    private fun convertStringToHttpUrl(url: String): HttpUrl? {
        return url.toHttpUrlOrNull()?.newBuilder() // remove fragment and query
            ?.query(null)
            ?.encodedFragment(null)
            ?.build()
    }

}
