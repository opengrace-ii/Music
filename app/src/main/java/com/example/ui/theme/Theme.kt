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
    primary = PeachLight,
    onPrimary = InkDark,
    primaryContainer = PeachDark,
    onPrimaryContainer = PeachSoft,
    secondary = PeachPrimary,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = DarkInkPrimary,
    surface = DarkSurface,
    onSurface = DarkInkPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkInkSecondary,
    outline = InkSoft
)

private val LightColorScheme = lightColorScheme(
    primary = PeachPrimary,
    onPrimary = OnPeachPrimary,
    primaryContainer = PeachSoft,
    onPrimaryContainer = InkDark,
    secondary = PeachDark,
    onSecondary = Color.White,
    background = CreamBackground,
    onBackground = InkDark,
    surface = CreamSurface,
    onSurface = InkDark,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = InkMedium,
    outline = Color(0xFFE8D5C8)
)

@Composable
fun OGMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve OGM branded peach aesthetic by default
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
