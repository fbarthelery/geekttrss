/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2020 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.article_details

import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
import androidx.core.content.getSystemService
import androidx.core.view.updatePadding
import com.geekorum.ttrss.R
import com.geekorum.ttrss.databinding.ToolbarArticleDetailsBinding
import com.google.accompanist.insets.LocalWindowInsets
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton


@Composable
fun ArticleBottomAppBar(
    articleDetailsViewModel: ArticleDetailsViewModel,
    bottomAppBarIsVisible: Boolean,
    background: ColorStateList?,
    onFabClicked: () -> Unit,
    modifier: Modifier = Modifier,
    applyNavigationBarInsets: Boolean = true,
) {
    val bapPaddingBottom = if (applyNavigationBarInsets) {
        LocalWindowInsets.current.navigationBars.bottom
    } else 0
    val lifecycleOwner = LocalLifecycleOwner.current
    val layoutInflater: LayoutInflater = LocalContext.current.getSystemService()!!
    //TODO looks like the star don't animate to checked state on creation
    val toolbarBinding = remember {
        ToolbarArticleDetailsBinding.inflate(layoutInflater).apply {
            this.lifecycleOwner = lifecycleOwner
            viewModel = articleDetailsViewModel
        }
    }
    AndroidView(modifier = modifier,
        factory = {
            val root = CoordinatorLayout(it)
            val bottomAppBar = BottomAppBar(
                it, null, com.google.android.material.R.attr.bottomAppBarStyle
            ).apply {
                id = R.id.bottom_app_bar
                fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
            }
            bottomAppBar.addView(toolbarBinding!!.root)
            val fab = FloatingActionButton(it).apply {
                id = R.id.fab
                setImageResource(R.drawable.ic_action_web_site)
            }
            root.addView(fab, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                anchorId = bottomAppBar.id
            })
            root.addView(bottomAppBar, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.BOTTOM
            })
            return@AndroidView root
        },
        update = {
            val fab = it.findViewById<FloatingActionButton>(R.id.fab)
            fab.setOnClickListener { onFabClicked() }
            val bap = it.findViewById<BottomAppBar>(R.id.bottom_app_bar).apply {
                backgroundTint = background
                updatePadding(bottom = bapPaddingBottom)
            }
            if (bottomAppBarIsVisible) {
                bap.performShow()
            } else {
                bap.performHide()
            }
        }
    )
}

