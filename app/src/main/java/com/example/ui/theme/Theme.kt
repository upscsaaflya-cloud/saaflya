package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NavyPrimaryDark,
    onPrimary = OnNavyPrimaryDark,
    primaryContainer = NavyContainerDark,
    onPrimaryContainer = OnNavyContainerDark,
    secondary = PeacockTealSecondaryDark,
    onSecondary = OnPeacockTealSecondaryDark,
    secondaryContainer = PeacockTealContainerDark,
    onSecondaryContainer = OnPeacockTealContainerDark,
    tertiary = FeatherGoldTertiaryDark,
    onTertiary = OnFeatherGoldTertiaryDark,
    tertiaryContainer = FeatherGoldContainerDark,
    onTertiaryContainer = OnFeatherGoldContainerDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimaryLight,
    onPrimary = OnNavyPrimaryLight,
    primaryContainer = NavyContainerLight,
    onPrimaryContainer = OnNavyContainerLight,
    secondary = PeacockTealSecondaryLight,
    onSecondary = OnPeacockTealSecondaryLight,
    secondaryContainer = PeacockTealContainerLight,
    onSecondaryContainer = OnPeacockTealContainerLight,
    tertiary = FeatherGoldTertiaryLight,
    onTertiary = OnFeatherGoldTertiaryLight,
    tertiaryContainer = FeatherGoldContainerLight,
    onTertiaryContainer = OnFeatherGoldContainerLight,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
