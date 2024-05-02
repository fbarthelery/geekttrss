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
package com.geekorum.ttrss.manage_feeds.add_feed

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geekorum.geekdroid.app.ModalBottomSheetActivity
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.WithNightModePreferencesTheme
import com.geekorum.ttrss.applicationComponent
import com.geekorum.ttrss.debugtools.withStrictMode
import com.geekorum.ttrss.manage_feeds.ActivityComponent
import com.geekorum.ttrss.manage_feeds.DaggerManageFeedComponent
import com.geekorum.ttrss.manage_feeds.R
import com.geekorum.ttrss.ui.AppTheme3
import kotlinx.coroutines.delay
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Chrome share offline page as multipart/related content send in EXTRA_STREAM
 * Mime multipart can be parsed with mime4j
 * 'org.apache.james:apache-mime4j-core:0.8.1'
 * 'org.apache.james:apache-mime4j-dome:0.8.1'
 * who doesn't have much dependencies and are used by k9mail
 */
class AddFeedActivity : ModalBottomSheetActivity()
{
    private lateinit var activityComponent: ActivityComponent

    private val viewModel: AddFeedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        enableEdgeToEdge()
        withStrictMode(StrictMode.allowThreadDiskReads()) {
            super.onCreate(savedInstanceState)
        }

        val urlString = intent.data?.toString() ?: intent.extras?.getString(Intent.EXTRA_TEXT)
        val url = urlString?.toHttpUrlOrNull()
        viewModel.init(url)

        viewModel.complete.observe(this, EventObserver {
            dismiss()
        })

        setSheetContent {
            WithNightModePreferencesTheme {
                AddFeedContent(viewModel)
            }
        }
    }

    fun inject() {
        val manageFeedComponent = DaggerManageFeedComponent.builder()
            .manageFeedsDependencies(applicationComponent)
            .build()
        activityComponent = manageFeedComponent
            .createActivityComponent()
            .newComponent(this)
        activityComponent.inject(this)
    }

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() {
            return activityComponent.hiltViewModelFactoryFactory.fromActivity(this,
                super.defaultViewModelProviderFactory)
        }

}


@Composable
private fun AddFeedContent(vm: AddFeedViewModel) {
    AppTheme3 {
        val feeds by vm.availableFeeds.collectAsStateWithLifecycle()
        val accounts by vm.accounts.observeAsState(emptyArray())
        AddFeedContent(
            isLoading = feeds == null,
            isSubscribeEnabled = vm.canSubscribe,
            feeds = feeds ?: emptyList(),
            selectedFeed = vm.selectedFeed,
            accounts = accounts,
            selectedAccount = vm.selectedAccount,
            onFeedSelectionChange = vm::setSelectedFeed,
            onAccountSelectionChange = vm::setSelectedAccount,
            onCancelClick = vm::cancel,
            onSubscribeClick = vm::subscribeToFeed
        )
    }
}

