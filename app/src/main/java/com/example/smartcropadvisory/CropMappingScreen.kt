package com.example.smartcropadvisory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate // New Icon
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SquareFoot // Icon for land size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropMappingScreen(navController: NavController) {
    var landSizeInput by remember { mutableStateOf("") }
    var showDetails by remember { mutableStateOf(false) }
    var calculatedPlantableCrops by remember { mutableStateOf("0") }

    // Basic dummy calculation: e.g., 50 crops per unit of land size
    val cropsPerUnitArea = 50
    val decimalFormat = DecimalFormat("#,###")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🌾 Land-based Planting Guide") } // Title changed
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Icon(
                imageVector = Icons.Filled.SquareFoot, // Changed Icon
                contentDescription = "Land Size Icon",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                "Planting Density & Guidelines", // Title changed
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            if (!showDetails) {
                Text(
                    "Enter your land size to get an estimate of plantable crops and general planting advice.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                OutlinedTextField(
                    value = landSizeInput,
                    onValueChange = { landSizeInput = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Enter Land Size (e.g., in Acres)") },
                    leadingIcon = { Icon(Icons.Filled.SquareFoot, contentDescription = "Land Size") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                Button(
                    onClick = {
                        val size = landSizeInput.toDoubleOrNull()
                        if (size != null && size > 0) {
                            calculatedPlantableCrops = decimalFormat.format(size * cropsPerUnitArea)
                            showDetails = true
                        } else {
                            // Optionally, show an error to the user if input is invalid
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    enabled = landSizeInput.isNotBlank()
                ) {
                    Icon(Icons.Filled.Calculate, contentDescription = "Calculate Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Get Planting Advice")
                }
            } else {
                // Display results and advice in a Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "📊 Planting Estimates for ${landSizeInput} units of land:", // Dynamic text
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        HighlightInfoRow(label = "Estimated Plantable Crops:", value = "Approx. $calculatedPlantableCrops units")

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            "🌱 General Planting & Spacing Guidelines",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        AdviceText(
                            "Crop spacing is crucial for optimal growth, sunlight exposure, air circulation, and nutrient uptake. Recommended distances vary greatly depending on the crop type (e.g., vegetables, fruit trees, grains) and local conditions."
                        )
                        AdviceText(
                            "Example: Tomatoes might need 18-36 inches between plants, while corn might be planted 8-12 inches apart in rows 30-36 inches apart. This translates to roughly X plants per acre for tomatoes and Y plants per acre for corn, assuming standard row spacing."
                        )
                        AdviceText(
                            "Always refer to specific guidelines for the exact crops you intend to plant and adjust based on your land's fertility and resources."
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            "🌿 Mixed Cropping Considerations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        AdviceText(
                            "Mixed cropping (intercropping) involves growing two or more crops in proximity. This can improve soil health, biodiversity, and pest control, and provide diversified income."
                        )
                        AdviceText(
                            "Key considerations for mixed cropping:"
                        )
                        BulletPoint(text = "Companion Plants: Choose crops that benefit each other (e.g., legumes fixing nitrogen for neighboring plants).")
                        BulletPoint(text = "Resource Competition: Ensure crops don't excessively compete for light, water, and nutrients. Consider different rooting depths and canopy structures.")
                        BulletPoint(text = "Pest Management: Some plant combinations can deter pests or attract beneficial insects.")
                        BulletPoint(text = "Timing & Harvest: Plan planting and harvesting times to avoid interference.")
                        AdviceText(
                            "Example: Planting basil with tomatoes can deter certain pests. Legumes (beans, peas) can be intercropped with cereals (maize, sorghum)."
                        )
                        AdviceText(
                            "Consider the overall planting density when mixing crops. The total number of plants might be higher than monoculture but individual crop densities might be lower."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        showDetails = false
                        landSizeInput = "" // Optionally reset input
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Enter Different Land Size")
                }
            }
        }
    }
}

@Composable
private fun HighlightInfoRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun AdviceText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Justify,
        lineHeight = 20.sp
    )
}

@Composable
private fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
        Text("•  ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Justify,
            lineHeight = 20.sp
        )
    }
}
