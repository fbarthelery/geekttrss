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
package com.geekorum.favikonsnoop.snoopers

import com.geekorum.favikonsnoop.AdaptiveDimension
import com.geekorum.favikonsnoop.FixedDimension
import com.google.common.truth.Truth.assertThat
import kotlin.test.Test


class HtmlHelpersTest {

    @Test
    fun testParsingOfSizes() {
        var result = parseSizes("any")
        assertThat(result).containsExactly(AdaptiveDimension)

        result = parseSizes("14x14")
        assertThat(result).containsExactly(FixedDimension(14, 14))

        result = parseSizes("14X14")
        assertThat(result).containsExactly(FixedDimension(14, 14))

        result = parseSizes("invalid")
        assertThat(result).isEmpty()

        result = parseSizes("")
        assertThat(result).isEmpty()

        result = parseSizes("14x14 12X12 any")
        assertThat(result).containsExactly(
            FixedDimension(14, 14),
            FixedDimension(12, 12),
            AdaptiveDimension
        )

        result = parseSizes("invalid 14x14 12X12 any -32")
        assertThat(result).containsExactly(
            FixedDimension(14, 14),
            FixedDimension(12, 12),
            AdaptiveDimension
        )

    }
}
