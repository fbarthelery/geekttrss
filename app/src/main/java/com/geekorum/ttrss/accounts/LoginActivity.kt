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

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.geekorum.geekdroid.accounts.AccountAuthenticatorAppCompatActivity
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * A Login screen to a Tinytinyrss server.
 */
@AndroidEntryPoint
class LoginActivity : AccountAuthenticatorAppCompatActivity() {

    companion object {
        const val ACTION_ADD_ACCOUNT = "add_account"
        const val ACTION_CONFIRM_CREDENTIALS = "confirm_credentials"

        const val EXTRA_ACCOUNT = "account"
    }

    @Inject
    lateinit var accountManager: AndroidTinyrssAccountManager

    private val loginViewModel: LoginViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val account = IntentCompat.getParcelableExtra(intent, EXTRA_ACCOUNT, Account::class.java)?.let {
            accountManager.fromAndroidAccount(it)
        }
        val action = requireNotNull(intent?.action) { "Invalid intent action passed to $this"}
        loginViewModel.initialize(action, account)

        loginViewModel.actionCompleteEvent.observe(this, EventObserver { event->
            when (event) {
                is LoginViewModel.ActionCompleteEvent.Failed -> handleActionFailed()
                is LoginViewModel.ActionCompleteEvent.Success -> handleActionSuccess(event)
            }
        })

        setContent {
            AppTheme {
                LoginScreen(windowSizeClass = calculateWindowSizeClass(this@LoginActivity),
                    viewModel = loginViewModel)
            }
        }
    }

    private fun handleActionSuccess(event: LoginViewModel.ActionCompleteEvent.Success) {
        accountAuthenticatorResult = Bundle().apply {
            when (intent.action) {
                ACTION_ADD_ACCOUNT -> {
                    putString(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.TTRSS_ACCOUNT_TYPE)
                    putString(AccountManager.KEY_ACCOUNT_NAME, event.account.username)
                }
                ACTION_CONFIRM_CREDENTIALS -> putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true)
            }
        }
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun handleActionFailed() {
        accountAuthenticatorResult = Bundle().apply {
            when (intent.action) {
                ACTION_ADD_ACCOUNT -> {
                    putInt(AccountManager.KEY_ERROR_CODE, AccountAuthenticator.ERROR_CODE_AUTHENTICATOR_FAILURE)
                    putString(AccountManager.KEY_ERROR_MESSAGE, "Unable to add account")
                }
                ACTION_CONFIRM_CREDENTIALS -> putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
            }
        }
        finish()
    }

}


