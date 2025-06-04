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
package com.geekorum.ttrss.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalWideNavigationRail
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailColors
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrain
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import coil.compose.rememberAsyncImagePainter
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.data.FeedWithFavIcon
import com.geekorum.ttrss.ui.AppTheme3
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModalFeedNavigationRail(
    modifier: Modifier = Modifier,
    quickFeedAccess: @Composable () -> Unit,
    feedSection: @Composable () -> Unit,
    settingsSection: @Composable () -> Unit,
    header: @Composable (() -> Unit)? = null,
    state: WideNavigationRailState = rememberWideNavigationRailState(),
    hideOnCollapse: Boolean = false,
    collapsedShape: Shape = WideNavigationRailDefaults.containerShape,
    expandedShape: Shape = MaterialTheme.shapes.large.copy(topStart = CornerSize(0), bottomStart = CornerSize(0)),
    colors: WideNavigationRailColors = WideNavigationRailDefaults.colors(),
) {
    ModalWideNavigationRail(
        modifier = modifier,
        state = state,
        hideOnCollapse = hideOnCollapse,
        collapsedShape = collapsedShape,
        expandedShape = expandedShape,
        colors = colors,
        header = header,
    ) {
        // WideNavigationRail has a top padding of 44.dp that pushes everything down
        // we need to take it into account
        // and we want an added 44.dp padding bottom for readability
        Column(Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = WNRVerticalPadding * 2)
        ) {

            val isDisplayedInModalDialog = state.currentValue == WideNavigationRailValue.Expanded
            // add nav items using the same layout algorithm then WideNavigationRail
            // quick access feeds that are visible in collapse too
            InnerWideNavigationRailLayout(
                isModal = isDisplayedInModalDialog,
                expanded = state.targetValue.isExpanded,
                modifier = Modifier, // .border(4.dp, Color.Cyan),
                colors = colors,
                content = quickFeedAccess
            )
            AnimatedVisibility(state.targetValue.isExpanded,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                // always displayed in modal dialog
                InnerWideNavigationRailLayout(
                    isModal = true,
                    expanded = state.targetValue.isExpanded,
                    modifier = Modifier,
                    colors = colors,
                    content = feedSection
                )
            }

            // settings visible in collapse too
            InnerWideNavigationRailLayout(
                isModal = isDisplayedInModalDialog,
                expanded = state.targetValue.isExpanded,
                modifier = Modifier,
                colors = colors,
                content = settingsSection
            )
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FeedNavigationRail(
    modifier: Modifier = Modifier,
    quickFeedAccess: @Composable () -> Unit,
    feedSection: @Composable () -> Unit,
    settingsSection: @Composable () -> Unit,
    header: @Composable (() -> Unit)? = null,
    state: WideNavigationRailState = rememberWideNavigationRailState(),
    shape: Shape = WideNavigationRailDefaults.containerShape,
    colors: WideNavigationRailColors = WideNavigationRailDefaults.colors(),
) {
    WideNavigationRail(
        modifier = modifier,
        state = state,
        colors = colors,
        shape = shape,
        header = header,
    ) {
        // WideNavigationRail has a top padding of 44.dp that pushes everything down
        // we need to take it into account
        // and we want an added 44.dp padding bottom for readability
        Column(Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = WNRVerticalPadding * 2)
        ) {
            InnerWideNavigationRailLayout(
                isModal = false,
                expanded = state.targetValue.isExpanded,
                modifier = Modifier, // .border(4.dp, Color.Cyan),
                colors = colors,
                content = quickFeedAccess
            )

            AnimatedVisibility(state.targetValue.isExpanded,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                // always displayed in modal dialog
                InnerWideNavigationRailLayout(
                    isModal = false,
                    expanded = state.targetValue.isExpanded,
                    modifier = Modifier,
                    colors = colors,
                    content = feedSection
                )
            }

            // settings visible in collapse too
            InnerWideNavigationRailLayout(
                isModal = false,
                expanded = state.targetValue.isExpanded,
                modifier = Modifier,
                colors = colors,
                content = settingsSection
            )
        }
    }
}




@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun NavRailMenuButton(
    isMenuOpen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        val icon = if (isMenuOpen)
            Icons.AutoMirrored.Filled.MenuOpen
        else Icons.Filled.Menu
        Icon(icon, null)
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.labelLarge,
        modifier = modifier.padding(start = 36.dp, top = 12.dp, bottom = 8.dp))
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsWideNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    railExpanded: Boolean = false,
) {
    WideNavigationRailItem(
        modifier = modifier,
        railExpanded = railExpanded,
        icon = {
            val icon = if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
            Icon(icon,
                contentDescription = stringResource(R.string.activity_settings_title)
            )
        },
        label = { Text(stringResource(R.string.activity_settings_title)) },
        selected = selected,
        onClick = onClick,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MagazineWideNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    railExpanded: Boolean = false,
) {
    WideNavigationRailItem(
        modifier = modifier,
        railExpanded = railExpanded,
        icon = {
            val icon = if (selected) Icons.Filled.Newspaper else Icons.Outlined.Newspaper
            Icon(icon,
                contentDescription = stringResource(R.string.title_magazine)
            )
        },
        label = { Text(stringResource(R.string.title_magazine)) },
        selected = selected,
        onClick = onClick,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VirtualFeedWideNavigationRailItem(
    feed: Feed,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    railExpanded: Boolean = false,
) {
    require(Feed.isVirtualFeed(feed.id)) { "VirtualFeedNavigationRailItem should only be used by virtual feed. Use FeedNavigationRailItem" }
    WideNavigationRailItem(
        modifier = modifier,
        railExpanded = railExpanded,
        selected = selected,
        onClick = onClick,
        icon = {
            val iconVectorSelected = when {
                feed.isArchivedFeed -> Icons.Filled.Inventory2
                feed.isStarredFeed -> Icons.Filled.Star
                feed.isPublishedFeed -> Icons.Filled.CheckBox
                feed.isFreshFeed -> Icons.Filled.LocalCafe
                feed.isAllArticlesFeed -> Icons.Filled.FolderOpen
                else -> error("VirtualFeedNavigationRailItem should only be used by virtual feed. Use FeedNavigationRailItem")
            }
            val iconVectorUnselected = when {
                feed.isArchivedFeed -> Icons.Outlined.Inventory2
                feed.isStarredFeed -> Icons.Outlined.StarOutline
                feed.isPublishedFeed -> Icons.Outlined.CheckBox
                feed.isFreshFeed -> Icons.Outlined.LocalCafe
                feed.isAllArticlesFeed -> Icons.Outlined.FolderOpen
                else -> error("VirtualFeedNavigationRailItem should only be used by virtual feed. Use FeedNavigationRailItem")
            }

            BadgedBox(
                badge = {
                    if (feed.unreadCount > 0) {
                        val text = if (feed.unreadCount > 999) "^_^" else "${feed.unreadCount}"
                        Badge {
                            Text(text)
                        }
                    }
                }
            ) {
                val imageVector = if (selected) iconVectorSelected else iconVectorUnselected
                Icon(imageVector, contentDescription = null)
            }
        },
        label = {
            val label = feed.displayTitle.takeIf { it.isNotBlank() } ?: feed.title
            Text(label)
        }
    )
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FeedWideNavigationRailItem(
    feedWithFavIcon: FeedWithFavIcon,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    railExpanded: Boolean = false,
) {
    val feed = feedWithFavIcon.feed
    require(!Feed.isVirtualFeed(feed.id)) { "FeedNavigationRailItem should only be used by non virtual feed. Use VirtualFeedNavigationRailItem" }
    WideNavigationRailItem(
        modifier = modifier,
        railExpanded = railExpanded,
        selected = selected,
        onClick = onClick,
        icon = {
            val feedIconPainter = rememberAsyncImagePainter(
                model = feedWithFavIcon.favIcon?.url,
                placeholder = painterResource(R.drawable.ic_rss_feed_orange),
                fallback = painterResource(R.drawable.ic_rss_feed_orange),
                error = painterResource(R.drawable.ic_rss_feed_orange),
            )
            BadgedBox(
                badge = {
                    if (feed.unreadCount > 0) {
                        val text = if (feed.unreadCount > 999) "^_^" else "${feed.unreadCount}"
                        Badge {
                            Text(text)
                        }
                    }
                }
            ) {
                Image(
                    painter = feedIconPainter,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        label = {
            val label = feed.displayTitle.takeIf { it.isNotBlank() } ?: feed.title
            Text(label)
        }
    )
}

@Composable
private fun InnerWideNavigationRailLayout(
    modifier: Modifier,
    isModal: Boolean,
    expanded: Boolean,
    colors: WideNavigationRailColors,
    content: @Composable () -> Unit
) {
    // use correct values for inner layout
    WideNavigationRailLayout(
        modifier = modifier,
        isModal = isModal,
        expanded = expanded,
        colors = colors,
        windowInsets = WindowInsets(0),
        arrangement = Arrangement.Top,
        shape = RectangleShape,
        header = null,
        content = content
    )
}

/**
 * A fork of WideNavigationRailLayout to allow placing items in a scrollable column
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WideNavigationRailLayout(
    modifier: Modifier,
    isModal: Boolean,
    expanded: Boolean,
    colors: WideNavigationRailColors,
    shape: Shape,
    header: @Composable (() -> Unit)?,
    windowInsets: WindowInsets,
    arrangement: Arrangement.Vertical,
    content: @Composable () -> Unit
) {
    var currentWidth by remember { mutableIntStateOf(0) }
    var actualMaxExpandedWidth by remember { mutableIntStateOf(0) }
    val minimumA11ySize =
        if (LocalMinimumInteractiveComponentSize.current == Dp.Unspecified) {
            0.dp
        } else {
            LocalMinimumInteractiveComponentSize.current
        }

    // TODO: Load the motionScheme tokens from the component tokens file.
    val animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Dp>()
    val modalAnimationSpec = MaterialTheme.motionScheme.fastSpatialSpec<Dp>()
    val minWidth by
    animateDpAsState(
        targetValue = if (!expanded) CollapsedRailWidth else ExpandedRailMinWidth,
        animationSpec = if (!isModal) animationSpec else modalAnimationSpec
    )
    val widthFullRange by
    animateDpAsState(
        targetValue = if (!expanded) CollapsedRailWidth else ExpandedRailMaxWidth,
        animationSpec = if (!isModal) animationSpec else modalAnimationSpec
    )
    val itemVerticalSpacedBy by
    animateDpAsState(
        targetValue = if (!expanded) 4.0.dp else 0.dp,
        animationSpec = animationSpec
    )
    val itemMinHeight by
    animateDpAsState(
        targetValue = if (!expanded) TopIconItemMinHeight else minimumA11ySize,
        animationSpec = animationSpec
    )

    // ignore colors, shape just use a layout
/*    Surface(
        color = if (!isModal) colors.containerColor else colors.modalContainerColor,
        contentColor = colors.contentColor,
        shape = shape,
        modifier = modifier,
    ) {*/
    Box(modifier = modifier) {
        Layout(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .windowInsetsPadding(windowInsets)
                    .widthIn(max = ExpandedRailMaxWidth)
                    // as this is use inside the real rail measurment we don't need vertical padding
//                    .padding(top = WNRVerticalPadding)
                    .selectableGroup()
                    .semantics { isTraversalGroup = true },
            content = {
                if (header != null) {
                    Box(Modifier.layoutId(HeaderLayoutIdTag)) { header() }
                }
                content()
            },
            measurePolicy =
                object : MeasurePolicy {
                    override fun MeasureScope.measure(
                        measurables: List<Measurable>,
                        constraints: Constraints
                    ): MeasureResult {
                        // height is infinity in vertical scroll
                        var height = constraints.maxHeight

                        var itemsCount = measurables.size
                        var actualExpandedMinWidth = constraints.minWidth
                        val actualMinWidth =
                            if (constraints.minWidth == 0) {
                                actualExpandedMinWidth =
                                    ExpandedRailMinWidth.roundToPx()
                                        .coerceAtMost(constraints.maxWidth)
                                minWidth.roundToPx().coerceAtMost(constraints.maxWidth)
                            } else {
                                constraints.minWidth
                            }
                        // If there are no items, rail will be empty.
                        if (itemsCount < 1) {
                            return layout(actualMinWidth, constraints.minHeight) {}
                        }
                        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
                        var itemsMeasurables = measurables

                        var constraintsOffset = 0
                        var headerPlaceable: Placeable? = null
                        if (header != null) {
                            headerPlaceable =
                                measurables
                                    .fastFirst { it.layoutId == HeaderLayoutIdTag }
                                    .measure(looseConstraints)
                            // Header is always first element in measurables list.
                            if (itemsCount > 1)
                                itemsMeasurables = measurables.subList(1, itemsCount)
                            // Real item count doesn't include the header.
                            itemsCount--
                            constraintsOffset = headerPlaceable.height
                        }

                        val itemsPlaceables =
                            if (itemsCount > 0) mutableListOf<Placeable>() else null
                        val itemMaxWidthConstraint =
                            if (expanded) looseConstraints.maxWidth else actualMinWidth
                        var expandedItemMaxWidth = 0
                        if (itemsPlaceables != null) {
                            itemsMeasurables.fastMap {
                                val measuredItem =
                                    it.measure(
                                        looseConstraints
                                            .offset(vertical = -constraintsOffset)
                                            .constrain(
                                                Constraints.fitPrioritizingWidth(
                                                    minWidth = minimumA11ySize.roundToPx(),
                                                    minHeight = itemMinHeight.roundToPx(),
                                                    maxWidth = itemMaxWidthConstraint,
                                                    maxHeight = looseConstraints.maxHeight,
                                                )
                                            )
                                    )
                                val maxItemWidth = measuredItem.measuredWidth
                                if (expanded && expandedItemMaxWidth < maxItemWidth) {
                                    expandedItemMaxWidth =
                                        maxItemWidth + ItemHorizontalPadding.roundToPx()
                                }
                                constraintsOffset = measuredItem.height
                                itemsPlaceables.add(measuredItem)
                            }
                        }

                        var width = actualMinWidth
                        // Limit collapsed rail to fixed width, but expanded rail can be as wide as
                        // constraints.maxWidth
                        if (expanded) {
                            val widestElementWidth =
                                maxOf(expandedItemMaxWidth, headerPlaceable?.width ?: 0)

                            if (
                                widestElementWidth > actualMinWidth &&
                                widestElementWidth > actualExpandedMinWidth
                            ) {
                                val widthConstrain =
                                    maxOf(widestElementWidth, actualExpandedMinWidth)
                                        .coerceAtMost(constraints.maxWidth)
                                // Use widthFullRange so there's no jump in animation for when the
                                // expanded width has to be wider than actualExpandedMinWidth.
                                width = widthFullRange.roundToPx().coerceAtMost(widthConstrain)
                                actualMaxExpandedWidth = width
                            }
                        } else {
                            if (actualMaxExpandedWidth > 0) {
                                // Use widthFullRange so there's no jump in animation for the case
                                // when the expanded width was wider than actualExpandedMinWidth.
                                width =
                                    widthFullRange
                                        .roundToPx()
                                        .coerceIn(
                                            minimumValue = actualMinWidth,
                                            maximumValue =
                                                currentWidth.coerceAtLeast(actualMinWidth)
                                        )
                            }
                        }
                        currentWidth = width

                        // assume we don't have an header nor any padding as this used in content
                        if (height == Constraints.Infinity) {
                            height = run {
                                return@run if (itemsPlaceables != null) {
                                    val sizes = IntArray(itemsPlaceables.size)
                                    itemsPlaceables.fastForEachIndexed { index, item ->
                                        sizes[index] = item.height
                                        if (index < itemsPlaceables.size - 1) {
                                            sizes[index] += itemVerticalSpacedBy.roundToPx()
                                        }
                                    }
                                    sizes.sum() // + WNRVerticalPadding.roundToPx()
                                } else 0
                            }
                        }

                        return layout(width, height) {
                            // as this is use inside the real rail measurment we don't need vertical padding
                            val railHeight = height  - WNRVerticalPadding.roundToPx()
                            var headerOffset = 0
                            if (headerPlaceable != null && headerPlaceable.height > 0) {
                                headerPlaceable.placeRelative(0, 0)
                                headerOffset +=
                                    headerPlaceable.height + WNRHeaderPadding.roundToPx()
                            }

                            if (itemsPlaceables != null) {
                                val layoutSize =
                                    if (arrangement == Arrangement.Center) {
                                        // For centered arrangement the items will be centered in
                                        // the container, not in the remaining space below the
                                        // header.
                                        railHeight
                                    } else {
                                        railHeight - headerOffset
                                    }
                                val sizes = IntArray(itemsPlaceables.size)
                                itemsPlaceables.fastForEachIndexed { index, item ->
                                    sizes[index] = item.height
                                    if (index < itemsPlaceables.size - 1) {
                                        sizes[index] += itemVerticalSpacedBy.roundToPx()
                                    }
                                }
                                val y = IntArray(itemsPlaceables.size)
                                with(arrangement) { arrange(layoutSize, sizes, y) }

                                val offset =
                                    if (arrangement == Arrangement.Center) 0 else headerOffset
                                itemsPlaceables.fastForEachIndexed { index, item ->
                                    item.placeRelative(0, y[index] + offset)
                                }
                            }
                        }
                    }
                }
        )
    }
}


private const val HeaderLayoutIdTag: String = "header"
private val ItemHorizontalPadding = 20.dp

private val WNRVerticalPadding = 44.0.dp
// Padding at the bottom of the rail's header. This padding will only be added when the header is
// not null and the rail arrangement is Top.
private val WNRHeaderPadding: Dp = 40.dp
private val CollapsedRailWidth = 96.0.dp
private val ExpandedRailMinWidth = 220.0.dp
private val ExpandedRailMaxWidth = 360.0.dp
private val TopIconItemMinHeight = 64.0.dp


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal val WideNavigationRailValue.isExpanded
    get() = this == WideNavigationRailValue.Expanded


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PreviewMobileModalFeedNavigationRail() {
    AppTheme3 {
        val state = rememberWideNavigationRailState()
        val coroutineScope = rememberCoroutineScope()
        var selectedItem by remember { mutableIntStateOf(-1) }
        Row {
            ModalFeedNavigationRail(
                state = state,
                hideOnCollapse = true,
                quickFeedAccess = {
                    QuickFeedAccessForPreview(state.targetValue.isExpanded, selectedItem, onItemClick = {
                        selectedItem = it
                    })
                },
                feedSection = {
                    FeedsSectionForPreview(
                        state.targetValue.isExpanded,
                        selectedItem - 4,
                        onItemClick = {
                            selectedItem = it + 4
                        }
                    )
                },
                settingsSection = {
                    SettingsWideNavigationRailItem(
                        selected = false,
                        onClick = {},
                        railExpanded = state.targetValue.isExpanded
                    )
                }
            )
            Scaffold(
                topBar = {
                    TopAppBar(title = { }, navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    state.toggle()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                    })
                }
            ) {
                Box(Modifier.padding(it))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Preview(device = "spec:width=980dp,height=800dp,dpi=240")
@Composable
private fun PreviewTableModalFeedNavigationRail() {
    AppTheme3 {
        val state = rememberWideNavigationRailState()
        val coroutineScope = rememberCoroutineScope()
        var selectedItem by remember { mutableIntStateOf(-1) }
        Row {
            ModalFeedNavigationRail(
                state = state,
                header = {
                    Column {
                        NavRailMenuButton(
                            isMenuOpen = state.targetValue.isExpanded,
                            onClick = {
                                coroutineScope.launch {
                                    state.toggle()
                                }
                            },
                            modifier = Modifier.padding(start = 24.dp))

                        ExtendedFloatingActionButton(
                            text = {
                                Text("floating")
                            },
                            icon = {
                                Icon(Icons.Default.Edit, null)
                            },
                            onClick = {},
                            expanded = state.targetValue.isExpanded,
                            modifier = Modifier.padding(top = 16.dp, start =20.dp)
                        )
                    }
                },
                quickFeedAccess = {
                    QuickFeedAccessForPreview(state.targetValue.isExpanded, selectedItem, onItemClick = {
                        selectedItem = it
                    })
                },
                feedSection = {
                    FeedsSectionForPreview(
                        state.targetValue.isExpanded,
                        selectedItem - 4,
                        onItemClick = {
                            selectedItem = it + 4
                        }
                    )
                },
                settingsSection = {
                    SettingsWideNavigationRailItem(
                        selected = false,
                        onClick = {},
                        railExpanded = state.targetValue.isExpanded
                    )
                }
            )

            Scaffold(
                topBar = {
                    TopAppBar(title = {
                        Text("title")
                    })
                }
            ) {
                Box(Modifier.padding(it))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Preview(device = "spec:width=980dp,height=800dp,dpi=240")
@Composable
private fun PreviewTabletFeedNavigationRail() {
    AppTheme3 {
        val state = rememberWideNavigationRailState()
        val coroutineScope = rememberCoroutineScope()
        var selectedItem by remember { mutableIntStateOf(-1) }
        Row {
            var showHeader by remember { mutableStateOf(true) }
            val header: (@Composable () -> Unit)? = if (showHeader) null else {
                {
                    Column {
                        Text("Header")
                        NavRailMenuButton(
                            isMenuOpen = state.targetValue.isExpanded,
                            onClick = {
                                coroutineScope.launch {
                                    state.toggle()
                                }
                            },
                            modifier = Modifier.padding(start = 24.dp)
                        )

                        ExtendedFloatingActionButton(
                            text = {
                                Text("floating")
                            },
                            icon = {
                                Icon(Icons.Default.Edit, null)
                            },
                            onClick = {},
                            expanded = state.targetValue.isExpanded,
                            modifier = Modifier.padding(top = 16.dp, start = 20.dp)
                        )
                    }
                }
            }

            FeedNavigationRail(
                state = state,
                header = header,
                quickFeedAccess = {
                    QuickFeedAccessForPreview(
                        state.targetValue.isExpanded,
                        selectedItem,
                        onItemClick = {
                            selectedItem = it
                        }
                    )
                },
                feedSection = {
                    FeedsSectionForPreview(
                        state.targetValue.isExpanded,
                        selectedItem - 4,
                        onItemClick = {
                            selectedItem = it + 4
                        }
                    )
                },
                settingsSection = {
                    SettingsWideNavigationRailItem(
                        selected = false,
                        onClick = { showHeader = !showHeader },
                        railExpanded = state.targetValue.isExpanded
                    )
                },
            )

            Scaffold(
                topBar = {
                    TopAppBar(title = {
                        Text("title")
                    })
                }
            ) {
                Box(Modifier.padding(it))
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun FeedsSectionForPreview(
    railExpanded: Boolean,
    selectedItem: Int,
    onItemClick: (Int) -> Unit
) {
    val feeds = listOf(
        FeedWithFavIcon(
            feed = Feed(
                id = 2,
                title = "Frandroid",
                unreadCount = 42,
            ),
            favIcon = null
        ),
        FeedWithFavIcon(
            feed = Feed(
                id = 3,
                title = "Gentoo universe",
                unreadCount = 10,
            ),
            favIcon = null
        ),
        FeedWithFavIcon(
            feed = Feed(
                id = 4,
                title = "LinuxFr",
                unreadCount = 108,
            ),
            favIcon = null
        ),
        FeedWithFavIcon(
            feed = Feed(
                id = 5,
                title = "LinuxFrOrg",
                unreadCount = 8,
            ),
            favIcon = null
        ),
    )
    SectionHeader("Feeds")
    feeds.fastForEachIndexed { index, feed ->
        FeedWideNavigationRailItem(
            feed,
            selected = selectedItem == index,
            railExpanded = railExpanded,
            onClick = { onItemClick(index) })
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun QuickFeedAccessForPreview(
    railExpanded: Boolean,
    selectedItem: Int,
    onItemClick: (Int) -> Unit
) {
    val fakeMagazineId = 42L
    val feeds = listOf(
        Feed(id = fakeMagazineId),
        Feed(
            id = Feed.FEED_ID_ALL_ARTICLES,
            title = "All articles",
            unreadCount = 1290,
        ),
        Feed(
            id = Feed.FEED_ID_FRESH,
            title = "Fresh articles",
        ),
        Feed(
            id = Feed.FEED_ID_STARRED,
            title = "Starred articles",
        )
    )
    feeds.forEachIndexed { index, item ->
        if (index == 0) {
            MagazineWideNavigationRailItem(
                selected = selectedItem == index,
                railExpanded = railExpanded,
                onClick = { onItemClick(index) }
            )
        } else {
            VirtualFeedWideNavigationRailItem(
                item,
                selected = selectedItem == index,
                railExpanded = railExpanded,
                onClick = { onItemClick(index) }
            )
        }
    }
}