package com.example.ui.theme

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
    primary = SatPrimary,
    onPrimary = Color.White,
    primaryContainer = SatDarkSurfaceVariant,
    onPrimaryContainer = SatSecondary,
    secondary = SatSecondary,
    onSecondary = Color.Black,
    tertiary = SatTertiary,
    onTertiary = Color.Black,
    background = SatDarkBackground,
    onBackground = SatOnDark,
    surface = SatDarkSurface,
    onSurface = SatOnDark,
    surfaceVariant = SatDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC7C2E5)
)

private val LightColorScheme = lightColorScheme(
    primary = SatLightPrimary,
    onPrimary = Color.White,
    primaryContainer = SatLightSurfaceVariant,
    onPrimaryContainer = SatLightPrimary,
    secondary = SatLightSecondary,
    onSecondary = Color.White,
    tertiary = SatTertiary,
    background = SatLightBackground,
    onBackground = Color(0xFF161327),
    surface = SatLightSurface,
    onSurface = Color(0xFF161327),
    surfaceVariant = SatLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF4A4465)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our branded palette by default for strong identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Store is sleek in dark mode by default for high-tech aesthetic
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
