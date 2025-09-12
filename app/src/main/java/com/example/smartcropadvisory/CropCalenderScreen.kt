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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDate // For more robust date handling
import java.time.format.DateTimeFormatter
import java.util.Locale

// ----------------- Data Model (Updated) -----------------
data class DatedCropActivity(
    val date: String, // e.g., "2024-01-15" (YYYY-MM-DD for sorting) or a specific LocalDate
    val crop: String,
    val task: String,
    val seeds: String? = null, // Optional fields
    val water: String? = null,
    val fertilizer: String? = null,
    val note: String? = null,
    val equipment: String? = null // Example of another optional field
)

// Helper to format date for display
fun formatDateForDisplay(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH))
    } catch (e: Exception) {
        dateString // Fallback to original string if parsing fails
    }
}


// ----------------- Dummy Data (Updated with specific dates) -----------------
private val datedCropCalendarData = listOf(
    DatedCropActivity(date = "2024-01-10", crop = "Wheat", task = "Soil Preparation", note = "Plow the field and add manure."),
    DatedCropActivity(date = "2024-01-15", crop = "Wheat", task = "Sowing", seeds = "Certified high-yield seeds (Variety XYZ)", water = "Light irrigation post-sowing", fertilizer = "Basal dose NPK 20:10:10", note = "Ensure even seed distribution."),
    DatedCropActivity(date = "2024-02-05", crop = "Wheat", task = "First Irrigation", water = "Critical stage, ensure adequate moisture.", note = "Check for early weed growth."),
    DatedCropActivity(date = "2024-02-20", crop = "Wheat", task = "Weed Control", note = "Manual weeding or apply recommended herbicide."),
    DatedCropActivity(date = "2024-03-01", crop = "Sugarcane", task = "Land Preparation", note = "Deep ploughing for good root development."),
    DatedCropActivity(date = "2024-03-10", crop = "Sugarcane", task = "Planting Setts", seeds = "3-budded setts, treated", water = "Immediate light irrigation", fertilizer = "FYM 5t/ha incorporated", note = "Plant in rows with proper spacing."),
    DatedCropActivity(date = "2024-04-05", crop = "Paddy", task = "Nursery Bed Preparation", seeds = "High-yield variety seeds for nursery", note = "Select a well-drained sunny area for nursery."),
    DatedCropActivity(date = "2024-04-15", crop = "Paddy", task = "Sowing in Nursery", water = "Keep nursery beds moist.", fertilizer = "FYM 1t/ha for nursery"),
    DatedCropActivity(date = "2024-05-10", crop = "Paddy", task = "Main Field Puddling", note = "Puddle field thoroughly before transplanting."),
    DatedCropActivity(date = "2024-05-20", crop = "Paddy", task = "Transplanting Seedlings", water = "Maintain 2-3 cm water level", fertilizer = "NPK 15:15:15 as basal dose", note = "Transplant 2-3 seedlings per hill."),
    DatedCropActivity(date = "2024-07-01", crop = "Maize", task = "Sowing", seeds = "Hybrid variety", water = "Sow after first monsoon rains.", fertilizer = "Basal NPK"),
    DatedCropActivity(date = "2024-07-20", crop = "Maize", task = "Weeding & Thinning", note = "Remove weeds and extra plants for healthy growth."),
    DatedCropActivity(date = "2024-09-01", crop = "Cotton", task = "Sowing", seeds = "Bt cotton seeds", water = "Ensure good soil moisture"),
    DatedCropActivity(date = "2024-09-25", crop = "Cotton", task = "Fertilizer Application (Top Dressing)", fertilizer = "Urea application", note = "Split application of Nitrogen."),
    DatedCropActivity(date = "2024-11-15", crop = "Groundnut", task = "Harvesting", note = "Check for pod maturity. Harvest when leaves start to yellow and dry.")
).sortedBy { it.date } // Sort by date

// ----------------- Crop Calendar Screen (Updated) -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropCalendarScreen(navController: NavController) {
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() } // Use item ID (date+crop+task) for stable keys

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("📅 Crop Activity Calendar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Date-wise farming tasks for optimal crop management.",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Example: Group by Crop (Optional, can also just list chronologically)
                // For this example, we'll list chronologically.
                // If you want to group by crop, you would do:
                // val groupedByCrop = datedCropCalendarData.groupBy { it.crop }
                // groupedByCrop.forEach { (crop, activities) ->
                //    item { Text(crop, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 8.dp)) }
                //    items(activities, key = { it.date + it.task }) { activity -> ... }
                // }

                items(datedCropCalendarData.size, key = { index ->
                    // Create a more unique key if data can change frequently
                    datedCropCalendarData[index].date + datedCropCalendarData[index].crop + datedCropCalendarData[index].task
                }) { index ->
                    val item = datedCropCalendarData[index]
                    val itemKey = item.date + item.crop + item.task // Unique key for expanded state
                    val isExpanded = expandedStates[itemKey] ?: false

                    ActivityCard(
                        item = item,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedStates[itemKey] = !isExpanded
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityCard(
    item: DatedCropActivity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, hoveredElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        formatDateForDisplay(item.date), // Display formatted date
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        item.crop,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand/Collapse",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                "🔨 Task: ${item.task}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 3 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 3 })
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    item.seeds?.let { ActivityDetailRow(icon = Icons.Filled.Nature, label = "Seeds", value = it) }
                    item.water?.let { ActivityDetailRow(icon = Icons.Filled.WaterDrop, label = "Water", value = it) }
                    item.fertilizer?.let { ActivityDetailRow(icon = Icons.Filled.Science, label = "Fertilizer", value = it) }
                    item.equipment?.let { ActivityDetailRow(icon = Icons.Filled.Build, label = "Equipment", value = it) }
                    item.note?.let {
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        ActivityDetailRow(icon = Icons.Filled.Info, label = "Note", value = it, isNote = true)
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, isNote: Boolean = false) {
    Row(
        verticalAlignment = if (isNote) Alignment.Top else Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
        )
    }
}
