package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = UnitySky,
    onPrimary = DarkCanvas,
    primaryContainer = UnitySkyContainer,
    onPrimaryContainer = DarkInkPrimary,
    secondary = LtsEmerald,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = LtsEmeraldContainer,
    tertiary = UnityIndigo,
    background = DarkCanvas,
    onBackground = DarkInkPrimary,
    surface = DarkCard,
    onSurface = DarkInkPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkInkSecondary,
    outline = DarkOutline,
    outlineVariant = DarkSurfaceVariant,
    error = ErrorCrimson,
    errorContainer = ErrorCrimsonContainer
)

private val LightColorScheme = lightColorScheme(
    primary = UnityBlue,
    onPrimary = Color.White,
    primaryContainer = UnityBlueContainer,
    onPrimaryContainer = UnityBlueDark,
    secondary = LtsEmerald,
    onSecondary = Color.White,
    secondaryContainer = LtsEmeraldContainer,
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = UnityIndigo,
    onTertiary = Color.White,
    tertiaryContainer = UnityIndigoContainer,
    onTertiaryContainer = Color(0xFF3730A3),
    background = StudioCanvas,
    onBackground = InkPrimary,
    surface = StudioCard,
    onSurface = InkPrimary,
    surfaceVariant = StudioSurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = HairlineOutline,
    outlineVariant = ToolbarBorder,
    error = ErrorCrimson,
    onError = Color.White,
    errorContainer = ErrorCrimsonContainer
)

val UnityHubShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun UnityHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Keep status bar icons dark because the top toolbar is always crisp white.
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = UnityHubShapes,
        content = content
    )
}
