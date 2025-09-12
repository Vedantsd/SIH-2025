package com.example.smartcropadvisory

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IOTScreen(navController: NavController) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Soil Fertility & Water (IoT)") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("📊 Soil Fertility Index: 78%")
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = 0.78f, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Text("💧 Water Content: 62%")
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = 0.62f, modifier = Modifier.fillMaxWidth())
        }
    }
}
