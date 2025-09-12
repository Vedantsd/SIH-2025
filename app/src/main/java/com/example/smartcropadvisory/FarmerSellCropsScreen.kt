package com.example.smartcropadvisory
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerSellCropsScreen(navController: NavController) {
    var cropName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var expectedPrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    // TODO: Add image upload for crop photos

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("List Your Crop for Sale") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Enter Crop Details", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = cropName,
                onValueChange = { cropName = it },
                label = { Text("Crop Name (e.g., Organic Wheat, Fresh Tomatoes)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity (e.g., 50 Quintals, 200 KG)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = expectedPrice,
                onValueChange = { expectedPrice = it },
                label = { Text("Expected Price (e.g., ₹2500 / Quintal)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (e.g., quality, variety, harvest date - optional)") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
            )
            // TODO: Add Image Uploader for crop photos

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    // TODO: Validate input
                    // TODO: Save crop listing to database (associate with current farmer user)
                    // val newListing = FarmerCropListingItem(id = generateId(), cropName = cropName, farmerName = "Current User", quantity = quantity, askingPrice = expectedPrice)
                    Log.d("FarmerSellCrops", "List Crop: $cropName, Qty: $quantity, Price: $expectedPrice")
                    navController.popBackStack() // Go back after listing
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("List My Crop")
            }
        }
    }
}
