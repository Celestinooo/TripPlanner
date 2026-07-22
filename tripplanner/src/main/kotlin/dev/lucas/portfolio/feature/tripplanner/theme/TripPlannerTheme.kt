package dev.lucas.portfolio.feature.tripplanner.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

// Composition locals expostos para que screens e ViewModels possam
// ler e alterar o tema sem depender do core:designsystem do Portfolio.
val LocalThemeMode = compositionLocalOf<ThemeMode> { ThemeMode.SYSTEM }
val LocalSetThemeMode = compositionLocalOf<(ThemeMode) -> Unit> { {} }

@Composable
fun TripPlannerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onSetThemeMode: (ThemeMode) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val useDark = when (themeMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        useDark  -> darkColorScheme()
        else     -> lightColorScheme()
    }

    CompositionLocalProvider(
        LocalThemeMode provides themeMode,
        LocalSetThemeMode provides onSetThemeMode,
    ) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
