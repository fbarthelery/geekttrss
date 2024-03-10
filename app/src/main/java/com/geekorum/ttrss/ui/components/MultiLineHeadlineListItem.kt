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

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import kotlin.math.max

/*
 * Fork of androidx.compose.material3.ListItem
 * to support headline with multiple lines and recognized the list as a 3 line items in this case
 */

@Composable
fun MultiLineHeadlineListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
) {
    val decoratedHeadlineContent: @Composable () -> Unit = {
        ProvideTextStyleFromToken(
            colors.headlineColor,
            MaterialTheme.typography.bodyLarge,
            headlineContent
        )
    }
    val decoratedSupportingContent: @Composable (() -> Unit)? = supportingContent?.let {
        {
            ProvideTextStyleFromToken(
                colors.supportingTextColor,
                MaterialTheme.typography.bodyMedium,
                it
            )
        }
    }
    val decoratedOverlineContent: @Composable (() -> Unit)? = overlineContent?.let {
        {
            ProvideTextStyleFromToken(
                colors.overlineColor,
                MaterialTheme.typography.labelSmall,
                it
            )
        }
    }
    val decoratedLeadingContent: @Composable (() -> Unit)? = leadingContent?.let {
        {
            Box(Modifier.padding(end = LeadingContentEndPadding)) {
                CompositionLocalProvider(
                    LocalContentColor provides colors.leadingIconColor,
                    content = it
                )
            }
        }
    }
    val decoratedTrailingContent: @Composable (() -> Unit)? = trailingContent?.let {
        {
            Box(Modifier.padding(start = TrailingContentStartPadding)) {
                ProvideTextStyleFromToken(
                    colors.trailingIconColor,
                    MaterialTheme.typography.labelSmall,
                    content = it
                )
            }
        }
    }

    Surface(
        modifier = Modifier
            .semantics(mergeDescendants = true) {}
            .then(modifier),
        shape = ListItemDefaults.shape,
        color = colors.containerColor,
        contentColor = colors.headlineColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    ) {
        ListItemLayout(
            headline = decoratedHeadlineContent,
            overline = decoratedOverlineContent,
            supporting = decoratedSupportingContent,
            leading = decoratedLeadingContent,
            trailing = decoratedTrailingContent,
        )
    }
}

