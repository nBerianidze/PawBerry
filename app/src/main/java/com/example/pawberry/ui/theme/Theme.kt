package com.example.pawberry.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BerryLight,
    secondary = LeafLight,
    tertiary = PeachLight,
    background = Color(0xFF1C1214),
    surface = Color(0xFF26181B),
    onPrimary = Color(0xFF3D0014),
    onSecondary = Color(0xFF123012),
    onBackground = Color(0xFFFFF0F2),
    onSurface = Color(0xFFFFF0F2),
)

private val LightColorScheme = lightColorScheme(
    primary = Berry,
    secondary = Leaf,
    tertiary = Peach,
    background = Cream,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BerryDark,
    onSurface = Color(0xFF3A2A2D),
    error = Color(0xFFB3261E),
)

@Composable
fun PawBerryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
