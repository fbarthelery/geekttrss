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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.geekorum.ttrss.R

@Composable
fun OpenSourceDependenciesListScreen(
    viewModel: OpenSourceLicensesViewModel = hiltViewModel(),
    onDependencyClick: (String) -> Unit,
    onUpClick: () -> Unit
) {
    val dependencies by viewModel.dependenciesList.collectAsState(initial = emptyList())
    OpenSourceDependenciesListScreen(
        dependencies = dependencies,
        onDependencyClick = onDependencyClick,
        onUpClick = onUpClick
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OpenSourceDependenciesListScreen(
    dependencies: List<String>,
    onDependencyClick: (String) -> Unit,
    onUpClick: () -> Unit
) {
    val lazyListState = rememberLazyListState()
    val hasScrolled by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex  != 0 || lazyListState.firstVisibleItemScrollOffset > 0
        }
    }
    val topBarElevation by animateDpAsState(
        if (hasScrolled) 4.dp else 0.dp
    )
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.pref_title_oss_license)) },
            navigationIcon = {
                IconButton(onClick = onUpClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            },
            elevation = topBarElevation
        )
    }) {
        LazyColumn(Modifier.fillMaxSize(), state = lazyListState, contentPadding = it) {
            items(dependencies) {
                Column {
                    ListItem(
                        Modifier
                            .height(64.dp)
                            .clickable(onClick = { onDependencyClick(it) })
                    ) {
                        Text(
                            it, modifier = Modifier.padding(horizontal = 16.dp),
                            overflow = TextOverflow.Ellipsis, maxLines = 1
                        )
                    }
                    Divider(Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}
