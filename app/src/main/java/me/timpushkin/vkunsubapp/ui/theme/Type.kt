package me.timpushkin.vkunsubapp.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.timpushkin.vkunsubapp.R

val vkSans = FontFamily(
    Font(R.font.vksans_display_medium, weight = FontWeight.Medium)
)

val Typography = Typography(
    h6 = TextStyle(
        fontFamily = vkSans,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp
    ),
    body1 = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    body2 = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    ),
    button = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)
