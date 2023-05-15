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

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.ttrss.R
import com.geekorum.ttrss.app_reviews.AppReviewViewModel
import com.geekorum.ttrss.article_details.ArticleDetailActivity
import com.geekorum.ttrss.in_app_update.InAppUpdateViewModel
import com.geekorum.ttrss.on_demand_modules.InstallModuleViewModel
import com.geekorum.ttrss.session.SessionActivity
import com.geekorum.ttrss.ui.AppTheme
import com.geekorum.ttrss.ui.AppTheme3
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * An activity representing a list of Articles. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ArticleDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
@AndroidEntryPoint
class ArticleListActivity : SessionActivity() {

    private val activityViewModel: ActivityViewModel by viewModels()
    private val accountViewModel: TtrssAccountViewModel by viewModels()
    private val inAppUpdateViewModel: InAppUpdateViewModel by viewModels()
    private val feedsViewModel: FeedsViewModel by viewModels()
    private val tagsViewModel: TagsViewModel by viewModels()
    private val appReviewViewModel: AppReviewViewModel by viewModels()
    private val installModuleViewModel: InstallModuleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountViewModel.selectedAccount.observe(this) { account ->
            if (account != null) {
                activityViewModel.setAccount(account)
            } else {
                accountViewModel.startSelectAccountActivity(this)
            }
        }

        accountViewModel.noAccountSelectedEvent.observe(this, EventObserver {
            finish()
        })

        //TODO use a preference ?
        feedsViewModel.setOnlyUnread(true)

        setupEdgeToEdge()
        setContent()
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun setContent() {
        setContent {
            val navController = rememberNavController()
            val appBarPresenter = remember {
                createAppBarPresenter(navController)
            }
            val inAppUpdatePresenter = remember {
                createInAppUpdatePresenter()
            }
            val feedNavigationPresenter = remember {
                createFeedsNavigationMenuPresenter(navController)
            }
            val owner = LocalLifecycleOwner.current
            LaunchedEffect(activityViewModel, navController, owner) {
                activityViewModel.articleSelectedEvent.observe(owner, EventObserver { (_, article) ->
                    navController.navigateToArticle(article.id)
                })
            }

            val navBackStackEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(null)
            if (navBackStackEntry?.destination?.route == NavRoutes.Magazine) {
                LaunchedEffect(Unit) {
                    appReviewViewModel.launchReview(this@ArticleListActivity)
                }
            }

            AppTheme {
            AppTheme3 {
                val coroutineScope = rememberCoroutineScope()
                val scaffoldState = rememberScaffoldState()

                val undoUnreadSnackbarMessage by activityViewModel.undoReadSnackBarMessage.collectAsStateWithLifecycle()
                val nbArticles = undoUnreadSnackbarMessage?.nbArticles ?: 0
                val undoUnreadSnackbarComposable: (@Composable () -> Unit)? = if (nbArticles > 0) {
                    @Composable {
                        UndoUnreadSnackBar(undoUnreadSnackbarMessage = undoUnreadSnackbarMessage!!)
                    }
                } else null

                val windowSizeClass = calculateWindowSizeClass(this)
                val hasFabInFixedDrawer = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
                val fabPresenter = remember {
                    FabPresenter(navController)
                }

                val drawerLayoutPresenter = rememberDrawerLayoutPresenter(
                    navController,
                )

                ArticlesListScaffold(
                    scaffoldState = scaffoldState,
                    windowSizeClass = windowSizeClass,
                    topBar = {
                        appBarPresenter.ToolbarContent(
                            onNavigationMenuClick = {
                                coroutineScope.launch {
                                    scaffoldState.drawerState.open()
                                }
                            })
                    },
                    navigationMenu = {
                        feedNavigationPresenter.Content(
                            hasFab = hasFabInFixedDrawer,
                            onNavigation = {
                                coroutineScope.launch {
                                    scaffoldState.drawerState.close()
                                }
                            })
                    },
                    floatingActionButton = {
                        if (!hasFabInFixedDrawer) {
                            val isScrollingUp by activityViewModel.isScrollingUp.collectAsStateWithLifecycle()
                            fabPresenter.Content(
                                isScrollingUpOrRest = isScrollingUp,
                                onClick = { activityViewModel.refresh() },
                                modifier = Modifier.navigationBarsPadding()
                            )
                        }
                    },
                    bannerContent = {
                        Box(Modifier.padding(it)){
                            inAppUpdatePresenter.Content()
                        }
                    },
                    undoUnreadSnackBar = undoUnreadSnackbarComposable,
                    drawerGesturesEnabled = drawerLayoutPresenter.drawerGesturesEnabled
                ) {
                    ArticlesListNavHost(activityViewModel, navController)
                }
            }
            }
        }
    }

    private fun setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    private fun createAppBarPresenter(navController: NavController): AppBarPresenter {
        return AppBarPresenter(
            activity = this,
            activityViewModel = activityViewModel,
            tagsViewModel = tagsViewModel,
            navController = navController
        )
    }

    private fun createInAppUpdatePresenter(): InAppUpdatePresenter {
        return InAppUpdatePresenter(inAppUpdateViewModel)
    }

    private fun createFeedsNavigationMenuPresenter(navController: NavController): FeedsNavigationMenuPresenter {
        return FeedsNavigationMenuPresenter(
            navController,
            feedsViewModel, accountViewModel, activityViewModel, installModuleViewModel, this)
    }

}

@Composable
private fun UndoUnreadSnackBar(undoUnreadSnackbarMessage: UndoReadSnackbarMessage) {
    Snackbar(modifier = Modifier.padding(12.dp),
        action = @Composable {
            TextButton(
                colors = ButtonDefaults.textButtonColors(contentColor = SnackbarDefaults.primaryActionColor),
                onClick = { undoUnreadSnackbarMessage.onAction() },
                content = { Text(stringResource(R.string.undo_set_articles_read_btn)) }
            )
        }) {
        val nbArticles = undoUnreadSnackbarMessage.nbArticles
        Text(
            pluralStringResource(
                R.plurals.undo_set_articles_read_text,
                nbArticles,
                nbArticles
            )
        )
    }
    LaunchedEffect(undoUnreadSnackbarMessage.nbArticles) {
        delay(2_750)
        undoUnreadSnackbarMessage.onDismiss()
    }
}
