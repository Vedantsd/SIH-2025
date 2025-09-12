package com.example.smartcropadvisory

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

data class GovtScheme(
    val id: String,
    val name: String,
    val description: String,
    val benefits: List<String>,
    val eligibility: String,
    val websiteUrl: String
)

// ENSURE THIS LIST IS POPULATED WITH DATA
val sampleGovtSchemes = listOf(
    GovtScheme(
        id = "pm_kisan",
        name = "PM-Kisan Samman Nidhi Yojana",
        description = "A central sector scheme with 100% funding from the Government of India. It aims to supplement the financial needs of all landholding farmers' families.",
        benefits = listOf(
            "Income support of ₹6,000 per year in three equal installments.",
            "Direct benefit transfer (DBT) to farmers' bank accounts."
        ),
        eligibility = "All landholder farmer families (subject to certain exclusion criteria).",
        websiteUrl = "https://pmkisan.gov.in/" // REPLACE WITH ACTUAL URL
    ),
    GovtScheme(
        id = "fasal_bima",
        name = "Pradhan Mantri Fasal Bima Yojana (PMFBY)",
        description = "Provides comprehensive insurance coverage against crop loss due to non-preventable natural risks.",
        benefits = listOf(
            "Financial support to farmers suffering crop loss/damage arising out of unforeseen events.",
            "Stabilizing the income of farmers to ensure their continuance in farming.",
            "Encouraging farmers to adopt innovative and modern agricultural practices."
        ),
        eligibility = "All farmers including sharecroppers and tenant farmers growing notified crops in notified areas are eligible for coverage.",
        websiteUrl = "https://pmfby.gov.in/" // REPLACE WITH ACTUAL URL
    ),
    GovtScheme(
        id = "soil_health_card",
        name = "Soil Health Card Scheme",
        description = "A scheme to assist State Governments to issue Soil Health Cards to all farmers in the country. The Soil Health Cards provide information to farmers on nutrient status of their soil along with recommendation on appropriate dosage of nutrients to be applied for improving soil health and its fertility.",
        benefits = listOf(
            "Provides farmers with information on the nutrient status of their soil.",
            "Recommends appropriate dosage of nutrients for better yield.",
            "Helps in reducing the cost of cultivation by applying only necessary nutrients."
        ),
        eligibility = "Available to all farmers.",
        websiteUrl = "https://soilhealth.dac.gov.in/" // REPLACE WITH ACTUAL URL
    ),
    GovtScheme(
        id = "paramparagat_krishi",
        name = "Paramparagat Krishi Vikas Yojana (PKVY)",
        description = "An elaborated component of Soil Health Management (SHM) of major project National Mission of Sustainable Agriculture (NMSA). Under PKVY Organic farming is promoted through adoption of organic village by cluster approach and PGS certification.",
        benefits = listOf(
            "Promotes organic farming and improves soil health.",
            "Provides financial assistance to farmers for organic inputs, certification, marketing, etc.",
            "Aims to produce agricultural products free from chemicals and pesticides."
        ),
        eligibility = "Farmers interested in organic farming, typically in clusters.",
        websiteUrl = "https://pgsindia-ncof.gov.in/pkvy/index.html" // REPLACE WITH ACTUAL URL
    )
    // Add more schemes here if needed
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GovtSchemesScreen(navController: NavController) {
    val context = LocalContext.current
    Log.d("GovtSchemesScreen", "Displaying ${sampleGovtSchemes.size} schemes.") // For debugging

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("📜 Government Schemes", fontWeight = FontWeight.SemiBold) },
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
        if (sampleGovtSchemes.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No government schemes available at the moment.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sampleGovtSchemes) { scheme ->
                    SchemeCard(scheme = scheme, context = context)
                }
            }
        }
    }
}

@Composable
fun SchemeCard(scheme: GovtScheme, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = scheme.name,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = scheme.description,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Key Benefits:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            scheme.benefits.forEach { benefit ->
                Text("• $benefit", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp, top = 2.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("Eligibility:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(scheme.eligibility, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scheme.websiteUrl))
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("GovtSchemesScreen", "Error opening URL ${scheme.websiteUrl}: ${e.message}")
                        // Optionally, show a Toast to the user:
                        // Toast.makeText(context, "Could not open link. No browser found.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Learn More")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Filled.Launch, contentDescription = "Open Link", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GovtSchemesScreenPreview() {
    MaterialTheme {
        GovtSchemesScreen(navController = rememberNavController())
    }
}
