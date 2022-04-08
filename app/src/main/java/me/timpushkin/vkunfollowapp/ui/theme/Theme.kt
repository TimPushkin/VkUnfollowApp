package me.timpushkin.vkunfollowapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColors(
    primary = Blue,
    primaryVariant = Grey2,
    secondary = Grey1,
    secondaryVariant = Grey1,
    background = White,
    surface = LightGrey1,
    onPrimary = White,
    onSecondary = White,
    onBackground = Black,
    onSurface = Black
)

private val DarkColorPalette = darkColors(
    primary = Blue,
    primaryVariant = Grey3,
    secondary = Grey1,
    secondaryVariant = Grey1,
    background = LightBlack1,
    surface = LightBlack2,
    onPrimary = White,
    onSecondary = White,
    onBackground = LightGray2,
    onSurface = LightGray2
)

@Composable
fun VkFollowAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