@Composable
private fun ListItemLayout(
    leading: @Composable (() -> Unit)?,
    trailing: @Composable (() -> Unit)?,
    headline: @Composable () -> Unit,
    overline: @Composable (() -> Unit)?,
    supporting: @Composable (() -> Unit)?,
) {
    val layoutDirection = LocalLayoutDirection.current
    Layout(
        contents = listOf(
            headline,
            overline ?: {},
            supporting ?: {},
            leading ?: {},
            trailing ?: {},
        )
    ) { measurables, constraints ->
        val (headlineMeasurable, overlineMeasurable, supportingMeasurable, leadingMeasurable,
            trailingMeasurable) = measurables
        var currentTotalWidth = 0

        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
            .offset(
                horizontal = -(ListItemStartPadding + ListItemEndPadding).roundToPx(),
                vertical = -(ListItemVerticalPadding * 2).roundToPx()
            )

        val leadingPlaceable = leadingMeasurable.firstOrNull()?.measure(looseConstraints)
        currentTotalWidth += widthOrZero(leadingPlaceable)

        val trailingPlaceable = trailingMeasurable.firstOrNull()?.measure(
            looseConstraints.offset(
                horizontal = -currentTotalWidth
            )
        )
        currentTotalWidth += widthOrZero(trailingPlaceable)

        var currentTotalHeight = 0

        val headlinePlaceable = headlineMeasurable.firstOrNull()?.measure(
            looseConstraints.offset(
                horizontal = -currentTotalWidth
            )
        )
        currentTotalHeight += heightOrZero(headlinePlaceable)

        val supportingPlaceable = supportingMeasurable.firstOrNull()?.measure(
            looseConstraints.offset(
                horizontal = -currentTotalWidth,
                vertical = -currentTotalHeight
            )
        )
        currentTotalHeight += heightOrZero(supportingPlaceable)
        val isSupportingMultiline = supportingPlaceable != null &&
                (supportingPlaceable[FirstBaseline] != supportingPlaceable[LastBaseline])

        val overlinePlaceable = overlineMeasurable.firstOrNull()?.measure(
            looseConstraints.offset(
                horizontal = -currentTotalWidth,
                vertical = -currentTotalHeight
            )
        )

        // most important change is here
        val isHeadlineMultiline = headlinePlaceable != null &&
                (headlinePlaceable[FirstBaseline] != headlinePlaceable[LastBaseline])

        val listItemType = ListItemType.getListItemType(
            hasOverline = overlinePlaceable != null,
            hasSupporting = supportingPlaceable != null,
//            isSupportingMultiline = isSupportingMultiline
            isSupportingMultiline = isHeadlineMultiline
        )
        val isThreeLine = listItemType == ListItemType.ThreeLine

        val paddingValues = PaddingValues(
            start = ListItemStartPadding,
            end = ListItemEndPadding,
            top = if (isThreeLine) ListItemThreeLineVerticalPadding else ListItemVerticalPadding,
            bottom = if (isThreeLine) ListItemThreeLineVerticalPadding else ListItemVerticalPadding,
        )

        val width = calculateWidth(
            leadingPlaceable = leadingPlaceable,
            trailingPlaceable = trailingPlaceable,
            headlinePlaceable = headlinePlaceable,
            overlinePlaceable = overlinePlaceable,
            supportingPlaceable = supportingPlaceable,
            paddingValues = paddingValues,
            layoutDirection = layoutDirection,
            constraints = constraints,
        )
        val height = calculateHeight(
            leadingPlaceable = leadingPlaceable,
            trailingPlaceable = trailingPlaceable,
            headlinePlaceable = headlinePlaceable,
            overlinePlaceable = overlinePlaceable,
            supportingPlaceable = supportingPlaceable,
            listItemType = listItemType,
            paddingValues = paddingValues,
            constraints = constraints,
        )

        place(
            width = width,
            height = height,
            leadingPlaceable = leadingPlaceable,
            trailingPlaceable = trailingPlaceable,
            headlinePlaceable = headlinePlaceable,
            overlinePlaceable = overlinePlaceable,
            supportingPlaceable = supportingPlaceable,
            isThreeLine = isThreeLine,
            layoutDirection = layoutDirection,
            paddingValues = paddingValues,
        )
    }
}

private fun MeasureScope.calculateWidth(
    leadingPlaceable: Placeable?,
    trailingPlaceable: Placeable?,
    headlinePlaceable: Placeable?,
    overlinePlaceable: Placeable?,
    supportingPlaceable: Placeable?,
    layoutDirection: LayoutDirection,
    paddingValues: PaddingValues,
    constraints: Constraints,
): Int {
    if (constraints.hasBoundedWidth) {
        return constraints.maxWidth
    }
    // Fallback behavior if width constraints are infinite
    val horizontalPadding = (paddingValues.calculateLeftPadding(layoutDirection) +
            paddingValues.calculateRightPadding(layoutDirection)).roundToPx()
    val mainContentWidth = maxOf(
        widthOrZero(headlinePlaceable),
        widthOrZero(overlinePlaceable),
        widthOrZero(supportingPlaceable),
    )
    return horizontalPadding +
            widthOrZero(leadingPlaceable) +
            mainContentWidth +
            widthOrZero(trailingPlaceable)
}

private fun MeasureScope.calculateHeight(
    leadingPlaceable: Placeable?,
    trailingPlaceable: Placeable?,
    headlinePlaceable: Placeable?,
    overlinePlaceable: Placeable?,
    supportingPlaceable: Placeable?,
    listItemType: ListItemType,
    paddingValues: PaddingValues,
    constraints: Constraints,
): Int {
    val defaultMinHeight = when (listItemType) {
        // changed to use Dp directly
        ListItemType.OneLine -> 56.dp
        ListItemType.TwoLine -> 72.dp
        else /* ListItemType.ThreeLine */ -> 88.dp
    }
    val minHeight = max(constraints.minHeight, defaultMinHeight.roundToPx())

    val verticalPadding =
        paddingValues.calculateTopPadding() + paddingValues.calculateBottomPadding()

    val mainContentHeight = heightOrZero(headlinePlaceable) +
            heightOrZero(overlinePlaceable) +
            heightOrZero(supportingPlaceable)

    return max(
        minHeight,
        verticalPadding.roundToPx() + maxOf(
            heightOrZero(leadingPlaceable),
            mainContentHeight,
            heightOrZero(trailingPlaceable),
        )
    ).coerceAtMost(constraints.maxHeight)
}

