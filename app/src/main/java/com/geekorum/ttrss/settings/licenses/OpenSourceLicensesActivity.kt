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
package com.geekorum.ttrss.settings.licenses

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geekorum.ttrss.ui.AppTheme3
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OpenSourceLicensesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AppTheme3 {
                val sysUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                DisposableEffect(sysUiController, useDarkIcons) {
                    sysUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
                    onDispose {  }
                }
                DependencyNavHost(
                    navigateUp = {
                        onNavigateUp()
                    }
                )
            }
        }
    }
}


@Composable
fun DependencyNavHost(
    openSourceLicensesViewModel: OpenSourceLicensesViewModel = hiltViewModel(),
    navigateUp: () -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "dependencies") {
        composable("dependencies") {
            OpenSourceDependenciesListScreen(
                viewModel = openSourceLicensesViewModel,
                onDependencyClick = {
                    navController.navigate("dependency_license/${Uri.encode(it)}")
                },
                onUpClick = navigateUp
            )
        }
        composable("dependency_license/{dependency}") {
            val dependency = requireNotNull(it.arguments?.getString("dependency"))
            OpenSourceLicenseScreen(
                viewModel = openSourceLicensesViewModel,
                dependency = dependency,
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
    }
}

