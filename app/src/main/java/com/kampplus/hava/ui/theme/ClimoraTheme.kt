package com.kampplus.hava.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ClimoraColors = darkColorScheme(
    primary = Color(0xFF9FD5FF), onPrimary = Color(0xFF082C50),
    primaryContainer = Color(0xFF214C78), onPrimaryContainer = Color(0xFFE0F0FF),
    secondary = Color(0xFFF6CE82), secondaryContainer = Color(0xFF203C59),
    onSecondaryContainer = Color(0xFFE0F0FF), surfaceVariant = Color(0xFF24415D),
    background = Color(0xFF071B37), surface = Color(0xFF0C2444),
    surfaceContainer = Color(0xCC193D66), onSurface = Color(0xFFF3F7FF),
    onBackground = Color.White, onSurfaceVariant = Color(0xFFCCDCEC)
)

@Composable
fun ClimoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ClimoraColors, typography = HavaTypography, content = content)
}
