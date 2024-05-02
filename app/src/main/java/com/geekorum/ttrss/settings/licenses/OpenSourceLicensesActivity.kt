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
package com.geekorum.ttrss.settings.licenses

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.geekorum.ttrss.ui.AppTheme3
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@AndroidEntryPoint
class OpenSourceLicensesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme3 {
                DependencyNavHost(
                    navigateUp = {
                        onNavigateUp()
                    }
                )
            }
        }
    }
}

@Serializable
private object DependenciesListDestination

@Serializable
private data class DependenciesLicenseDestination(
    val dependency: String
)

@Composable
fun DependencyNavHost(
    openSourceLicensesViewModel: OpenSourceLicensesViewModel = hiltViewModel(),
    navigateUp: () -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = DependenciesListDestination) {
        composable<DependenciesListDestination> {
            OpenSourceDependenciesListScreen(
                viewModel = openSourceLicensesViewModel,
                onDependencyClick = {
                    val route = DependenciesLicenseDestination(it)
                    navController.navigate(route)
                },
                onUpClick = navigateUp
            )
        }
        composable<DependenciesLicenseDestination> {
            val dependency = it.toRoute<DependenciesLicenseDestination>().dependency
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
