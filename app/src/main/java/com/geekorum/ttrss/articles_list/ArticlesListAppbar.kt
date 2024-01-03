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
package com.geekorum.ttrss.articles_list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesListAppBar(
    title: @Composable () -> Unit,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
    appBarState: ArticlesListAppbarState = rememberArticlesListAppBarState(),
    displaySortMenuButton: Boolean = true,
    displaySearchButton: Boolean = true,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    navigationIcon: @Composable (() -> Unit)? = null,
) {
    val isSearchVisibleAndOpen = displaySearchButton && appBarState.searchOpen
    TopAppBar(
        modifier = modifier,
        colors = colors,
        navigationIcon = {
            if (isSearchVisibleAndOpen) {
                IconButton(
                    onClick = {
                    appBarState.closeSearch()
                }) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "back")
                }
            } else {
                navigationIcon?.invoke()
            }
        },
        title = {
            if (displaySearchButton && appBarState.searchOpen) {
                SearchTextField(
                    value = appBarState.searchText ,
                    onValueChange = { appBarState.searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            } else {
                title()
            }
        },
        actions = {
            if (!isSearchVisibleAndOpen) {
                if (displaySearchButton) {
                    IconButton(onClick = {
                        appBarState.openSearch()
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

@Composable
fun AppBarTitleText(title: String) {
    Text(title, overflow = TextOverflow.Ellipsis, maxLines = 1)
}

@Stable
class ArticlesListAppbarState(
    initialSearchText: String = "",
    initialSearchIsOpen: Boolean = false,
    private val onSearchTextChange: (String) -> Unit,
    private val onSearchOpenChange: (Boolean) -> Unit,
) {
    var searchOpen by  mutableStateOf(initialSearchIsOpen)
        private set
    private var _searchText by mutableStateOf(initialSearchText)

    var searchText: String
        get() = _searchText
        set(value) {
            _searchText = value
            if (searchOpen) {
                onSearchTextChange(value)
            }
        }


    fun openSearch() {
        searchOpen = true
        onSearchOpenChange(searchOpen)
    }

    fun closeSearch() {
        searchText = ""
        searchOpen = false
        onSearchOpenChange(searchOpen)
    }

    companion object {
        fun Saver(
            onSearchTextChange: (String) -> Unit,
            onSearchOpenChange: (Boolean) -> Unit,
        ): Saver<ArticlesListAppbarState, Any> =
            mapSaver(
                save = {
                    mapOf(
                        "searchOpen" to it.searchOpen,
                        "searchText" to it.searchText
                    )
                },
                restore = {
                    val text = it["searchText"] as String
                    val isOpen = it["searchOpen"] as Boolean
                    ArticlesListAppbarState(initialSearchText = text,
                        initialSearchIsOpen = isOpen,
                        onSearchTextChange = onSearchTextChange,
                        onSearchOpenChange = onSearchOpenChange
                        )
                }
            )
    }
}

@Composable
fun rememberArticlesListAppBarState(
    onSearchTextChange: (String) -> Unit = {},
    onSearchOpenChange: (Boolean) -> Unit = {}
): ArticlesListAppbarState {
    return rememberSaveable(
        saver = ArticlesListAppbarState.Saver(
            onSearchTextChange = onSearchTextChange,
            onSearchOpenChange = onSearchOpenChange
        )
    ) {
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
            Text(stringResource(R.string.placeholder_textfield_search), style = MaterialTheme.typography.titleLarge)
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
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent

        )
    )
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
            Icon(Icons.AutoMirrored.Default.Sort, contentDescription = stringResource(R.string.content_desc_btn_sort_menu))
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
                },
                text = {
                    Text(current.getLabel())
                },
                trailingIcon = {
                    RadioButton(selected = current == sortOrder, onClick = null)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewArticlesListAppBar() {
    AppTheme3 {
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