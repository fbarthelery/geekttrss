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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme

@Composable
fun ArticlesListAppBar(
    title: @Composable () -> Unit,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
    appBarState: ArticlesListAppbarState = rememberArticlesListAppBarState(),
    displaySortMenuButton: Boolean = true,
    displaySearchButton: Boolean = true,
    navigationIcon: @Composable (() -> Unit)? = null,
) {
    TopAppBar(
        modifier = modifier,
    ) {

        Row(
            Modifier
                .fillMaxHeight()
                .width(68.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (displaySearchButton && appBarState.searchOpen) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.high,
                    content = {
                        IconButton(onClick = {
                            appBarState.close()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "back")
                        }
                    }
                )
            } else {
                if (navigationIcon == null) {
                    Spacer(Modifier.width(12.dp))
                } else {
                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.high,
                        content = navigationIcon
                    )
                }
            }
        }

        Row(
            Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.h6) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.high,
                    content = {
                        if (displaySearchButton && appBarState.searchOpen) {
                            SearchTextField(
                                value = appBarState.searchText ,
                                onValueChange = { appBarState.searchText = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            title()
                        }
                    }
                )
            }
        }

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Row(
                Modifier.fillMaxHeight(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = {
                    val isSearchVisibleAndOpen = displaySearchButton && appBarState.searchOpen
                    if (!isSearchVisibleAndOpen) {
                        if (displaySearchButton) {
                            IconButton(onClick = {
                                appBarState.open()
                            }) {
                                Icon(Icons.Default.Search, contentDescription = "search")
                            }
                        }

                        if (displaySortMenuButton) {
                            SortMenuButton(sortOrder, onSortOrderChange)
                        }
                    }
                }
            )
        }
    }
}


@Stable
class ArticlesListAppbarState(
    private val onSearchTextChange: (String) -> Unit,
    private val onSearchOpenChange: (Boolean) -> Unit,
) {
    var searchOpen by  mutableStateOf(false)
        private set
    private var _searchText by mutableStateOf("")

    var searchText: String
        get() = _searchText
        set(value) {
            _searchText = value
            if (searchOpen) {
                onSearchTextChange(value)
            }
        }


    fun open() {
        searchOpen = true
        onSearchOpenChange(searchOpen)
    }

    fun close() {
        searchOpen = false
        onSearchOpenChange(searchOpen)
        _searchText = ""
    }
}

@Composable
fun rememberArticlesListAppBarState(
    onSearchTextChange: (String) -> Unit = {},
    onSearchOpenChange: (Boolean) -> Unit = {}
): ArticlesListAppbarState {
    return remember {
        ArticlesListAppbarState(
            onSearchTextChange = onSearchTextChange,
            onSearchOpenChange = onSearchOpenChange)
    }
}

@Composable
private fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    MaterialTheme(colors = MaterialTheme.colors.copy(
        surface = MaterialTheme.colors.primarySurface,
        onSurface = MaterialTheme.colors.contentColorFor(MaterialTheme.colors.primarySurface),
        ),
        typography = MaterialTheme.typography.copy(
            subtitle1 = MaterialTheme.typography.h6
        )
    ) {
        ProvideTextStyle(value = MaterialTheme.typography.h6) {
            val focusRequester = remember {
                FocusRequester()
            }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            TextField(
                modifier = modifier.focusRequester(focusRequester),
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(stringResource(R.string.placeholder_textfield_search))
                },
                trailingIcon = {
                    if (value.isNotEmpty()) {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.content_desc_btn_clear))
                        }
                    }
                },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    onValueChange(value)
                }),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    cursorColor = MaterialTheme.colors.secondary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent

                )
            )
        }
    }
}

@Composable
fun SortMenuButton(
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(
            modifier = modifier,
            onClick = {
                expanded = true
            }) {
            Icon(Icons.Default.Sort, contentDescription = stringResource(R.string.content_desc_btn_sort_menu))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }) {
            SortMenuRadioGroup(closeMenu = { expanded = false},
                sortOrder = sortOrder,
                onSortOrderChange = onSortOrderChange
            )
        }
    }
}

enum class SortOrder {
    MOST_RECENT_FIRST,
    OLDEST_FIRST,
}

@Composable
fun SortOrder.getLabel(): String {
    val id = when (this) {
        SortOrder.MOST_RECENT_FIRST -> R.string.sort_order_most_recent_first
        SortOrder.OLDEST_FIRST -> R.string.sort_order_oldest_first
    }
    return stringResource(id)
}

@Composable
private fun SortMenuRadioGroup(
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    closeMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.selectableGroup()) {
        SortOrder.values().forEach { current ->
            DropdownMenuItem(modifier = Modifier.selectable(
                selected = current == sortOrder,
                role = Role.RadioButton,
                onClick = {
                    onSortOrderChange(current)
                    closeMenu()
                }),
                onClick = {
                    onSortOrderChange(current)
                    closeMenu()
                }
            ) {
                Text(
                    current.getLabel(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp))
                RadioButton(selected = current == sortOrder, onClick = null)
            }
        }
    }
}

@Preview
@Composable
fun PreviewArticlesListAppBar() {
    AppTheme {
        var sortOrder by remember {
            mutableStateOf(SortOrder.MOST_RECENT_FIRST)
        }
        Scaffold(
            topBar = {
                ArticlesListAppBar(
                    title = {
                        Text("Title ")
                    },
                    navigationIcon = {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                    },
                    sortOrder = sortOrder,
                    onSortOrderChange = { sortOrder = it }
                )
            }
        ) {
            Text("content", Modifier.padding(it))
        }
    }
}