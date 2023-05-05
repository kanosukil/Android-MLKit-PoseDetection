package edu.blazepose.fallencheck.ui.theme

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)

private val primaryColor = Color(0xFF5E92F3)
private val primaryLightColor = Color(0xFF1565C0)
private val primaryDarkColor = Color(0xFF003C8F)
private val secondaryColor = Color(0xFF3949AB)
private val secondaryLightColor = Color(0xFF6F74DD)
private val secondaryDarkColor = Color(0xFF00227B)

val DarkColors = darkColors(
    primary = primaryColor,
    primaryVariant = primaryDarkColor,
    secondary = secondaryColor,
    secondaryVariant = secondaryDarkColor
)
val LightColors = lightColors(
    primary = primaryColor,
    primaryVariant = primaryLightColor,
    secondary = secondaryColor,
    secondaryVariant = secondaryLightColor
)