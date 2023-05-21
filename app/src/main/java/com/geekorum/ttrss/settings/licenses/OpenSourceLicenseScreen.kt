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

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OpenSourceLicenseScreen(
    viewModel: OpenSourceLicensesViewModel = hiltViewModel(),
    dependency: String,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val license by viewModel.getLicenseDependency(dependency).collectAsState("")
    OpenSourceLicenseScreen(
        dependency = dependency,
        license = license,
        onBackClick = onBackClick,
        onUrlClick = {
            viewModel.openLinkInBrowser(context, it)
        },
        onUrlsFound = {
            val uris = it.map { uri -> uri.toUri() }
            viewModel.mayLaunchUrl(*uris.toTypedArray())
        }
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalTextApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicenseScreen(
    dependency: String,
    license: String,
    onBackClick: () -> Unit,
    onUrlClick: (String) -> Unit,
    onUrlsFound: (List<String>) -> Unit,
) {
    val linkifiedLicense = linkifyText(text = license)
    LaunchedEffect(linkifiedLicense) {
        val uris =
            linkifiedLicense.getUrlAnnotations(0, linkifiedLicense.length).map { it.item.url }
        onUrlsFound(uris)
    }

    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
        TopAppBar(title = { Text(dependency, overflow = TextOverflow.Ellipsis, maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            },
        )
    }) { paddingValues ->
        val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
        val pressIndicator = Modifier.pointerInput(layoutResult, linkifiedLicense) {
            detectTapGestures { pos ->
                layoutResult.value?.let { layoutResult ->
                    val posWithScroll = pos.copy(y = pos.y + scrollState.value)
                    val offset = layoutResult.getOffsetForPosition(posWithScroll)
                    linkifiedLicense.getUrlAnnotations(start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            onUrlClick(annotation.item.url)
                        }
                }
            }
        }

        Text(linkifiedLicense,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .then(pressIndicator)
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
            onTextLayout = {
                layoutResult.value = it
            }
        )
    }
}

/**
 * https://regexr.com/37i6s
 */
private val UrlRegexp = """https?://(www\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\.[a-z]{2,4}\b([-a-zA-Z0-9@:%_+.~#?&/=]*)""".toRegex()

@OptIn(ExperimentalTextApi::class)
@Composable
private fun linkifyText(text: String): AnnotatedString {
    val style = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline
    )
    return remember(text, style) {
        buildAnnotatedString {
            var currentIdx = 0
            for (match in UrlRegexp.findAll(text)) {
                if (currentIdx < match.range.first) {
                    append(text.substring(currentIdx, match.range.first))
                }
                val url = text.substring(match.range)
                withAnnotation(UrlAnnotation(url)) {
                    withStyle(style) {
                        append(url)
                    }
                }
                currentIdx = match.range.last + 1
            }
            append(text.substring(currentIdx))
        }
    }
}
