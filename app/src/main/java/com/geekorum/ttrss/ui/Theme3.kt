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
package com.geekorum.ttrss.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geekorum.ttrss.R


@Composable
fun AppTheme3(
    darkTheme: Boolean = isSystemInDarkTheme(),
    enableDynamicColors: Boolean = true,
    content: @Composable () -> Unit) {
    val colorScheme = when {
        enableDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> AppTheme3.DarkColorScheme
        else -> AppTheme3.LightColorScheme
    }
    AppTheme3(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
fun AppTheme3(colorScheme: ColorScheme, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTheme3.Typography,
        shapes = AppTheme3.Shapes,
        content = content
    )
}


private val ExpletusSans = FontFamily(Font(R.font.expletus_sans_medium))
private val Fenix = FontFamily(Font(R.font.fenix))
private val Rubik = FontFamily(Font(R.font.rubik))
private val RubikLight = FontFamily(Font(R.font.rubik_light))



object AppTheme3 {
    val LightColorScheme = lightColorScheme(
        primary = Color(0xFF006783),
        primaryContainer = Color(0xFFbde9ff),
        onPrimaryContainer = Color(0xFF001f2a),
        secondary = Color(0xFF006783),
        secondaryContainer = Color(0xFFbde9ff),
        onSecondaryContainer = Color(0xFF001f2a),
        tertiary = Color(0xFF006e2a),
        tertiaryContainer = Color(0xFF69ff87),
        onTertiaryContainer = Color(0xFF002108),
        error = Color(0xFFba1a1a),
        errorContainer = Color(0xFFffdad6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFf8fdff),
        onBackground = Color(0xFF001f25),
        surface = Color(0xFFf8fdff),
        onSurface = Color(0xFF001f25),
        outline = Color(0xFF70787d),
        surfaceVariant = Color(0xFFdce4e9),
        onSurfaceVariant = Color(0xFF40484c),
    )

    val DarkColorScheme = darkColorScheme(
        primary = Color(0xFF65d3ff),
        onPrimary = Color(0xFF003546),
        primaryContainer = Color(0xFF004d64),
        onPrimaryContainer = Color(0xFFbde9ff),
        secondary = Color(0xFF64d3ff),
        onSecondary = Color(0xFF003546),
        secondaryContainer = Color(0xFF004d64),
        onSecondaryContainer = Color(0xFFbde9ff),
        tertiary = Color(0xFF3ce36a),
        onTertiary = Color(0xFF003912),
        tertiaryContainer = Color(0xFF00531e),
        onTertiaryContainer = Color(0xFF69ff87),
        error = Color(0xFFffb4ab),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000a),
        onErrorContainer = Color(0xFFffdad6),
        background = Color(0xFF001f25),
        onBackground = Color(0xFFa6eeff),
        surface = Color(0xFF001f25),
        onSurface = Color(0xFFa6eeff),
        outline = Color(0xFF8a9297),
        surfaceVariant = Color(0xFF40484c),
        onSurfaceVariant = Color(0xFFc0c8cd),
    )

    object Colors {
        val MaterialGreenA700 = Color(0xFF00C853)
    }

    val Icons = Default

    val Shapes = Shapes(
        extraSmall = RoundedCornerShape(topEnd = 4.dp, bottomStart = 4.dp),
        small = RoundedCornerShape(topEnd = 8.dp, bottomStart = 8.dp),
        medium = RoundedCornerShape(topEnd = 12.dp, bottomStart = 12.dp),
        large = RoundedCornerShape(topEnd = 16.dp, bottomStart = 16.dp),
        extraLarge = RoundedCornerShape(topEnd = 28.dp, bottomStart = 28.dp)
    )

    val Typography = Typography(
        displaySmall = TextStyle(
            fontFamily = ExpletusSans,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = ExpletusSans,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = ExpletusSans,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = ExpletusSans,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = Fenix,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        labelLarge = TextStyle(
            fontFamily = Rubik,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        bodySmall = TextStyle(
            fontFamily = RubikLight,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),

        )
}