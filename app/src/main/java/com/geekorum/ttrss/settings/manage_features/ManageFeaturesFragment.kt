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
package com.geekorum.ttrss.settings.manage_features

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme3
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageFeaturesFragment : Fragment() {

    private val viewModel: ManageFeaturesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme3 {
                    ManageFeaturesScreen(viewModel, onInstallFeatureClick = {
                        val intent = Intent(context, InstallFeatureActivity::class.java).apply {
                            putExtra(InstallFeatureActivity.EXTRA_FEATURES_LIST, arrayOf(it))
                        }
                        startActivity(intent)
                    })
                }
            }
        }
    }
}

@Composable
fun ManageFeaturesScreen(
    viewModel: ManageFeaturesViewModel,
    onInstallFeatureClick: (FeatureStatus) -> Unit
) {
    val features by viewModel.features.collectAsStateWithLifecycle()
    ManageFeaturesScreen(
        features = features,
        isEditable = viewModel.canModify,
        onInstallFeatureClick = onInstallFeatureClick,
        onUninstallFeatureClick = {
            viewModel.uninstallModule(it.name)
        }
    )
}

@Composable
fun ManageFeaturesScreen(
    features: List<FeatureStatus>, isEditable: Boolean,
    onInstallFeatureClick: (FeatureStatus) -> Unit,
    onUninstallFeatureClick: (FeatureStatus) -> Unit
) {
    Scaffold { contentPadding ->
        LazyColumn(contentPadding = contentPadding) {
            itemsIndexed(
                items = features,
                key = { _, feat -> feat.name }
            ) { idx, feature ->
                Column(Modifier.fillMaxWidth()) {
                    FeatureItem(feature, isEditable,
                        onInstallClick = {
                            onInstallFeatureClick(feature)
                        },
                        onUninstallClick = {
                            onUninstallFeatureClick(feature)
                        }
                    )
                    if (idx < features.lastIndex) {
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(
    feature: FeatureStatus,
    isEditable: Boolean,
    onInstallClick: () -> Unit,
    onUninstallClick: () -> Unit
) {
    ListItem(trailingContent = {
        if (feature.installed && isEditable) {
            TextButton(onClick = onUninstallClick) {
                Text(stringResource(R.string.btn_uninstall_feature))
            }
        } else if (!feature.installed) {
            TextButton(onClick = onInstallClick) {
                Text(stringResource(R.string.btn_install_feature))
            }
        }
    },
        headlineContent = {
            Text(feature.name)
        })
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Preview
@Composable
private fun PreviewManageFeaturesScreen() {
    AppTheme3 {
        val features = listOf(
            FeatureStatus("manage feeds", true),
            FeatureStatus("podcast feeds", false),
            FeatureStatus("video feeds", false),
        )
        ManageFeaturesScreen(features, isEditable = true,
            onInstallFeatureClick = {}, onUninstallFeatureClick = {})
    }
}