@Composable
internal fun LoginScreen(windowSizeClass: WindowSizeClass, viewModel: LoginViewModel = hiltViewModel()) {
    val scaffoldState = rememberScaffoldState()
    LoginScreen(
        windowSizeClass = windowSizeClass,
        loginInProgress = viewModel.loginInProgress,
        loginFormUiState = viewModel.loginFormUiState,
        scaffoldState = scaffoldState,
        onLoginClick = {
        viewModel.confirmLogin()
    })

    viewModel.snackbarErrorMessageId?.let { messageId ->
        val message = stringResource(messageId)
        LaunchedEffect(message) {
            scaffoldState.snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }
}

@Composable
fun LoginScreen(
    windowSizeClass: WindowSizeClass,
    loginInProgress: Boolean,
    loginFormUiState: LoginFormUiState,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    onLoginClick: () -> Unit
) {
    val useTabletLayout = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                elevation = if (useTabletLayout) 0.dp else AppBarDefaults.TopAppBarElevation,
                title = {
                    Text("Login")
                })
        },
    ) {
        if (useTabletLayout) {
            TabletLayoutContent(Modifier.padding(it)) {
                Crossfade(loginInProgress, label = "CircularProgressCrossfade") { showLoginProgress ->
                    if (showLoginProgress) {
                        CircularProgressIndicator(
                            Modifier
                                .fillMaxWidth()
                                .wrapContentWidth()
                                .size(64.dp)
                                .padding(top = 56.dp)
                        )
                    } else {
                        LoginForm(loginFormUiState, onLoginClick = onLoginClick, Modifier.padding(16.dp))
                    }
                }
            }
        } else {
            Crossfade(loginInProgress, label = "CircularProgressCrossfade",
                modifier = Modifier.padding(it)
            ) { showLoginProgress ->
                if (showLoginProgress) {
                    CircularProgressIndicator(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentWidth()
                            .size(64.dp)
                            .padding(top = 56.dp)
                    )
                } else {
                    LoginForm(loginFormUiState, onLoginClick = onLoginClick, Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TabletLayoutContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .height(136.dp) // 192.dp - 56.dp of appbar
                .fillMaxWidth()
                .background(MaterialTheme.colors.primarySurface)
        )

        Card(
            Modifier
                .width(560.dp)
                .fillMaxSize(),
            content = content
        )
    }
}

@Stable
interface LoginFormUiState {
    var serverUrl: String
    var username: String
    var password: String
    var useHttpAuthentication: Boolean
    var httpAuthUsername: String
    var httpAuthPassword: String
    val canChangeUsernameOrUrl: Boolean
    val serverUrlFieldErrorMsg: Int?
    val usernameFieldErrorMsg: Int?
    val passwordFieldErrorMsg: Int?
    val loginButtonEnabled: Boolean
}

class MutableLoginFormUiState : LoginFormUiState{
    override var serverUrl: String by mutableStateOf("")
    override var username: String  by mutableStateOf("")
    override var password: String by mutableStateOf("")
    override var useHttpAuthentication: Boolean by mutableStateOf(false)
    override var httpAuthUsername: String by mutableStateOf("")
    override var httpAuthPassword: String by mutableStateOf("")
    override var canChangeUsernameOrUrl: Boolean by mutableStateOf(true)
    override var serverUrlFieldErrorMsg: Int? by mutableStateOf(null)
    override var usernameFieldErrorMsg: Int? by mutableStateOf(null)
    override var passwordFieldErrorMsg: Int? by mutableStateOf(null)
    override var loginButtonEnabled: Boolean by mutableStateOf(false)
}

@Composable
private fun LoginForm(
    uiState: LoginFormUiState,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextFieldWithError(
            value = uiState.serverUrl, onValueChange = uiState::serverUrl::set,
            enabled = uiState.canChangeUsernameOrUrl,
            label = {
                Text(stringResource(R.string.prompt_url))
            },
            leadingIcon = {
                Icon(Icons.Default.Web, contentDescription = null)
            },
            errorId = uiState.serverUrlFieldErrorMsg,
            keyboardOptions= KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next)
        )

        OutlinedTextFieldWithError(
            value = uiState.username, onValueChange = uiState::username::set,
            enabled = uiState.canChangeUsernameOrUrl,
            label = {
                Text(stringResource(R.string.prompt_username))
            },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            errorId = uiState.usernameFieldErrorMsg,
            keyboardOptions= KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.padding(top = 16.dp)
        )

        OutlinedTextFieldWithError(
            value = uiState.password, onValueChange = uiState::password::set,
            label = {
                Text(stringResource(R.string.prompt_password))
            },
            leadingIcon = {
                Icon(Icons.Default.Password, contentDescription = null)
            },
            errorId = uiState.passwordFieldErrorMsg,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions= KeyboardOptions(keyboardType = KeyboardType.Password,
                imeAction = if (uiState.useHttpAuthentication) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                onLoginClick()
            }),
            modifier = Modifier.padding(top = 16.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .width(280.dp)
            .padding(top = 16.dp)) {
            Text(stringResource(R.string.lbl_use_http_authentication), style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.weight(1f))
            Switch(checked = uiState.useHttpAuthentication,
                onCheckedChange = { uiState.useHttpAuthentication = it })
        }

        AnimatedVisibility(uiState.useHttpAuthentication) {
            Column {
                OutlinedTextField(
                    value = uiState.httpAuthUsername, onValueChange = uiState::httpAuthUsername::set,
                    placeholder = {
                        Text(stringResource(R.string.prompt_username))
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.padding(top = 16.dp)
                )

                OutlinedTextField(
                    value = uiState.httpAuthPassword, onValueChange = uiState::httpAuthPassword::set,
                    placeholder = {
                        Text(stringResource(R.string.prompt_password))
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Password, contentDescription = null)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        onLoginClick()
                    }),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        Button(
            onClick = onLoginClick,
            enabled = uiState.loginButtonEnabled,
            modifier = Modifier.padding(vertical = 16.dp)) {
            Text(stringResource(R.string.action_sign_in))
        }
    }
}

@Composable
private fun OutlinedTextFieldWithError(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    @StringRes
    errorId: Int? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Column(modifier) {
        OutlinedTextField(value = value, onValueChange = onValueChange,
            enabled = enabled,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            isError = errorId != null,
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions= keyboardOptions,
            keyboardActions = keyboardActions
        )
        val errorMsg = errorId?.let { stringResource(id = errorId) } ?: ""
        Text(errorMsg, style = MaterialTheme.typography.caption, modifier = Modifier.padding(top = 4.dp))
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
fun PreviewLoginScreen() {
    BoxWithConstraints {
        val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight) )
        AppTheme {
            LoginScreen(windowSizeClass, loginInProgress = false,
                loginFormUiState = MutableLoginFormUiState(),
                onLoginClick = {})
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun PreviewLoginScreenTablet() {
    BoxWithConstraints {
        val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight) )
        AppTheme {
            LoginScreen(windowSizeClass, loginInProgress = false,
                loginFormUiState = MutableLoginFormUiState(),
                onLoginClick = {})
        }
    }
}