package com.example.smartcropadvisory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// --- IMPORT YOUR ACTUAL DATA MODELS ---
import com.example.smartcropadvisory.models.SoilHealth
import com.example.smartcropadvisory.models.SoilTestUnique

// --- Data class for Crop Cost Factors ---
data class CropCostFactors(
    val seedCostPerAcre: Double,
    val fertilizerCostPerAcre: Double,
    val pesticideCostPerAcre: Double,
    val laborCostPerAcre: Double,
) {
    val totalEstimatedCostPerAcre: Double
        get() = seedCostPerAcre + fertilizerCostPerAcre + pesticideCostPerAcre + laborCostPerAcre
}

// --- Sample Crop Cost Database (VERY SIMPLIFIED) ---
val cropCostDatabase: Map<String, CropCostFactors> = mapOf(
    "Tomato" to CropCostFactors(1500.0, 3000.0, 2000.0, 5000.0),
    "Potato" to CropCostFactors(4000.0, 3500.0, 1500.0, 6000.0),
    "Lettuce" to CropCostFactors(1000.0, 2000.0, 1000.0, 3000.0),
    "Maize" to CropCostFactors(2000.0, 4000.0, 1000.0, 4000.0),
    "Wheat" to CropCostFactors(1200.0, 3000.0, 800.0, 3500.0)
)

// --- Data class for Recommended Crop ---
data class RecommendedCrop(
    val name: String,
    val suitabilityScore: Int,
    val reasons: List<String>,
    val notes: String? = null,
    val estimatedCostPerAcre: Double?,
    val withinBudget: Boolean? = null
)

// --- Placeholder Data Fetching Functions (Correctly Typed) ---
fun getCurrentSoilHealthForCropRec(): SoilHealth? {
    return SoilHealth(
        location = "Field Alpha", fertility = "Medium (65%)", waterHoldingCapacity = "Good",
        organicMatter = "Low (1.1%)", phLevel = "5.8 (Acidic)",
        note = "Requires organic matter and potential pH correction."
    )
}

fun getCurrentSoilTestDataForCropRec(): List<SoilTestUnique> {
    return listOf(
        SoilTestUnique("pH Level", "5.8", "N/A", 30, "Low"),
        SoilTestUnique("Organic Carbon", "0.6%", "N/A", 35, "Low"),
        SoilTestUnique("Nitrogen (N)", "25 kg/ha", "N/A", 40, "Low"),
        SoilTestUnique("Phosphorus (P)", "15 kg/ha", "N/A", 30, "Low"),
        SoilTestUnique("Potassium (K)", "120 kg/ha", "N/A", 75, "Good")
    )
}

