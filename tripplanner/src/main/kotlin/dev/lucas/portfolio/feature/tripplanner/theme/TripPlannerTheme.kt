package dev.lucas.portfolio.feature.tripplanner.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

val LocalThemeMode = compositionLocalOf<ThemeMode> { ThemeMode.SYSTEM }
val LocalSetThemeMode = compositionLocalOf<(ThemeMode) -> Unit> { {} }

private val TripLightColorScheme = lightColorScheme(
    primary              = LightPrimary,
    onPrimary            = LightOnPrimary,
    primaryContainer     = LightPrimaryContainer,
    onPrimaryContainer   = LightOnPrimaryContainer,
    secondary            = LightSecondary,
    onSecondary          = LightOnSecondary,
    secondaryContainer   = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary             = LightTertiary,
    onTertiary           = LightOnTertiary,
    tertiaryContainer    = LightTertiaryContainer,
    onTertiaryContainer  = LightOnTertiaryContainer,
    background           = LightBackground,
    onBackground         = LightOnBackground,
    surface              = LightSurface,
    onSurface            = LightOnSurface,
    surfaceVariant       = LightSurfaceVariant,
    onSurfaceVariant     = LightOnSurfaceVariant,
    outline              = LightOutline,
    outlineVariant       = LightOutlineVariant,
    error                = LightError,
    onError              = LightOnError,
    errorContainer       = LightErrorContainer,
    onErrorContainer     = LightOnErrorContainer,
    inverseSurface       = LightInverseSurface,
    inverseOnSurface     = LightInverseOnSurface,
    inversePrimary       = LightInversePrimary,
)

private val TripDarkColorScheme = darkColorScheme(
    primary              = DarkPrimary,
    onPrimary            = DarkOnPrimary,
    primaryContainer     = DarkPrimaryContainer,
    onPrimaryContainer   = DarkOnPrimaryContainer,
    secondary            = DarkSecondary,
    onSecondary          = DarkOnSecondary,
    secondaryContainer   = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary             = DarkTertiary,
    onTertiary           = DarkOnTertiary,
    tertiaryContainer    = DarkTertiaryContainer,
    onTertiaryContainer  = DarkOnTertiaryContainer,
    background           = DarkBackground,
    onBackground         = DarkOnBackground,
    surface              = DarkSurface,
    onSurface            = DarkOnSurface,
    surfaceVariant       = DarkSurfaceVariant,
    onSurfaceVariant     = DarkOnSurfaceVariant,
    outline              = DarkOutline,
    outlineVariant       = DarkOutlineVariant,
    error                = DarkError,
    onError              = DarkOnError,
    errorContainer       = DarkErrorContainer,
    onErrorContainer     = DarkOnErrorContainer,
    inverseSurface       = DarkInverseSurface,
    inverseOnSurface     = DarkInverseOnSurface,
    inversePrimary       = DarkInversePrimary,
)

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

    CompositionLocalProvider(
        LocalThemeMode provides themeMode,
        LocalSetThemeMode provides onSetThemeMode,
    ) {
        MaterialTheme(
            colorScheme = if (useDark) TripDarkColorScheme else TripLightColorScheme,
            content     = content,
        )
    }
}
