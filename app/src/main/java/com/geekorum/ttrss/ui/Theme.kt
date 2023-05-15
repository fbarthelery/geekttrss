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
package com.geekorum.ttrss.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geekorum.ttrss.R
import com.geekorum.ttrss.ui.AppTheme.DarkColors
import com.geekorum.ttrss.ui.AppTheme.LightColors

@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    AppTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}

@Composable
fun AppTheme(colors: Colors, content: @Composable () -> Unit) {
    MaterialTheme(
        colors = colors,
        typography = AppTheme.Typography,
        shapes = AppTheme.Shapes,
        content = content
    )
}

object AppTheme {

    val LightColors = lightColors(
        primary = Color(0xFF607D8b),
        primaryVariant = Color(0xFF34515e),
        secondary = Color(0xFF00C853),
        secondaryVariant = Color(0xFF009624),
        onPrimary = Color.White,
        onSecondary = Color.White
    )

    val DarkColors = darkColors(
        primary = Color(0xFF90A4AE),
        primaryVariant = Color(0xFF62757f),
        secondary = Color(0xFF69f0ae),
        secondaryVariant = Color(0xFF2bbd7e),
        onPrimary = Color.Black,
        onSecondary = Color.Black
    )

    val Icons = Default

    val Shapes = androidx.compose.material.Shapes(
        small = RoundedCornerShape(topEnd = 8.dp, bottomStart = 8.dp),
        medium = RoundedCornerShape(topEnd = 16.dp, bottomStart = 16.dp),
        large = RoundedCornerShape(topEnd = 20.dp, bottomStart = 20.dp)
    )

    val Typography = androidx.compose.material.Typography(
        h3 = TextStyle(
            fontFamily = ExpletusSans,
            fontWeight = FontWeight.Normal,
            fontSize = 48.sp,
            letterSpacing = 0.sp
        ),
        h4 = TextStyle(
            fontFamily = ExpletusSans,
            fontWeight = FontWeight.Normal,
            fontSize = 34.sp,
            letterSpacing = 0.25.sp
        ),
        h5 = TextStyle(
            fontFamily = ExpletusSans,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            letterSpacing = 0.sp
        ),
        body1 = TextStyle(
            fontFamily = Fenix,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            letterSpacing = 0.5.sp
        ),
        button = TextStyle(
            fontFamily = Rubik,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            letterSpacing = 1.25.sp
        ),
        caption = TextStyle(
            fontFamily = RubikLight,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            letterSpacing = 0.4.sp
        ),

        )
}

val ExpletusSans = FontFamily(Font(R.font.expletus_sans_medium))
val Fenix = FontFamily(Font(R.font.fenix))
val Rubik = FontFamily(Font(R.font.rubik))
val RubikLight = FontFamily(Font(R.font.rubik_light))

