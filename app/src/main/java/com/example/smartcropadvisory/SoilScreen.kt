package com.example.smartcropadvisory

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // For CropSelectionDialog
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// --- IMPORT YOUR CENTRALIZED CONSTANTS AND MODELS ---
// It is CRITICAL that these are defined ONLY ONCE, typically in a separate Models.kt or Constants.kt
// and then imported here.
import com.example.smartcropadvisory.models.SoilHealth
import com.example.smartcropadvisory.models.SoilTestUnique
import com.example.smartcropadvisory.models.Feature // Assuming you use this from AppModels.kt
// If SELECTED_CROP_PLACEHOLDER and sampleCropsList are in AppModels.kt or a Constants.kt, import them:
// import com.example.smartcropadvisory.models.SELECTED_CROP_PLACEHOLDER
// import com.example.smartcropadvisory.models.sampleCropsList
// OR if they are top-level constants in another file in this package:
// (no explicit import needed if in the same package and not private)


// --- Placeholder Data Fetching Functions (Adapt to your ViewModel/Data Layer) ---
// Ensure these functions use the *imported* SELECTED_CROP_PLACEHOLDER
fun getSoilHealthDataForRecommendation(): SoilHealth? {
    return SoilHealth(
        location = "Field Alpha",
        fertility = "Medium (65%)",
        waterHoldingCapacity = "Good",
        organicMatter = "Low (1.1%)",
        phLevel = "5.8 (Acidic)",
        note = "Requires organic matter and pH correction for optimal growth."
    )
}

fun getSoilTestDataForRecommendation(): List<SoilTestUnique> {
    // Uses the global/imported SELECTED_CROP_PLACEHOLDER
    return listOf(
        SoilTestUnique("pH Level", "5.8", "Apply lime to raise pH for $SELECTED_CROP_PLACEHOLDER.", 30, "Low"),
        SoilTestUnique("Organic Carbon", "0.6%", "Incorporate compost or manure before planting $SELECTED_CROP_PLACEHOLDER.", 35, "Low"),
        SoilTestUnique("Nitrogen (N)", "25 kg/ha", "Apply Urea (40-50 kg/ha) for $SELECTED_CROP_PLACEHOLDER.", 40, "Low"),
        SoilTestUnique("Phosphorus (P)", "15 kg/ha", "Use DAP (50-60 kg/ha) for $SELECTED_CROP_PLACEHOLDER.", 30, "Low"),
        SoilTestUnique("Potassium (K)", "120 kg/ha", "Sufficient for $SELECTED_CROP_PLACEHOLDER, monitor crop response.", 75, "Good"),
        SoilTestUnique("Zinc (Zn)", "0.5 ppm", "Consider Zinc Sulphate for $SELECTED_CROP_PLACEHOLDER if deficiency symptoms appear.", 45, "Low")
    )
}

