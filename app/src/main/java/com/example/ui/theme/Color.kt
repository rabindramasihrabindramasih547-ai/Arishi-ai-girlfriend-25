package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary neon & romantic companion tones
val ArishiPurple = Color(0xFFA855F7)
val ArishiPurpleDark = Color(0xFF160D27)
val ArishiSurfaceDark = Color(0xFF221538)
val ArishiSurfaceLightDark = Color(0xFF2E1C4A)
val ArishiPink = Color(0xFFF43F5E)
val ArishiLavender = Color(0xFFD8B4FE)
val ArishiNeonGlow = Color(0xFFE879F9)
val ArishiCyanAccent = Color(0xFF38BDF8)
val ArishiTextPrimary = Color(0xFFF8FAFC)
val ArishiTextSecondary = Color(0xFFCBD5E1)
val ArishiTextMuted = Color(0xFF94A3B8)
val ArishiCardBorder = Color(0xFF4C2A78)
val ArishiSuccess = Color(0xFF10B981)
val ArishiWarning = Color(0xFFF59E0B)

val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = ArishiPink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF581C87),
    onPrimaryContainer = ArishiLavender,
    secondary = ArishiLavender,
    onSecondary = ArishiPurpleDark,
    secondaryContainer = Color(0xFF3B185F),
    onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = ArishiCyanAccent,
    onTertiary = Color.Black,
    background = ArishiPurpleDark,
    onBackground = ArishiTextPrimary,
    surface = ArishiSurfaceDark,
    onSurface = ArishiTextPrimary,
    surfaceVariant = ArishiSurfaceLightDark,
    onSurfaceVariant = ArishiTextSecondary,
    outline = ArishiCardBorder
)
