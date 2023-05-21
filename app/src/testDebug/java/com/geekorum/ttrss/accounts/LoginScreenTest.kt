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

import androidx.activity.ComponentActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme3
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowLog
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @BeforeTest
    fun setUp() {
        ShadowLog.stream = System.out // Redirect Logcat to console
    }

    @Test
    fun testThatLargeScreenUseLargeLayout() {
        composeTestRule.setContent {
            AppTheme3 {
                val sizeClass = WindowSizeClass.calculateFromSize(DpSize(width=1280.dp, height=800.dp))
                LoginScreen(sizeClass, loginInProgress = false, loginFormUiState = MutableLoginFormUiState(), onLoginClick = {})
            }
        }

        composeTestRule.onNodeWithTag("contentCard").assertIsDisplayed()
    }

    @Test
    fun testThatSmallScreenUseNormalLayout() {
        composeTestRule.setContent {
            AppTheme3 {
                val sizeClass = WindowSizeClass.calculateFromSize(DpSize(width=400.dp, height=700.dp))
                LoginScreen(sizeClass, loginInProgress = false, loginFormUiState = MutableLoginFormUiState(), onLoginClick = {})
            }
        }

        composeTestRule.onNodeWithTag("contentCard").assertDoesNotExist()
    }

    @Test
    fun testErrorsMessageAreDisplayed() {
        composeTestRule.setContent {
            AppTheme3 {
                val sizeClass = WindowSizeClass.calculateFromSize(DpSize(width=400.dp, height=700.dp))
                val uiState = MutableLoginFormUiState().apply {
                    serverUrlFieldErrorMsg = R.string.error_invalid_http_url
                    usernameFieldErrorMsg = R.string.error_field_required
                    passwordFieldErrorMsg = R.string.error_field_required
                }
                LoginScreen(sizeClass, loginInProgress = false, loginFormUiState = uiState, onLoginClick = {})
            }
        }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.error_invalid_http_url)).assertIsDisplayed()
        composeTestRule.onAllNodesWithText(composeTestRule.activity.getString(R.string.error_field_required))
            .assertCountEquals(2)
    }

    @Test
    fun testHttpAuthFieldsAreDisplayedWhenHttpAuthIsUsed() {
        val uiState = MutableLoginFormUiState()
        composeTestRule.setContent {
            AppTheme3 {
                val sizeClass = WindowSizeClass.calculateFromSize(DpSize(width=400.dp, height=700.dp))
                LoginScreen(sizeClass, loginInProgress = false, loginFormUiState = uiState, onLoginClick = {})
            }
        }

        composeTestRule.onAllNodesWithText(composeTestRule.activity.getString(R.string.prompt_username))
            .assertCountEquals(1)
        composeTestRule.onAllNodesWithText(composeTestRule.activity.getString(R.string.prompt_password))
            .assertCountEquals(1)

        composeTestRule.onNode(isToggleable())
            .performScrollTo()
            .assertIsOff()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNode(isToggleable()).assertIsOn()
        composeTestRule.onAllNodesWithText(composeTestRule.activity.getString(R.string.prompt_username))
            .assertCountEquals(2)
        composeTestRule.onAllNodesWithText(composeTestRule.activity.getString(R.string.prompt_password))
            .assertCountEquals(2)
    }

    @Test
    fun testCircularLoadingIsDisplayed() {
        composeTestRule.setContent {
            AppTheme3 {
                val uiState = MutableLoginFormUiState().apply {
                    loginButtonEnabled = true
                }
                var loginInProgress by remember { mutableStateOf(false) }
                val sizeClass = WindowSizeClass.calculateFromSize(DpSize(width=400.dp, height=700.dp))
                LoginScreen(sizeClass, loginInProgress = loginInProgress, loginFormUiState = uiState, onLoginClick = {
                    loginInProgress = true
                })
            }
        }

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertDoesNotExist()

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.action_sign_in))
            .performScrollTo()
            .assertIsEnabled()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }


    @Test
    fun testUsernameUrlFieldsAreDisabled() {
        composeTestRule.setContent {
            AppTheme3 {
                val uiState = MutableLoginFormUiState().apply {
                    canChangeUsernameOrUrl = false
                }
                val sizeClass = WindowSizeClass.calculateFromSize(DpSize(width=400.dp, height=700.dp))
                LoginScreen(sizeClass, loginInProgress = false, loginFormUiState = uiState, onLoginClick = {})
            }
        }

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertDoesNotExist()

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.prompt_username))
            .assertIsNotEnabled()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.prompt_url))
            .assertIsNotEnabled()
            .assertIsDisplayed()

    }

}