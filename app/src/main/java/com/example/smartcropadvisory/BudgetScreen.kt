package com.example.smartcropadvisory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// Dummy crop suggestions based on budget range
private val cropSuggestions = listOf(
    "Below ₹10,000 → Grow Bajra + Pulses (Low-cost, resilient crops).",
    "₹10,000 - ₹25,000 → Maize + Groundnut (Balanced investment).",
    "₹25,000 - ₹50,000 → Cotton + Soybean (High profit margin).",
    "₹50,000+ → Sugarcane + Paddy (High water + investment crops)."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(navController: NavController) {
    var budget by remember { mutableStateOf("") }
    var suggestion by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("💰 Budget-based Crop Selection") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFF8E1), Color(0xFFE8F5E9))
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Input section
            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it },
                label = { Text("Enter your budget (₹)") },
                leadingIcon = {
                    Icon(Icons.Default.AttachMoney, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val value = budget.toIntOrNull() ?: 0
                    suggestion = when {
                        value < 10000 -> cropSuggestions[0]
                        value in 10000..25000 -> cropSuggestions[1]
                        value in 25001..50000 -> cropSuggestions[2]
                        else -> cropSuggestions[3]
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Agriculture, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Get Suggestion")
            }

            Spacer(Modifier.height(20.dp))

            // Suggestion card
            if (suggestion.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            Icons.Default.Spa,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Suggested Crops", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(suggestion, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
