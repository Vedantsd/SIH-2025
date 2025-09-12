package com.example.smartcropadvisory

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// SoilHealth data class is now in Models.kt

private val soilHealthScreenData = listOf(
    SoilHealth("Field A", "High (85%)", "Good (70%)", "2.5%", "6.8 (Neutral)", "Suitable for wheat and maize."),
    SoilHealth("Field B", "Medium (60%)", "Moderate (55%)", "1.8% (Slightly Low)", "7.2 (Slightly Alkaline)", "Can grow paddy with irrigation. Consider adding organic matter."),
    SoilHealth("Field C", "Low (40%)", "Poor (35%)", "1.2% (Low)", "5.9 (Acidic)", "Soil needs significant organic amendments and pH correction.")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilHealthScreen(navController: NavController) {
    val expandedStates = remember { mutableStateListOf(*Array(soilHealthScreenData.size) { false }) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🌱 Soil Health Overview", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Use theme background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    )
                    .padding(vertical = 20.dp, horizontal = 16.dp), // Increased padding
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Monitor soil fertility, water capacity, pH, organic matter, and important notes for your fields.",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White), // Slightly larger text
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                itemsIndexed(soilHealthScreenData) { index, item ->
                    SoilHealthInfoCard(
                        soilHealthItem = item,
                        expanded = expandedStates[index],
                        onToggleExpand = { expandedStates[index] = !expandedStates[index] }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Padding at the bottom
        }
    }
}

@Composable
private fun SoilHealthInfoCard(
    soilHealthItem: SoilHealth,
    expanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand, role = androidx.compose.ui.semantics.Role.Button),
        shape = RoundedCornerShape(12.dp), // Consistent rounding
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, hoveredElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp) // Increased spacing
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "📍 ${soilHealthItem.location}",
                    style = MaterialTheme.typography.titleLarge, // Larger title
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = slideInVertically(initialOffsetY = { -it / 3 }) + fadeIn(initialAlpha = 0.3f),
                exit = slideOutVertically(targetOffsetY = { -it / 3 }) + fadeOut(targetAlpha = 0.3f)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    DetailRow(icon = Icons.Filled.Eco, label = "Fertility:", value = soilHealthItem.fertility)
                    DetailRow(icon = Icons.Filled.Opacity, label = "Water Capacity:", value = soilHealthItem.waterHoldingCapacity) // Changed Icon
                    DetailRow(icon = Icons.Filled.Grass, label = "Organic Matter:", value = soilHealthItem.organicMatter)
                    DetailRow(icon = Icons.Filled.Science, label = "pH Level:", value = soilHealthItem.phLevel)
                    Divider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    DetailRow(icon = Icons.Filled.SpeakerNotes, label = "Note:", value = soilHealthItem.note, isNote = true) // Changed Icon
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String, isNote: Boolean = false) {
    Row(verticalAlignment = if (isNote) Alignment.Top else Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(22.dp) // Slightly larger icon
        )
        Spacer(Modifier.width(10.dp)) // Increased spacing
        Text(
            "$label ",
            style = MaterialTheme.typography.bodyLarge, // Consistent text style
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = if (isNote) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge, // Adjusted note style
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
        )
    }
}
