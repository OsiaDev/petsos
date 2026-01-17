package com.osia.petsos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,  // #7C3AED
    onPrimary = Color.White,
    primaryContainer = PrimaryPurpleLight,  // #F3E8FF
    onPrimaryContainer = Color(0xFF21005D),

    secondary = SecondaryOrange,  // #FB923C
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = Color(0xFF7C2D12),

    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),

    error = ErrorLight,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = BackgroundLight,  // #F9FAFB
    onBackground = TextPrimary,  // #1F2937

    surface = Color.White,
    onSurface = TextPrimary,  // #1F2937
    surfaceVariant = SurfaceLight,  // #F3F4F6
    onSurfaceVariant = TextSecondary,  // #6B7280

    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB),
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = PrimaryPurpleLight,

    secondary = SecondaryOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF9A3412),
    onSecondaryContainer = Color(0xFFFFEDD5),

    background = BackgroundDark,  // #18181B
    onBackground = TextDarkPrimary,  // #F4F4F5

    surface = SurfaceDark,  // #27272A
    onSurface = TextDarkPrimary,  // #F4F4F5
    surfaceVariant = Color(0xFF3F3F46),
    onSurfaceVariant = TextSecondaryDark,  // #A1A1AA
)

@Composable
fun PetSOSTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TypographySans,
        content = content
    )
}