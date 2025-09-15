/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
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

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme3

@Composable
internal fun LoginScreen(windowSizeClass: WindowSizeClass, viewModel: LoginViewModel = hiltViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    LoginScreen(
        windowSizeClass = windowSizeClass,
        loginInProgress = viewModel.loginInProgress,
        loginFormUiState = viewModel.loginFormUiState,
        onLoginClick = {
            viewModel.confirmLogin()
        })

    viewModel.snackbarErrorMessageId?.let { messageId ->
        val message = stringResource(messageId)
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    windowSizeClass: WindowSizeClass,
    loginInProgress: Boolean,
    loginFormUiState: LoginFormUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onLoginClick: () -> Unit
) {
    val useTabletLayout = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState)  },
        topBar = {
            val colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                colors = colors,
                title = {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
                })
        },
    ) {
        if (useTabletLayout) {
            TabletLayoutContent(Modifier.padding(it)) {
                Crossfade(
                    loginInProgress,
                    label = "CircularProgressCrossfade"
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
                        LoginForm(
                            loginFormUiState,
                            onLoginClick = onLoginClick,
                            Modifier.padding(16.dp)
                        )
                    }
                }
            }
        } else {
            Crossfade(
                loginInProgress, label = "CircularProgressCrossfade",
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
                    LoginForm(
                        loginFormUiState,
                        onLoginClick = onLoginClick,
                        Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TabletLayoutContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .height(136.dp) // 192.dp - 56.dp of appbar
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .testTag("fakeAppBar")
        )

        ElevatedCard(
            Modifier
                .width(560.dp)
                .fillMaxSize()
                .testTag("contentCard"),
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

class MutableLoginFormUiState : LoginFormUiState {
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
            )
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (uiState.useHttpAuthentication) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                onLoginClick()
            }),
            modifier = Modifier.padding(top = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .width(280.dp)
                .padding(top = 16.dp)
        ) {
            Text(
                stringResource(R.string.lbl_use_http_authentication),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(checked = uiState.useHttpAuthentication,
                onCheckedChange = { uiState.useHttpAuthentication = it })
        }

        AnimatedVisibility(uiState.useHttpAuthentication) {
            Column {
                OutlinedTextField(
                    value = uiState.httpAuthUsername,
                    onValueChange = uiState::httpAuthUsername::set,
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
                    value = uiState.httpAuthPassword,
                    onValueChange = uiState::httpAuthPassword::set,
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
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
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
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            enabled = enabled,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            isError = errorId != null,
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
        val errorMsg = errorId?.let { stringResource(id = errorId) } ?: ""
        Text(
            errorMsg,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(device = Devices.TABLET)
@Preview(device = Devices.TABLET,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun PreviewLoginScreen() {
    BoxWithConstraints {
        val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
        AppTheme3 {
            LoginScreen(windowSizeClass, loginInProgress = false,
                loginFormUiState = MutableLoginFormUiState(),
                onLoginClick = {})
        }
    }
}
