package com.example.smartcropadvisory

import androidx.compose.ui.graphics.vector.ImageVector

// --- Shared Data Models ---

data class SoilHealth(
    val location: String,
    val fertility: String,
    val waterHoldingCapacity: String,
    val organicMatter: String,
    val phLevel: String,
    val note: String // Ensure 'note' is present
)

data class SoilTestUnique(
    val parameter: String,
    val value: String,
    val recommendation: String,
    val percentage: Int, // Ensure 'percentage' is present
    val status: String
)

data class SoilFeature(
    val title: String,
    val route: String,
    val icon: ImageVector
)

const val SELECTED_CROP_PLACEHOLDER = "[Selected Crop]"
val sampleCropsList = listOf("Wheat", "Maize", "Paddy", "Cotton", "Sugarcane", "Soybean", "Tomato")
