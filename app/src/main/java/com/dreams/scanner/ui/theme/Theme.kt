package com.dreams.scanner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand palette: deep navy + white with sky-blue accents.
private val LightColors = lightColorScheme(
    primary = Color(0xFF0B2A5B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD9EAFF),
    onPrimaryContainer = Color(0xFF04173A),
    secondary = Color(0xFF0A84C8),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCDEBFF),
    onSecondaryContainer = Color(0xFF00263D),
    tertiary = Color(0xFF00A6D6),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC4F0FF),
    onTertiaryContainer = Color(0xFF00212C),
    background = Color(0xFFF5F8FC),
    onBackground = Color(0xFF0F1B2D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F1B2D),
    surfaceVariant = Color(0xFFE2EAF4),
    onSurfaceVariant = Color(0xFF43515F),
    outline = Color(0xFF73808F),
    outlineVariant = Color(0xFFC4CEDA),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F5FB),
    surfaceContainer = Color(0xFFEAF0F8),
    surfaceContainerHigh = Color(0xFFE4ECF6),
    surfaceContainerHighest = Color(0xFFDFE8F3)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7CCBFF),
    onPrimary = Color(0xFF00263F),
    primaryContainer = Color(0xFF14396E),
    onPrimaryContainer = Color(0xFFD6EAFF),
    secondary = Color(0xFF4FC3F7),
    onSecondary = Color(0xFF00293B),
    secondaryContainer = Color(0xFF0E4A6B),
    onSecondaryContainer = Color(0xFFC9EBFF),
    tertiary = Color(0xFF5CE1FF),
    onTertiary = Color(0xFF00363F),
    tertiaryContainer = Color(0xFF004E5C),
    onTertiaryContainer = Color(0xFFB8F0FF),
    background = Color(0xFF07132A),
    onBackground = Color(0xFFE3ECF8),
    surface = Color(0xFF0C1B38),
    onSurface = Color(0xFFE3ECF8),
    surfaceVariant = Color(0xFF1B2D4D),
    onSurfaceVariant = Color(0xFFB5C3D6),
    outline = Color(0xFF7E8DA3),
    outlineVariant = Color(0xFF2B3E5F),
    surfaceContainerLowest = Color(0xFF050E20),
    surfaceContainerLow = Color(0xFF0A1830),
    surfaceContainer = Color(0xFF102240),
    surfaceContainerHigh = Color(0xFF16294A),
    surfaceContainerHighest = Color(0xFF1D3256)
)

@Composable
fun DreamsTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
