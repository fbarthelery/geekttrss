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
package com.geekorum.ttrss.articles_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesListAppBar(
    title: @Composable () -> Unit,
    onSearchClick: () -> Unit,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
    displaySortMenuButton: Boolean = true,
    displaySearchButton: Boolean = true,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    navigationIcon: @Composable (() -> Unit)? = null,
) {
    TopAppBar(
        modifier = modifier,
        colors = colors,
        navigationIcon = {
                navigationIcon?.invoke()
        },
        title = title,
        actions = {
            if (displaySearchButton) {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, contentDescription = "search")
                }
            }
            if (displaySortMenuButton) {
                SortMenuButton(sortOrder, onSortOrderChange)
            }
        }
    )
}

@Composable
fun AppBarTitleText(title: String) {
    Text(title, overflow = TextOverflow.Ellipsis, maxLines = 1)
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


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ArticlesSearchBar(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onUpClick: () -> Unit,
    modifier: Modifier = Modifier,
    suggestions: List<String> = emptyList(),
) {
    SearchBar(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        inputField = {
            SearchBarDefaults.InputField(
                modifier = Modifier.fillMaxWidth(),
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {
                    onSearch(it)
                    onExpandedChange(false)
                },
                expanded = expanded,
                onExpandedChange = {
                    onExpandedChange(it)
                },
                placeholder = {
                    Text(
                        stringResource(R.string.placeholder_textfield_search),
                    )
                },
                leadingIcon = {
                    IconButton(onClick = onUpClick) {
                        Icon(
                            imageVector = AppTheme3.IconsAutoMirrored.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(R.string.content_desc_btn_clear)
                            )
                        }
                    }
                },
            )
        },
        shadowElevation = 3.dp,
        modifier = modifier,
    ) {
        LazyColumn {
            items(suggestions, key = { it }) { suggestion ->
                ListItem(
                    headlineContent = {
                        Text(suggestion)
                    },
                    leadingContent = {
                        Icon(AppTheme3.Icons.History, null)
                    },
                    modifier = Modifier.clickable {
                        onQueryChange(suggestion)
                        onSearch(suggestion)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PreviewArticlesListAppBar() {
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
                    onSortOrderChange = { sortOrder = it },
                    onSearchClick = {}
                )
            }
        ) {
            Text("content", Modifier.padding(it))
        }
    }
}

@Preview
@Composable
private fun PreviewArticlesSearch() {
    AppTheme3 {
        var active by remember {
            mutableStateOf(false)
        }
        var query by remember {
            mutableStateOf("")
        }
        var content by remember {
            mutableStateOf("content")
        }
        Scaffold(
            topBar = {
                ArticlesSearchBar(
                    expanded = active,
                    onExpandedChange = {
                        active = it
                    },
                    query = query,
                    onQueryChange = { query = it },
                    onUpClick = {
                        active = false
                        query = ""
                    },
                    onSearch = {
                               content = "Search results for $it"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth()
                )
            }
        ) {
            Text(content, Modifier.padding(it))
        }
    }
}