@Composable
private fun AddFeedContent(
    isLoading: Boolean,
    isSubscribeEnabled: Boolean,
    feeds: Collection<FeedsFinder.FeedResult>,
    selectedFeed: FeedsFinder.FeedResult?,
    accounts: Array<Account>,
    selectedAccount: Account?,
    onFeedSelectionChange: (FeedsFinder.FeedResult) -> Unit,
    onAccountSelectionChange: (Account) -> Unit,
    onCancelClick: () -> Unit,
    onSubscribeClick: () -> Unit
) {
    Surface {
        Column {
            TitleBar()
            Spacer(Modifier.height(16.dp))

            Column(Modifier.animateContentSize()) {
                if (isLoading) {
                    LoadingFeedProgress()
                } else {
                    if (feeds.isNotEmpty()) {
                        FeedSelector(
                            feeds,
                            selectedFeed,
                            onSelectionChange = onFeedSelectionChange,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(stringResource(R.string.activity_add_feed_no_feeds_found),
                            modifier = Modifier.padding(16.dp))

                        LaunchedEffect(Unit){
                            delay(3000)
                            onCancelClick()
                        }
                    }
                    if (accounts.size > 1) {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(top = 16.dp)) {
                            AccountSelector(accounts = accounts,
                                selectedAccount = selectedAccount,
                                onSelectionChange = onAccountSelectionChange,
                                modifier = Modifier.padding(vertical = 16.dp))
                        }
                    }
                }
            }

            ButtonsBar(isSubscribeEnabled = isSubscribeEnabled,
                onCancelClick = onCancelClick,
                onSubscribeClick = onSubscribeClick)

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedSelector(
    feeds: Collection<FeedsFinder.FeedResult>,
    selectedFeed: FeedsFinder.FeedResult?,
    onSelectionChange: (FeedsFinder.FeedResult) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = modifier.padding(horizontal = 8.dp),
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        val isSingleFeed = feeds.size == 1
        val menuModifier = if (isSingleFeed) Modifier else Modifier.menuAnchor()
        val focusedBorderColor = if (isSingleFeed) MaterialTheme.colorScheme.surface
                                        else MaterialTheme.colorScheme.primary

        val unfocusedBorderColor = if (isSingleFeed) MaterialTheme.colorScheme.surface
                                            else MaterialTheme.colorScheme.outline
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .then(menuModifier),
            readOnly = true,
            value = selectedFeed?.title ?: "",
            onValueChange = {},
            trailingIcon = {
                if (!isSingleFeed) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = focusedBorderColor,
                unfocusedBorderColor = unfocusedBorderColor),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            feeds.forEach { feed ->
                DropdownMenuItem(
                    text = { Text(feed.title) },
                    onClick = {
                        onSelectionChange(feed)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSelector(
    accounts: Array<Account>,
    selectedAccount: Account?,
    onSelectionChange: (Account) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            modifier = Modifier.padding(horizontal = 8.dp),
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                label = { Text(stringResource(R.string.activity_add_feed_account_subtitle))},
                value = selectedAccount?.name ?: "",
                onValueChange = {},
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                accounts.forEach { account ->
                    DropdownMenuItem(
                        text = { Text(account.name) },
                        onClick = {
                            onSelectionChange(account)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

@Composable
private fun ButtonsBar(
    isSubscribeEnabled: Boolean,
    onCancelClick: () -> Unit,
    onSubscribeClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onCancelClick ) {
            Text(stringResource(android.R.string.cancel))
        }
        Spacer(Modifier.width(16.dp))
        TextButton(onClick =onSubscribeClick, enabled = isSubscribeEnabled) {
            Text(stringResource(R.string.activity_add_feed_btn_subscribe))
        }
    }
}


@Composable
private fun LoadingFeedProgress() {
    ListItem(
        headlineContent = {
            Text(stringResource(R.string.activity_add_feed_looking_for_feed))
        },
        leadingContent = {
            CircularProgressIndicator()
        }
    )
}

@Composable
private fun TitleBar() {
    Surface(color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
    ) {
        Text(stringResource(R.string.activity_add_feed_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight()
                .wrapContentHeight()
        )
    }
}


@Preview
@Composable
private fun PreviewAddFeedContent() {
    AppTheme3 {
        val feeds = listOf(
            FeedsFinder.FeedResult(
                source = FeedsFinder.Source.HTML,
                type = "whateve",
                href = "",
                title = "The github blog feed"
            ),
            FeedsFinder.FeedResult(
                source = FeedsFinder.Source.HTML,
                type = "whateve",
                href = "",
                title = "The github blog feed comment"
            )
        )
        val accounts = listOf(Account("first", "wtv"), Account("second", "wet") ).toTypedArray()
        var selectedAccount by remember { mutableStateOf(accounts.first()) }
        var selectedFeed by remember { mutableStateOf(feeds.firstOrNull()) }

        AddFeedContent(isLoading = false,
            isSubscribeEnabled = false,
            feeds = feeds,
            selectedFeed = selectedFeed,
            accounts = accounts,
            selectedAccount = selectedAccount,
            onFeedSelectionChange = { selectedFeed = it},
            onAccountSelectionChange = { selectedAccount = it},
            onCancelClick = {},
            onSubscribeClick = {}
        )
    }
}


/**
 * Only used as a destination in the feature modules.
 * The AddFeedInstallerActivity takes care of the logic.
 */
class CompleteInstallFragment : Fragment()
