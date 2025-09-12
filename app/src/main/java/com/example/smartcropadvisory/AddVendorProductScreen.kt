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
fun AddVendorProductScreen(navController: NavController) {
    var productName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    // TODO: Add category dropdown, image upload

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add/Edit Product") },
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
            Text("Enter Product Details", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name (e.g., Urea Fertilizer, Neem Pesticide)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (e.g., ₹700 / bag)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("Available Stock (units)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Product Description (optional)") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
            )
            // TODO: Add Dropdown for Category (Fertilizer, Pesticide, Seed, Tool)
            // TODO: Add Image Uploader

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    // TODO: Validate input
                    // TODO: Save product to database (associate with current vendor user)
                    // val newProduct = VendorProductItem(id = generateId(), name = productName, price = price, stock = stock.toIntOrNull() ?: 0)
                    Log.d("AddVendorProduct", "Save: $productName, Price: $price, Stock: $stock")
                    navController.popBackStack() // Go back after saving
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Product")
            }
        }
    }
}
