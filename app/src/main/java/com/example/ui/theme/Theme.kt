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
    primary = BrandSecondaryBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueDark,
    onPrimaryContainer = Color(0xFFD4E5FB),
    secondary = BrandYellow,
    onSecondary = OnBrandYellow,
    secondaryContainer = Color(0xFF4D3800),
    onSecondaryContainer = BrandYellowContainer,
    tertiary = BrandSecondaryBlue,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceElevated,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueContainer,
    onPrimaryContainer = OnBrandBlueContainer,
    secondary = BrandYellow,
    onSecondary = OnBrandYellow,
    secondaryContainer = BrandYellowContainer,
    onSecondaryContainer = OnBrandYellowContainer,
    tertiary = BrandSecondaryBlue,
    onTertiary = Color.White,
    background = BrandPageBackground,
    surface = BrandSurface,
    surfaceVariant = BrandSurfaceElevated,
    onBackground = BrandTextPrimary,
    onSurface = BrandTextPrimary,
    onSurfaceVariant = BrandTextSecondary,
    outline = BrandBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent premium Blue & Yellow identity
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
