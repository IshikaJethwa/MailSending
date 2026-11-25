package com.dn.recruitermailer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Indigo = Color(0xFF2F5BEA)
val Violet = Color(0xFF6A3DE8)
val Mint = Color(0xFF19C3A5)

val HeaderGradient = Brush.linearGradient(listOf(Indigo, Violet))

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    secondary = Mint,
    onSecondary = Color.White,
    background = Color(0xFFF3F5FA),
    onBackground = Color(0xFF1B2433),
    surface = Color.White,
    onSurface = Color(0xFF1B2433),
    surfaceVariant = Color(0xFFE9EDF7),
    onSurfaceVariant = Color(0xFF5B6475),
    outline = Color(0xFFCBD2E0),
    error = Color(0xFFD64545)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FA6FF),
    onPrimary = Color(0xFF0E1530),
    secondary = Mint,
    onSecondary = Color(0xFF00201A),
    background = Color(0xFF0F1320),
    onBackground = Color(0xFFE6EAF2),
    surface = Color(0xFF181D2C),
    onSurface = Color(0xFFE6EAF2),
    surfaceVariant = Color(0xFF232A3D),
    onSurfaceVariant = Color(0xFFA3ACBF),
    outline = Color(0xFF39425A),
    error = Color(0xFFFF7A7A)
)

@Composable
fun MailerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