// --- Modified Recommendation Logic ---
fun getCropRecommendationsWithBudget(
    soilHealth: SoilHealth?, soilTests: List<SoilTestUnique>,
    farmerBudget: Double?, farmSizeAcres: Double = 1.0
): List<RecommendedCrop> {
    val recommendations = mutableListOf<RecommendedCrop>()
    fun extractNumeric(value: String?): Double? = value?.filter { it.isDigit() || it == '.' }?.toDoubleOrNull()
    val ph = extractNumeric(soilHealth?.phLevel)
    val organicMatterPercent = extractNumeric(soilHealth?.organicMatter)
    fun findStatus(paramName: String): String = soilTests.find { it.parameter.contains(paramName, ignoreCase = true) }?.status ?: "Unknown"

    val nitrogenStatus = findStatus("Nitrogen")
    val potassiumStatus = findStatus("Potassium")

    val potentialCrops = listOf("Tomato", "Potato", "Lettuce", "Maize", "Wheat") // Define crops to evaluate

    potentialCrops.forEach { cropName ->
        var score = 50 // Base score for each crop
        val reasons = mutableListOf<String>()
        var notes: String? = null

        // Apply rules based on cropName (this is highly simplified)
        when (cropName) {
            "Tomato" -> {
                notes = "Needs good drainage. Likes well-drained, fertile soil."
                if (ph != null) {
                    if (ph in 6.0..6.8) { score += 20; reasons.add("Ideal pH (${String.format("%.1f", ph)})") }
                    else if (ph < 6.0) { score -= 10; reasons.add("Acidic (pH ${String.format("%.1f", ph)}), may need lime") }
                    else { score -= 10; reasons.add("Alkaline (pH ${String.format("%.1f", ph)})") }
                }
                if (organicMatterPercent != null && organicMatterPercent < 1.5) { score -= 10; reasons.add("Low OM (${String.format("%.1f", organicMatterPercent)}%)") }
                else if (organicMatterPercent != null) { score += 5; reasons.add("Adequate OM (${String.format("%.1f", organicMatterPercent)}%)") }
                if (potassiumStatus.equals("Good", ignoreCase = true)) { score += 10; reasons.add("Good K levels") }
            }
            "Potato" -> {
                notes = "Susceptible to scab in high pH. Prefers loamy, well-drained soil."
                if (ph != null) {
                    if (ph in 5.0..6.0) { score += 20; reasons.add("Prefers acidic soil (pH ${String.format("%.1f", ph)})") }
                    else { score -= 15; reasons.add("pH (${String.format("%.1f", ph)}) outside ideal (5.0-6.0)") }
                }
                if (nitrogenStatus.equals("Low", ignoreCase = true)) { score -= 10; reasons.add("Low N, needs N fertilizer") }
                if (potassiumStatus.equals("Good", ignoreCase = true)) { score += 15; reasons.add("High K demand met") }
            }
            "Lettuce" -> {
                notes = "Cool weather crop. Prefers rich, loose soil."
                if (ph != null) {
                    if (ph in 6.0..7.0) { score += 15; reasons.add("Good pH range (${String.format("%.1f",ph)})") }
                    else { score -=5; reasons.add("pH (${String.format("%.1f",ph)}) slightly off ideal (6.0-7.0)") }
                }
                if (nitrogenStatus.equals("Low", ignoreCase = true)) { score -= 10; reasons.add("Low Nitrogen, supplement needed") }
                else { score += 10; reasons.add("Adequate Nitrogen") }
            }
            // Add rules for Maize, Wheat, etc.
        }

        val costFactors = cropCostDatabase[cropName]
        val estimatedTotalCostForCrop = costFactors?.totalEstimatedCostPerAcre?.times(farmSizeAcres)
        var isWithinBudget: Boolean? = null

        if (farmerBudget != null && estimatedTotalCostForCrop != null) {
            isWithinBudget = estimatedTotalCostForCrop <= farmerBudget
            if (!isWithinBudget) {
                score -= 20 // Penalize if over budget
                reasons.add("May exceed budget (Est. ₹${"%.0f".format(estimatedTotalCostForCrop)} for ${farmSizeAcres}ac)")
            } else {
                reasons.add("Fits budget (Est. ₹${"%.0f".format(estimatedTotalCostForCrop)} for ${farmSizeAcres}ac)")
            }
        } else if (farmerBudget != null && costFactors == null) {
            reasons.add("Cost data unavailable to check budget.")
        }


        if (score >= 40) { // Only add if reasonably suitable
            recommendations.add(
                RecommendedCrop(
                    cropName, score.coerceIn(0, 100), reasons, notes,
                    costFactors?.totalEstimatedCostPerAcre, // Store per acre cost
                    isWithinBudget
                )
            )
        }
    }

    return recommendations.sortedWith(
        compareByDescending<RecommendedCrop> { it.withinBudget == true }
            .thenByDescending { it.suitabilityScore }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropRecommendationScreen(navController: NavController) {
    val soilHealth: SoilHealth? = remember { getCurrentSoilHealthForCropRec() }
    val soilTestData: List<SoilTestUnique> = remember { getCurrentSoilTestDataForCropRec() }

    var budgetInput by remember { mutableStateOf("") }
    val farmerBudget = budgetInput.toDoubleOrNull()

    var farmSizeInput by remember { mutableStateOf("1.0") }
    val farmSizeAcres = farmSizeInput.toDoubleOrNull() ?: 1.0

    val recommendations by remember(soilHealth, soilTestData, farmerBudget, farmSizeAcres) {
        derivedStateOf {
            getCropRecommendationsWithBudget(soilHealth, soilTestData, farmerBudget, farmSizeAcres)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🌱 Crop Recommendations") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                CurrentSoilSummaryCard(soilHealth, soilTestData)
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Your Inputs for Recommendation",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Enter Your Budget (e.g., ₹50000)") },
                        leadingIcon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Budget") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = farmSizeInput,
                        onValueChange = { farmSizeInput = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Enter Farm Size (Acres)") },
                        leadingIcon = { Icon(Icons.Filled.SquareFoot, contentDescription = "Farm Size") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (recommendations.isNotEmpty()) {
                item {
                    Text(
                        "Recommended Crops (Soil & Budget):",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(recommendations) { crop ->
                    RecommendedCropCard(crop = crop)
                }
            } else if (farmerBudget != null || soilHealth != null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                        Text(
                            "No suitable crop recommendations found with the current soil data, budget, and farm size. " +
                                    "Please ensure soil tests are up-to-date and inputs are realistic.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            } else {
                item {
                    Text(
                        "Please enter your budget and ensure soil data is available to get recommendations.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CurrentSoilSummaryCard(soilHealth: SoilHealth?, soilTests: List<SoilTestUnique>) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(16.dp).fillMaxWidth()) {
            Text(
                "Current Soil Conditions Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            soilHealth?.let {
                DetailRow("pH Level:", it.phLevel)
                DetailRow("Organic Matter:", it.organicMatter)
            }
            val displayedParams = mutableSetOf<String>()
            if (soilHealth?.phLevel != null) displayedParams.add("ph")
            if (soilHealth?.organicMatter != null) displayedParams.add("organic")

            soilTests.forEach { test ->
                val paramNameLower = test.parameter.lowercase()
                if ((paramNameLower.contains("nitrogen") ||
                            paramNameLower.contains("phosphorus") ||
                            paramNameLower.contains("potassium")) ||
                    (paramNameLower.contains("organic carbon") && !displayedParams.contains("organic")) ||
                    (paramNameLower.contains("ph") && !displayedParams.contains("ph"))
                ) {
                    DetailRow("${test.parameter}:", "${test.value} (${test.status})")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(4.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun RecommendedCropCard(crop: RecommendedCrop) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = when {
                crop.withinBudget == true && crop.suitabilityScore >= 75 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                crop.withinBudget == true && crop.suitabilityScore >= 55 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                crop.withinBudget == false -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Yard,
                    contentDescription = "Recommended Crop",
                    tint = if (crop.withinBudget == false) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(crop.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { crop.suitabilityScore / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(MaterialTheme.shapes.small),
                color = when {
                    crop.suitabilityScore >= 75 -> MaterialTheme.colorScheme.primary
                    crop.suitabilityScore >= 55 -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.tertiary
                },
                trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            )
            Text(
                "Soil Suitability: ${crop.suitabilityScore}%",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 6.dp)
            )

            crop.estimatedCostPerAcre?.let { costPerAcre ->
                Text(
                    "Est. Cost: ₹${"%.0f".format(costPerAcre)} / acre",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (crop.withinBudget == false) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
            if (crop.withinBudget == false) {
                Text(
                    "Exceeds your current budget input for the farm size.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold
                )
            }

            if (crop.reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Reasons:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                crop.reasons.forEach { reason ->
                    Row(modifier = Modifier.padding(start = 8.dp, top = 4.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Info, null, modifier = Modifier.size(16.dp).padding(end = 6.dp, top = 2.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(reason, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            crop.notes?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Notes:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CropRecommendationScreenPreview() {
    MaterialTheme { CropRecommendationScreen(navController = rememberNavController()) }
}

@Preview(showBackground = true)
@Composable
fun RecommendedCropCardPreview() {
    MaterialTheme {
        Column(Modifier.padding(16.dp).width(300.dp)) {
            RecommendedCropCard(
                RecommendedCrop("Tomato", 85, listOf("Ideal pH", "Good K levels", "Fits budget"), "Needs good drainage.", 11500.0, true)
            )
            Spacer(Modifier.height(16.dp))
            RecommendedCropCard(
                RecommendedCrop("Potato (High Cost)", 60, listOf("pH off", "Exceeds budget"), "Susceptible to scab.", 15000.0, false)
            )
        }
    }
}