private val soilScreenFeatures: List<Feature> = listOf(
    Feature(
        title = "Soil Health Overview",
        icon = Icons.Default.Grass,
        route = ScreenRoutes.SoilHealth.route
    ),
    Feature(
        title = "Soil Testing Dashboard",
        icon = Icons.Default.Build,
        route = ScreenRoutes.SoilTesting.route
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilScreen(navController: NavController) {
    var selectedCrop by remember { mutableStateOf<String?>(null) }
    var showCropSelectionDialog by remember { mutableStateOf(false) }

    val currentSoilHealth = getSoilHealthDataForRecommendation()
    val rawSoilTestData = getSoilTestDataForRecommendation()

    val currentSoilTestData = remember(selectedCrop, rawSoilTestData) {
        if (selectedCrop != null) {
            // Uses the global/imported SELECTED_CROP_PLACEHOLDER
            rawSoilTestData.map {
                it.copy(recommendation = it.recommendation.replace(SELECTED_CROP_PLACEHOLDER, selectedCrop!!))
            }
        } else {
            rawSoilTestData
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🌱 Soil Services & Recommendations") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "Explore Soil Services",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(soilScreenFeatures) { feature ->
                    SoilFeatureTile(feature = feature, onClick = {
                        Log.d("SoilScreen", "Attempting to navigate to: ${feature.route} for ${feature.title}")
                        navController.navigate(feature.route)
                    })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Get Fertilizer Recommendations",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(
                onClick = { showCropSelectionDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Agriculture, contentDescription = "Select Crop", tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedCrop ?: "Tap to select crop", color = MaterialTheme.colorScheme.onPrimary)
            }

            if (showCropSelectionDialog) {
                CropSelectionDialog(
                    crops = sampleCropsList, // Uses the global/imported sampleCropsList
                    onCropSelected = { crop ->
                        selectedCrop = crop
                        showCropSelectionDialog = false
                    },
                    onDismiss = { showCropSelectionDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ***** ENSURE FertilizerRecommendationCard IS DEFINED OR IMPORTED *****
            if (selectedCrop != null) {
                FertilizerRecommendationCard( // This line was erroring out
                    crop = selectedCrop!!,
                    soilHealth = currentSoilHealth,
                    soilTestData = currentSoilTestData
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        "Please select a crop to view tailored fertilizer recommendations.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SoilFeatureTile(feature: Feature, onClick: () -> Unit) { // Parameter type changed to models.Feature
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick, role = androidx.compose.ui.semantics.Role.Button)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(68.dp),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = feature.title,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropSelectionDialog(
    crops: List<String>,
    onCropSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Crop", style = MaterialTheme.typography.headlineSmall) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                items(crops) { crop ->
                    Text(
                        text = crop,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCropSelected(crop) }
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}

// ***** START: Definition for FertilizerRecommendationCard (and its helpers) *****
// MOVE THIS (and its helper composables/functions) to its own file or integrate it
// properly if it already exists elsewhere and just needs importing.
// For now, I'm adding a basic definition here to resolve the "Unresolved reference".

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerRecommendationCard(
    crop: String,
    soilHealth: SoilHealth?,
    soilTestData: List<SoilTestUnique>? // Changed to List<SoilTestUnique>?
) {
    var soilHealthExpanded by remember { mutableStateOf(false) }
    var cropTipsExpanded by remember { mutableStateOf(false) }

    // Use the getRecommendedFertilizers function that was already in your file
    val recommendedFertilizers = getRecommendedFertilizers(crop, soilHealth, soilTestData)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Spa,
                    contentDescription = "Recommendations",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Fertilizer Plan for $crop",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            if (soilHealth == null && soilTestData.isNullOrEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = "Warning", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Soil data not yet available. Please check Soil Health and Soil Testing sections to get recommendations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                return@Column // Exit Column early if no data
            }

            soilHealth?.let {
                ExpandableRecommendationSection(
                    title = "Soil Health Insights (${it.location})",
                    icon = Icons.Filled.Eco,
                    expanded = soilHealthExpanded,
                    onToggle = { soilHealthExpanded = !soilHealthExpanded }
                ) {
                    RecommendationDetailItem("Overall Fertility:", it.fertility)
                    RecommendationDetailItem("Organic Matter:", it.organicMatter,
                        highlight = it.organicMatter.contains("Low", ignoreCase = true) ||
                                (it.organicMatter.filter { c -> c.isDigit() || c == '.' }.toDoubleOrNull() ?: 2.0) < 1.5
                    )
                    RecommendationDetailItem("pH Level:", it.phLevel,
                        highlight = (it.phLevel.filter { c -> c.isDigit() || c == '.' }.toDoubleOrNull() ?: 7.0).let { ph -> ph < 6.0 || ph > 7.5 }
                    )
                    // ... (rest of the content from your original FertilizerRecommendationCard)
                    if (it.organicMatter.contains("Low", ignoreCase = true) || (it.organicMatter.filter { c -> c.isDigit() || c == '.' }.toDoubleOrNull() ?: 2.0) < 1.5) {
                        HighlightedTip("💡 Boost organic matter with compost or manure for better soil structure and nutrient retention.")
                    }
                    (it.phLevel.filter { c -> c.isDigit() || c == '.' }.toDoubleOrNull() ?: 7.0).let { ph ->
                        if (ph < 6.0) HighlightedTip("⚠️ Acidic soil (pH ${String.format("%.1f", ph)}): Consider applying agricultural lime for $crop.")
                        else if (ph > 7.5) HighlightedTip("⚠️ Alkaline soil (pH ${String.format("%.1f", ph)}): Elemental sulfur or gypsum might be needed for $crop, depending on specific conditions.")
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }

            Text(
                "Recommended Fertilizers for $crop",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.secondary
            )
            if (recommendedFertilizers.isNotEmpty()) {
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    recommendedFertilizers.forEach { fertilizer ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Filled.BubbleChart, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(fertilizer, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            } else {
                Text("Unable to generate specific fertilizer recommendations with current data. Please ensure soil tests are up to date.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
            }
            Divider(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            ExpandableRecommendationSection(
                title = "General Crop Tips for $crop",
                icon = Icons.Filled.Forest,
                expanded = cropTipsExpanded,
                onToggle = { cropTipsExpanded = !cropTipsExpanded }
            ) {
                // ... (content of your crop tips from original FertilizerRecommendationCard)
                when (crop) {
                    "Wheat" -> { HighlightedTip("🌾 Ensure balanced NPK...") } // Add full tip
                    "Maize" -> { HighlightedTip("🌽 High Nitrogen demand...") } // Add full tip
                    // Add all other crop tips from your original file
                    else -> HighlightedTip("Follow local agricultural extension advice for $crop.")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)) {
                Button(onClick = { /* TODO */ }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Filled.ShoppingBag, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Find Fertilizers")
                }
                OutlinedButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Filled.LibraryBooks, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Learn More")
                }
            }
        }
    }
}

// Placeholder logic for getRecommendedFertilizers - Ensure this is the one you want to use
fun getRecommendedFertilizers(
    crop: String,
    soilHealth: SoilHealth?,
    soilTestData: List<SoilTestUnique>?
): List<String> {
    val recommendations = mutableListOf<String>()
    soilHealth?.phLevel?.let { phString -> /* ... your existing logic ... */ }
    soilHealth?.organicMatter?.let { omString -> /* ... your existing logic ... */ }
    var needsNitrogen = false; var needsPhosphorus = false; var needsPotassium = false
    soilTestData?.forEach { test -> /* ... your existing logic ... */ }
    if (needsNitrogen /* ... */) { recommendations.add("Urea or Ammonium Nitrate (Nitrogen source)")}
    if (needsPhosphorus /* ... */) { recommendations.add("DAP or SSP (Phosphorus source)")}
    if (needsPotassium /* ... */) { recommendations.add("Muriate of Potash (MOP - Potassium source)")}
    if (needsNitrogen || needsPhosphorus || needsPotassium) { /* ... NPK logic ... */ }
    when (crop) { "Tomato" -> { /* ... */ } }
    if (recommendations.isEmpty()) { recommendations.add("General purpose fertilizer recommended.") }
    return recommendations.distinct()
}


// These helper composables were part of your provided FertilizerRecommendationCard logic
@Composable
fun ExpandableRecommendationSection(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle, role = androidx.compose.ui.semantics.Role.Button).padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Icon(imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = if (expanded) "Collapse" else "Expand", tint = MaterialTheme.colorScheme.secondary)
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                content()
            }
        }
    }
}

@Composable
fun RecommendationDetailItem(label: String, value: String, highlight: Boolean = false) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(140.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = if (highlight) MaterialTheme.colorScheme.error else LocalContentColor.current)
    }
}

@Composable
fun HighlightedTip(text: String, isWarning: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(color = if (isWarning) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        val iconToShow = when { text.startsWith("💡") -> Icons.Filled.Lightbulb; text.startsWith("⚠️") -> Icons.Filled.WarningAmber; else -> Icons.Filled.Info }
        Icon(imageVector = iconToShow, contentDescription = "Tip", tint = if (isWarning || text.startsWith("⚠️")) MaterialTheme.colorScheme.error else if (text.startsWith("💡")) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(end = 8.dp).size(20.dp))
        Text(text = text.removePrefix("💡 ").removePrefix("⚠️ "), style = MaterialTheme.typography.bodyMedium, color = if (isWarning || text.startsWith("⚠️")) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer)
    }
}

// ***** END: Definition for FertilizerRecommendationCard (and its helpers) *****


// --- REMOVE DUMMY DEFINITIONS FROM THIS FILE ---
// These should be defined ONCE in your project, likely in AppModels.kt or a Constants.kt file
// and then IMPORTED at the top of this file.
// const val SELECTED_CROP_PLACEHOLDER = "[CROP_NAME]" // REMOVE IF DEFINED ELSEWHERE
// val sampleCropsList: List<String> = listOf("Wheat", ...) // REMOVE IF DEFINED ELSEWHERE

