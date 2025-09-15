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
package com.geekorum.ttrss.settings.manage_features

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.geekorum.ttrss.core.BaseActivity
import com.geekorum.ttrss.on_demand_modules.InstallModuleViewModel
import com.geekorum.ttrss.on_demand_modules.InstallSession.State.Status.*
import com.geekorum.ttrss.ui.AppTheme3
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Quick and dirty InstallFeatureActivity
 */
@AndroidEntryPoint
class InstallFeatureActivity : BaseActivity() {

    companion object {
        const val EXTRA_FEATURES_LIST = "features"
        const val CODE_REQUEST_USER_CONFIRMATION = 1
    }

    val viewModel: InstallModuleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme3 {
                InstallFeatureScreen(activity = this, viewModel, onAppInstalled = {
                    setResult(RESULT_OK)
                    lifecycleScope.launch {
                        delay(1000)
                        finish()
                    }
                })
            }
        }

        val features = intent.getStringArrayExtra(EXTRA_FEATURES_LIST) ?: emptyArray()
        viewModel.startInstallModules(*features)
    }
}


@Composable
fun InstallFeatureScreen(
    activity: Activity,
    viewModel: InstallModuleViewModel = hiltViewModel(),
    onAppInstalled: () -> Unit,
) {
    val installProgression by viewModel.progress.collectAsStateWithLifecycle()
    val installSessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val animate = when (installSessionState.status) {
        FAILED, CANCELED -> false
        else -> true
    }

    LaunchedEffect(installSessionState) {
        when (installSessionState.status) {
            INSTALLED -> onAppInstalled()
            REQUIRES_USER_CONFIRMATION -> {
                viewModel.startUserConfirmationDialog(activity,
                    InstallFeatureActivity.CODE_REQUEST_USER_CONFIRMATION
                )
            }
            else -> Unit
        }
    }

    InstallFeatureScreen(
        installationMessage = stringResource(installProgression.message),
        indeterminateProgress = installProgression.progressIndeterminate,
        progress = installProgression.progress,
        progressMax = installProgression.max,
        animate = animate,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun InstallFeatureScreen(
    installationMessage: String,
    indeterminateProgress: Boolean,
    progress: Int,
    progressMax: Int,
    animate: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(modifier) {
        // we could use rememberInfiniteTransition but we wanted the animation to stop gracefully
        val logoAnimation = remember { Animatable(0f) }

        var currentAnimationVelocity by  remember { mutableFloatStateOf(0f) }
        LaunchedEffect(animate) {
            if (!animate) {
                logoAnimation.animateTo(
                    360f,
                    tween(durationMillis = 800),
                    initialVelocity = currentAnimationVelocity
                )
            } else {
                try {
                    while (true) {
                        logoAnimation.snapTo(0f)
                        logoAnimation.animateTo(
                            360f, tween(durationMillis = 2000)
                        )
                    }
                } catch (e: CancellationException) {
                    currentAnimationVelocity = logoAnimation.velocity
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally) {

            Image(imageVector = Icons.Default.Settings, contentDescription = null,
                contentScale = ContentScale.FillWidth,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(200.dp)
                    .rotate(logoAnimation.value)
            )

            Text(installationMessage,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 32.dp))

            if (indeterminateProgress) {
                LinearProgressIndicator(
                    modifier = Modifier.padding(vertical = 16.dp))
            } else {
                LinearProgressIndicator(progress = { progress / progressMax.toFloat() },
                    modifier = Modifier.padding(vertical = 16.dp))
            }
        }
    }
}


@Preview
@Composable
fun PreviewInstallFeatureScreen() {
    AppTheme3 {
        var animate by remember { mutableStateOf(true) }
        InstallFeatureScreen(
            installationMessage = "Installation en cours",
            indeterminateProgress = false,
            progress = 33,
            progressMax = 100,
            animate = animate,
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    animate = !animate
                }
        )
    }
}

