/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.doOnNextLayout
import androidx.core.view.updatePadding
import androidx.lifecycle.LifecycleOwner
import com.geekorum.geekdroid.views.doOnApplyWindowInsets
import com.geekorum.ttrss.R
import com.geekorum.ttrss.in_app_update.InAppUpdateViewModel
import com.geekorum.ttrss.in_app_update.IntentSenderForResultStarter
import com.geekorum.ttrss.ui.AppTheme
import com.google.accompanist.drawablepainter.rememberDrawablePainter

private const val CODE_START_IN_APP_UPDATE = 1

/**
 * Presenter for InAppUpdates.
 * Really tied to ArticleListActivity
 */
class InAppUpdatePresenter(
    private val composeView: ComposeView,
    private val lifecyleOwner: LifecycleOwner,
    private val inAppUpdateViewModel: InAppUpdateViewModel,
    activityResultRegistry: ActivityResultRegistry,
) {

    private val inAppUpdateLauncher = registerInAppUpdateLauncher(
        ActivityResultContracts.StartIntentSenderForResult(),
        activityResultRegistry
    ) {
        if (it.resultCode != Activity.RESULT_OK) {
            inAppUpdateViewModel.cancelUpdateFlow()
        }
    }

    private val intentSenderForResultStarter = object : IntentSenderForResultStarter {
        override fun startIntentSenderForResult(intent: IntentSender, requestCode: Int, fillInIntent: Intent?, flagsMask: Int, flagsValues: Int, extraFlags: Int, options: Bundle?) {
            val intentSenderRequest = IntentSenderRequest.Builder(intent)
                .setFillInIntent(fillInIntent)
                .setFlags(flagsValues, flagsMask)
                .build()
            inAppUpdateLauncher.launch(intentSenderRequest)
        }
    }

    init {
        composeView.fitsSystemWindows = true
        composeView.doOnApplyWindowInsets { _, windowInsetsCompat, _ ->
            windowInsetsCompat
        }
        composeView.setContent {
            Content()
        }
    }

    @Composable
    fun Content() {
        AppTheme {
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
                modifier = Modifier.layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    sheetHeigth = placeable.height
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, 0)
                    }
                }
            ) {
                SheetContent(
                    isUpdateAvailable = isUpdateAvailable,
                    isUpdateReadyToInstalll = isUpdateReadyToInstall,
                    dismissSheet = {
                        showBanner = false
                    }
                )
            }

            // needed to add additional padding to articles list
            LaunchedEffect(showBanner, sheetHeigth) {
                val paddingBottom = if (showBanner) sheetHeigth else 0
                (composeView.parent as? View)?.doOnNextLayout { parent ->
                    val fragmentContainerView = parent.findViewById<View>(R.id.middle_pane_layout)
                    fragmentContainerView?.updatePadding(bottom = paddingBottom)
                }
            }
        }
    }

    @Composable
    private fun SheetContent(
        isUpdateAvailable: Boolean,
        isUpdateReadyToInstalll: Boolean,
        dismissSheet: () -> Unit,
    ) {
        Card(shape = RoundedCornerShape(0.dp),
            backgroundColor =  if (MaterialTheme.colors.isLight)
                colorResource(R.color.material_blue_grey_100)
            else MaterialTheme.colors.surface,
            elevation = 8.dp,
        ) {
            Box(modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .fillMaxWidth(),
                propagateMinConstraints = true
            ) {
                when {
                    isUpdateReadyToInstalll -> {
                        UpdateReadyBanner(onRestartClick = {
                            inAppUpdateViewModel.completeUpdate()
                            dismissSheet()
                        })
                    }
                    isUpdateAvailable -> {
                        UpdateAvailableBanner(onInstallClick = {
                            inAppUpdateViewModel.startUpdateFlow(intentSenderForResultStarter,
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
            Row(Modifier
                .padding(top = 24.dp)
                .padding(horizontal = 16.dp)) {
                val painter = rememberMipmapPainter(R.mipmap.ic_launcher)

                Image(painter = painter, contentDescription = null,
                    modifier = Modifier.size(56.dp)
                )
                Text(stringResource(R.string.banner_update_msg),
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(start = 16.dp))
            }

            AppTheme(
                colors = MaterialTheme.colors.copy(primary = MaterialTheme.colors.secondary)
            ) {
                Row(Modifier
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
    }

    @Composable
    private fun UpdateReadyBanner(onRestartClick: () -> Unit) {
        Column {
            Row(Modifier
                .padding(top = 24.dp)
                .padding(horizontal = 16.dp)) {
                Image(painter = rememberMipmapPainter(R.mipmap.ic_launcher), contentDescription = null,
                    Modifier.size(56.dp))
                Text(stringResource(R.string.banner_update_install_msg),
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(start = 16.dp))
            }
            AppTheme(
                colors = MaterialTheme.colors.copy(primary = MaterialTheme.colors.secondary)
            ) {
                TextButton(onClick = onRestartClick,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 12.dp, end = 8.dp, bottom = 8.dp)
                ) {
                    Text(stringResource(R.string.banner_install_btn))
                }
            }
        }
    }


    private fun <I, O> registerInAppUpdateLauncher(
        contract: ActivityResultContract<I, O>,
        registry: ActivityResultRegistry,
        callback: ActivityResultCallback<O>): ActivityResultLauncher<I> {
        return registry.register(
            "in_app_update_presenter", lifecyleOwner, contract, callback)
    }

    @Composable
    private fun rememberMipmapPainter(@DrawableRes mipmapId: Int): Painter {
        return rememberDrawablePainter(drawable = AppCompatResources.getDrawable(LocalContext.current, mipmapId))
    }

}
