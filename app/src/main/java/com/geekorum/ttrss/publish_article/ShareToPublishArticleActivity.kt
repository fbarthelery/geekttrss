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
package com.geekorum.ttrss.publish_article

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geekorum.geekdroid.app.ModalBottomSheetActivity
import com.geekorum.ttrss.R
import com.geekorum.ttrss.session.SessionActivityComponent
import com.geekorum.ttrss.ui.AppTheme3
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val DISPLAY_TIMEOUT_MD: Long = 1500

@AndroidEntryPoint
class ShareToPublishArticleActivity : ModalBottomSheetActivity() {

    lateinit var viewModel: SharingToPublishViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val articleId = intent.data?.lastPathSegment?.toLongOrNull()
        if (articleId == null) {
            Timber.e("Trying to publish article without articleId: ${intent.data}")
            finish()
            return
        }
        setSheetContent {
            AppTheme3 {
                SharingToPublishScreen(
                    articleId = articleId,
                    onComplete = {
                        finish()
                    })
            }
        }
    }
}

@HiltViewModel
class SharingToPublishViewModel @Inject constructor(
    componentFactory: SessionActivityComponent.Factory
) : ViewModel() {
    private val sessionActivityComponent = componentFactory.newComponent()
    private val setFieldActionFactory = sessionActivityComponent.setArticleFieldActionFactory

    var isComplete by mutableStateOf(false)
        private set

    fun publishArticle(articleId: Long) {
        setFieldActionFactory.createSetPublishedAction(viewModelScope, articleId, true)
            .execute()
        viewModelScope.launch {
            delay(DISPLAY_TIMEOUT_MD)
            isComplete = true
        }
    }
}

@Composable
fun SharingToPublishScreen(
    viewModel: SharingToPublishViewModel = hiltViewModel(),
    articleId: Long,
    onComplete: () -> Unit
) {
    LaunchedEffect(articleId) {
        viewModel.publishArticle(articleId)
    }
    LaunchedEffect(viewModel.isComplete) {
        if (viewModel.isComplete) {
            onComplete()
        }
    }
    SharingToPublishScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharingToPublishScreen() {
    Surface {
        Column {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                title = { Text(text = stringResource(R.string.activity_share_to_publish_toolbar_title)) })
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.lbl_publishing_in_progress))
                LinearProgressIndicator(
                    Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth())
            }
        }
    }
}


@Preview
@Composable
fun SharingToPublishScreenPreview() {
    AppTheme3 {
        SharingToPublishScreen()
    }
}