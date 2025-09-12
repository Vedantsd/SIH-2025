@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.smartcropadvisory

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlaceholderScreen(title: String) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(title) })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$title (UI Only)",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
