package com.example.smartcropadvisory

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartcropadvisory.models.FarmerProfile // CORRECT IMPORT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var landSize by remember { mutableStateOf("") }
    var cropType by remember { mutableStateOf("") }
    // Add other fields as needed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Farmer Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = landSize, onValueChange = { landSize = it }, label = { Text("Land Size (e.g., 5 Acres)") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = cropType, onValueChange = { cropType = it }, label = { Text("Main Crop Type") })
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Perform registration logic
                val newFarmer = FarmerProfile( // Create FarmerProfile instance
                    name = name,
                    email = email,
                    state = state,
                    district = district,
                    landSize = landSize,
                    cropType = cropType
                )
                // TODO: Save the newFarmer data (e.g., to a database, API)
                // TODO: Handle password securely (hash it before saving)
                // For now, just navigate to home or login
                navController.navigate("home") { // Or login
                    popUpTo("login") { inclusive = true } // Example: Clear back stack up to login
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
    }
}
