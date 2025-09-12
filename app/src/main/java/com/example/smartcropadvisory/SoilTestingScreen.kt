package com.example.smartcropadvisory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Ensure this specific import for LazyColumn items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smartcropadvisory.models.SoilTestUnique // IMPORT THE CENTRAL MODEL

// Sample data using the imported SoilTestUnique model
private val uniqueSoilTestData: List<SoilTestUnique> = listOf(
    SoilTestUnique("pH Level", "6.3", "Ideal for cereals. Maintain balance.", 80, "Good"),
    SoilTestUnique("Organic Carbon", "0.75%", "Add compost/FYM.", 40, "Low"),
    SoilTestUnique("Nitrogen (N)", "38 kg/ha", "Adequate, balanced dose required.", 70, "Good"),
    SoilTestUnique("Phosphorus (P)", "20 kg/ha", "Apply SSP @ 100kg/acre.", 45, "Low"),
    SoilTestUnique("Potassium (K)", "60 kg/ha", "Boosts disease resistance.", 85, "Good"),
    SoilTestUnique("Zinc (Zn)", "0.7 ppm", "Sufficient. Monitor crop for specific needs.", 65, "Good")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilTestingScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🧪 Soil Testing Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* TODO: Request Lab Test */ },
                icon = { Icon(Icons.Default.Biotech, contentDescription = "Book Test") },
                text = { Text("Book Lab Test") },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                        )
                    )
                )
        ) {
            HeaderSection()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp) // Padding for FAB
            ) {
                items(uniqueSoilTestData) { item ->
                    SoilParameterCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Science,
            contentDescription = "Soil Test Summary",
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Soil Test Insights",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Location: Central Farm | Last Test: Oct 2023",
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SoilParameterCard(item: SoilTestUnique) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, hoveredElevation = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.parameter,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusBadge(statusText = item.status) // Removed percentage if not directly used in badge
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Value: ${item.value}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { item.percentage / 100f }, // Corrected lambda for progress
                color = getStatusColor(item.status),
                trackColor = getStatusColor(item.status).copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Recommendation",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    item.recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusBadge(statusText: String) { // Removed percentage parameter if not used
    val color = getStatusColor(statusText)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.9f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            statusText,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "low" -> MaterialTheme.colorScheme.error
        "good", "adequate", "sufficient" -> MaterialTheme.colorScheme.primary
        "high", "excess" -> Color(0xFFFFA000) // Amber for high/excess
        "medium" -> MaterialTheme.colorScheme.secondary // Example for medium
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Default
    }
}

@Preview(showBackground = true)
@Composable
fun SoilTestingScreenPreview() {
    MaterialTheme { // Replace with your app's theme if different
        SoilTestingScreen(navController = rememberNavController())
    }
}
