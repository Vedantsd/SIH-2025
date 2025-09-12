package com.example.smartcropadvisory.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Agriculture-inspired light colors (always used)
private val AgricultureLightColors = lightColorScheme(
    primary = Color(0xFF4CAF50),      // Green (plants)
    secondary = Color(0xFF8BC34A),    // Light Green
    tertiary = Color(0xFFFFC107),     // Yellow / Sun / Crops
    background = Color(0xFFE8F5E9),   // Light green background
    surface = Color(0xFFC8E6C9),      // Pale green surface for cards
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun SmartCropAdvisoryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AgricultureLightColors,
        typography = Typography, // keep your existing Typography
        content = content
    )
}
