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
    onPrimaryContainer = OnPrimaryContainerLight,

    secondary = SecondaryOrange,  // #FB923C
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,

    tertiary = Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,

    background = BackgroundLight,  // #F9FAFB
    onBackground = TextPrimary,  // #1F2937

    surface = Color.White,
    onSurface = TextPrimary,  // #1F2937
    surfaceVariant = SurfaceLight,  // #F3F4F6
    onSurfaceVariant = TextSecondary,  // #6B7280

    outline = Gray300,
    outlineVariant = Gray200,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = PrimaryPurpleLight,

    secondary = SecondaryOrange,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = SecondaryContainerLight,

    background = BackgroundDark,  // #18181B
    onBackground = TextDarkPrimary,  // #F4F4F5

    surface = SurfaceDark,  // #27272A
    onSurface = TextDarkPrimary,  // #F4F4F5
    surfaceVariant = SurfaceVariantDark,
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