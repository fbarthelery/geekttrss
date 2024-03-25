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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    return object :PaddingValues {
        override fun calculateBottomPadding(): Dp {
            return this@plus.calculateBottomPadding() + other.calculateBottomPadding()
        }

        override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp {
            return this@plus.calculateLeftPadding(layoutDirection) + other.calculateLeftPadding(layoutDirection)
        }

        override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp {
            return this@plus.calculateRightPadding(layoutDirection) + other.calculateRightPadding(layoutDirection)
        }

        override fun calculateTopPadding(): Dp {
            return this@plus.calculateTopPadding() + other.calculateTopPadding()
        }
    }
}