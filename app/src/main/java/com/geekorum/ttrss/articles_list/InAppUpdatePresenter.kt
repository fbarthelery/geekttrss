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
package com.geekorum.ttrss.articles_list

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.geekorum.ttrss.R
import com.geekorum.ttrss.in_app_update.InAppUpdateViewModel
import com.geekorum.ttrss.in_app_update.IntentSenderForResultStarter
import com.google.accompanist.drawablepainter.rememberDrawablePainter

private const val CODE_START_IN_APP_UPDATE = 1

/**
 * Presenter for InAppUpdates.
 */
class InAppUpdatePresenter(
    private val inAppUpdateViewModel: InAppUpdateViewModel,
) {

    class InAppUpdateIntentSenderForResultStarter(
        private val inAppUpdateLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) : IntentSenderForResultStarter {
        override fun startIntentSenderForResult(intent: IntentSender, requestCode: Int, fillInIntent: Intent?, flagsMask: Int, flagsValues: Int, extraFlags: Int, options: Bundle?) {
            val intentSenderRequest = IntentSenderRequest.Builder(intent)
                .setFillInIntent(fillInIntent)
                .setFlags(flagsValues, flagsMask)
                .build()
            inAppUpdateLauncher.launch(intentSenderRequest)
        }
    }

    @Composable
    fun Content(modifier: Modifier = Modifier) {
        val isUpdateAvailable by inAppUpdateViewModel.isUpdateAvailable.collectAsState(false )
        val isUpdateReadyToInstall by inAppUpdateViewModel.isUpdateReadyToInstall.collectAsState(false )
        var showBanner by remember { mutableStateOf(false) }
        LaunchedEffect(isUpdateAvailable, isUpdateReadyToInstall) {
            showBanner = isUpdateAvailable || isUpdateReadyToInstall
        }

        var sheetHeigth by remember { mutableStateOf(0) }
        AnimatedVisibility(showBanner,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it}),
            modifier = modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                sheetHeigth = placeable.height
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            }
        ) {
            SheetContent(
                isUpdateAvailable = isUpdateAvailable,
                isUpdateReadyToInstall = isUpdateReadyToInstall,
                dismissSheet = {
                    showBanner = false
                }
            )
        }
    }

    @Composable
    private fun SheetContent(
        isUpdateAvailable: Boolean,
        isUpdateReadyToInstall: Boolean,
        dismissSheet: () -> Unit,
    ) {
        val appUpdateLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult() ) {
            if (it.resultCode != Activity.RESULT_OK) {
                inAppUpdateViewModel.cancelUpdateFlow()
            }
        }

        Card(
            shape = RoundedCornerShape(0.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .fillMaxWidth(),
                propagateMinConstraints = true
            ) {
                when {
                    isUpdateReadyToInstall -> {
                        UpdateReadyBanner(onRestartClick = {
                            inAppUpdateViewModel.completeUpdate()
                            dismissSheet()
                        })
                    }
                    isUpdateAvailable -> {
                        UpdateAvailableBanner(onInstallClick = {
                            val intentStarter = InAppUpdateIntentSenderForResultStarter(appUpdateLauncher)
                            inAppUpdateViewModel.startUpdateFlow(intentStarter,
                                CODE_START_IN_APP_UPDATE)
                            dismissSheet()
                        },
                            onDismissClick = {
                                dismissSheet()
                            })
                    }
                    else -> Unit
                }
            }
        }
    }

    @Composable
    private fun UpdateAvailableBanner(onInstallClick: () -> Unit, onDismissClick: () -> Unit) {
        Column {
            Row(
                Modifier
                    .padding(top = 24.dp)
                    .padding(horizontal = 16.dp)) {
                val painter = rememberMipmapPainter(R.mipmap.ic_launcher)

                Image(painter = painter, contentDescription = null,
                    modifier = Modifier.size(56.dp)
                )
                Text(stringResource(R.string.banner_update_msg),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp))
            }

            Row(
                Modifier
                    .align(Alignment.End)
                    .padding(top = 12.dp, end = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDismissClick) {
                    Text(stringResource(R.string.banner_dismiss_btn))
                }

                TextButton(onClick = onInstallClick) {
                    Text(stringResource(R.string.banner_update_btn))
                }
            }
        }
    }

    @Composable
    private fun UpdateReadyBanner(onRestartClick: () -> Unit) {
        Column {
            Row(
                Modifier
                    .padding(top = 24.dp)
                    .padding(horizontal = 16.dp)) {
                Image(painter = rememberMipmapPainter(R.mipmap.ic_launcher), contentDescription = null,
                    Modifier.size(56.dp))
                Text(stringResource(R.string.banner_update_install_msg),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp))
            }
            TextButton(onClick = onRestartClick,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 12.dp, end = 8.dp, bottom = 8.dp)
            ) {
                Text(stringResource(R.string.banner_install_btn))
            }
        }
    }

    @Composable
    private fun rememberMipmapPainter(@DrawableRes mipmapId: Int): Painter {
        return rememberDrawablePainter(drawable = AppCompatResources.getDrawable(LocalContext.current, mipmapId))
    }

}