package io.github.proify.lyricon.mgaprovider.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** 参考毛怪助手（Miuix）的浅色配色 */
object MgColors {
    val Background = Color(0xFFFBF6F2)
    val Card = Color(0xFFFFFFFF)
    val Orange = Color(0xFFF5A623)
    val OrangeSoft = Color(0xFFFFF0D8)
    val TextPrimary = Color(0xFF1B1B1F)
    val TextSecondary = Color(0xFF8C8C92)
    val Divider = Color(0x14000000)
}

@Composable
fun MgTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = MgColors.Orange,
            onPrimary = Color.White,
            background = MgColors.Background,
            onBackground = MgColors.TextPrimary,
            surface = MgColors.Card,
            onSurface = MgColors.TextPrimary,
            surfaceVariant = MgColors.OrangeSoft,
        ),
        content = content,
    )
}
