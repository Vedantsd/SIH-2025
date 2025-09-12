package com.example.smartcropadvisory

import android.util.Log // <<< ADD THIS IMPORT
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// --- Data Models ---
data class BuyableProduct(
    val id: String,
    val name: String,
    val price: String,
    val category: String, // e.g., "Seeds", "Fertilizers", "Pesticides"
    val emoji: String = "🛍️"
)

data class SellableCrop(
    val id: String,
    val name: String,
    val quantity: String, // e.g., "10 Quintals", "500 kg"
    val expectedPrice: String, // e.g., "₹2200 / Quintal"
    val farmerName: String = "Local Farmer", // Could be dynamic
    val location: String = "Nearby Farm", // Could be dynamic
    val emoji: String = "🌾"
)

// --- Sample Data ---
val sampleBuyableProducts = listOf(
    BuyableProduct("seed_wheat_01", "🌱 Wheat Seeds (Variety A)", "₹1200 / bag", "Seeds", emoji = "🌱"),
    BuyableProduct("fert_urea_01", "💧 Urea Fertilizer", "₹650 / 50kg", "Fertilizers", emoji = "💧"),
    BuyableProduct("pest_spray_01", "🪲 Organic Pesticide Spray", "₹400 / liter", "Pesticides", emoji = "🪲"),
    BuyableProduct("seed_maize_01", "🌽 Maize Seeds (Hybrid)", "₹1500 / bag", "Seeds", emoji = "🌽"),
    BuyableProduct("fert_dap_01", "💪 DAP Fertilizer", "₹1350 / 50kg", "Fertilizers", emoji = "💪"),
)

val sampleSellableCrops = listOf(
    SellableCrop("sell_wheat_farmerA_01", "Wheat (Premium)", "20 Quintals", "₹2300 / Quintal", farmerName = "Ramesh Kumar", location = "Jaipur Dist.", emoji = "🌾"),
    SellableCrop("sell_tomato_farmerB_01", "Tomatoes (Fresh)", "50 kg", "₹30 / kg", farmerName = "Sunita Devi", location = "Alwar Dist.", emoji = "🍅"),
    SellableCrop("sell_mango_farmerC_01", "Mangoes (Langra)", "5 Quintals", "₹7000 / Quintal", farmerName = "Amit Singh", location = "Kota Dist.", emoji = "🥭"),
)

enum class EcommerceTab {
    BUY, SELL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcommerceScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(EcommerceTab.BUY) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("E-commerce Hub") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(
                    selected = selectedTab == EcommerceTab.BUY,
                    onClick = { selectedTab = EcommerceTab.BUY },
                    text = { Text("Buy Products") },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Buy Products") }
                )
                Tab(
                    selected = selectedTab == EcommerceTab.SELL,
                    onClick = { selectedTab = EcommerceTab.SELL },
                    text = { Text("Sell Your Crop") },
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Sell Your Crop") }
                )
            }

            when (selectedTab) {
                EcommerceTab.BUY -> BuySection(products = sampleBuyableProducts)
                EcommerceTab.SELL -> SellSection(crops = sampleSellableCrops)
            }
        }
    }
}

@Composable
fun BuySection(products: List<BuyableProduct>) {
    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("No products available for purchase at the moment.", textAlign = TextAlign.Center)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Purchase Agricultural Inputs",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(products, key = { it.id }) { product ->
            ProductCard(product = product, onAddToCart = {
                // TODO: Implement Add to Cart logic (e.g., update a ViewModel, show a Snackbar)
                Log.d("EcommerceScreen", "Add to cart: ${product.name}")
            })
        }
    }
}

@Composable
fun ProductCard(product: BuyableProduct, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    "${product.emoji} ${product.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    product.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    product.price,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(onClick = onAddToCart) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = "Add to Cart", modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Add to Cart")
            }
        }
    }
}

@Composable
fun SellSection(crops: List<SellableCrop>) {
    // In a real app, this section would have a form for farmers to list their crops.
    // For now, we'll display a list of already sellable crops.
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    "Sell Your Harvest",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = { /* TODO: Navigate to a form to list new crop for sale */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("List Your Crop for Sale")
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (crops.isNotEmpty()) {
                    Text(
                        "Crops Currently Listed by Farmers:",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        if (crops.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                    Text("No crops currently listed for sale by farmers.", textAlign = TextAlign.Center)
                }
            }
        } else {
            items(crops, key = { it.id }) { crop ->
                SellableCropCard(crop = crop, onContactSeller = {
                    // TODO: Implement contact seller logic (e.g., show details, chat)
                    Log.d("EcommerceScreen", "Contact seller for: ${crop.name}")
                })
            }
        }
    }
}

@Composable
fun SellableCropCard(crop: SellableCrop, onContactSeller: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "${crop.emoji} ${crop.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Quantity: ${crop.quantity}", style = MaterialTheme.typography.bodyMedium)
            Text("Expected Price: ${crop.expectedPrice}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text("Seller: ${crop.farmerName} (${crop.location})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onContactSeller,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Contact Seller")
            }
        }
    }
}