private fun MeasureScope.place(
    width: Int,
    height: Int,
    leadingPlaceable: Placeable?,
    trailingPlaceable: Placeable?,
    headlinePlaceable: Placeable?,
    overlinePlaceable: Placeable?,
    supportingPlaceable: Placeable?,
    isThreeLine: Boolean,
    layoutDirection: LayoutDirection,
    paddingValues: PaddingValues,
): MeasureResult {
    return layout(width, height) {
        val startPadding = paddingValues.calculateStartPadding(layoutDirection).roundToPx()
        val endPadding = paddingValues.calculateEndPadding(layoutDirection).roundToPx()
        val topPadding = paddingValues.calculateTopPadding().roundToPx()

        leadingPlaceable?.let {
            it.placeRelative(
                x = startPadding,
                y = if (isThreeLine) topPadding else Alignment.CenterVertically.align(it.height, height)
            )
        }
        trailingPlaceable?.let {
            it.placeRelative(
                x = width - endPadding - it.width,
                y = if (isThreeLine) topPadding else Alignment.CenterVertically.align(it.height, height)
            )
        }

        val mainContentX = startPadding + widthOrZero(leadingPlaceable)
        val mainContentY = if (isThreeLine) {
            topPadding
        } else {
            val totalHeight = heightOrZero(headlinePlaceable) + heightOrZero(overlinePlaceable) +
                    heightOrZero(supportingPlaceable)
            Alignment.CenterVertically.align(totalHeight, height)
        }
        var currentY = mainContentY

        overlinePlaceable?.placeRelative(mainContentX, currentY)
        currentY += heightOrZero(overlinePlaceable)

        headlinePlaceable?.placeRelative(mainContentX, currentY)
        currentY += heightOrZero(headlinePlaceable)

        supportingPlaceable?.placeRelative(mainContentX, currentY)
    }
}

/* Changed to use textstyle directly */
@Composable
private fun ProvideTextStyleFromToken(
    color: Color,
//    textToken: TypographyKeyTokens,
    textStyle: TextStyle,
    content: @Composable () -> Unit,
) = ProvideContentColorTextStyle(
    contentColor = color,
//    textStyle = MaterialTheme.typography.fromToken(textToken),
    textStyle = textStyle,
    content = content
)

@JvmInline
private value class ListItemType private constructor(private val lines: Int) :
    Comparable<ListItemType> {

    override operator fun compareTo(other: ListItemType) = lines.compareTo(other.lines)

    companion object {
        /** One line list item */
        val OneLine = ListItemType(1)

        /** Two line list item */
        val TwoLine = ListItemType(2)

        /** Three line list item */
        val ThreeLine = ListItemType(3)

        internal fun getListItemType(
            hasOverline: Boolean,
            hasSupporting: Boolean,
            isSupportingMultiline: Boolean
        ): ListItemType {
            return when {
                (hasOverline && hasSupporting) || isSupportingMultiline -> ThreeLine
                hasOverline || hasSupporting -> TwoLine
                else -> OneLine
            }
        }
    }
}

// Container related defaults
// TODO: Make sure these values stay up to date until replaced with tokens.
@VisibleForTesting
internal val ListItemVerticalPadding = 8.dp

@VisibleForTesting
internal val ListItemThreeLineVerticalPadding = 12.dp

@VisibleForTesting
internal val ListItemStartPadding = 16.dp

@VisibleForTesting
internal val ListItemEndPadding = 16.dp

// Icon related defaults.
// TODO: Make sure these values stay up to date until replaced with tokens.
@VisibleForTesting
internal val LeadingContentEndPadding = 16.dp

// Trailing related defaults.
// TODO: Make sure these values stay up to date until replaced with tokens.
@VisibleForTesting
internal val TrailingContentStartPadding = 16.dp


@Composable
private fun ProvideContentColorTextStyle(
    contentColor: Color,
    textStyle: TextStyle,
    content: @Composable () -> Unit
) {
    val mergedStyle = LocalTextStyle.current.merge(textStyle)
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalTextStyle provides mergedStyle,
        content = content
    )
}

private fun widthOrZero(placeable: Placeable?) = placeable?.width ?: 0
private fun heightOrZero(placeable: Placeable?) = placeable?.height ?: 0
