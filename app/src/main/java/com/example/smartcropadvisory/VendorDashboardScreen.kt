package com.example.smartcropadvisory

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController // Changed from NavHostController for screen composables
import androidx.navigation.NavHostController // For VendorDashboardScreen itself
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Assuming ProfileInfoRow is in the same package or a subpackage like uicomponents
// If it's in com.example.smartcropadvisory.uicomponents.CommonUiElements.kt, then:
// import com.example.smartcropadvisory.uicomponents.ProfileInfoRow
// If it's directly in com.example.smartcropadvisory in another file:
import com.example.smartcropadvisory.ProfileInfoRow // Ensure this path is correct


// --- Data Model for Farmer-Listed Crops (Example) ---
data class FarmerListedCrop(
    val id: String,
    val name: String,
    val quantity: String,
    val expectedPrice: String,
    val farmerName: String,
    val location: String,
    val contactInfo: String?,
    val dateListed: Long,
    val emoji: String = "🌾"
)

// Sample data for demonstration. In a real app, this comes from a database/API.
val sampleFarmerCropsForSale = listOf(
    FarmerListedCrop("crop_wheat_001", "Premium Wheat", "50 Quintals", "₹2350 / Quintal", "Ramesh Kumar", "Jaipur District", "Contact: 98XXXXXX01", System.currentTimeMillis() - 86400000, "🌾"),
    FarmerListedCrop("crop_tomato_002", "Fresh Tomatoes", "200 kg", "₹35 / kg", "Sunita Devi", "Alwar District", "Contact: 99XXXXXX02", System.currentTimeMillis(), "🍅"),
    FarmerListedCrop("crop_mango_003", "Organic Mangoes (Langra)", "10 Quintals", "₹7500 / Quintal", "Amit Singh", "Kota District", "Contact: 97XXXXXX03", System.currentTimeMillis() - 172800000, "🥭"),
    FarmerListedCrop("crop_onion_004", "Red Onions", "100 Quintals", "₹1800 / Quintal", "Geeta Patel", "Sikar District", "Contact: 96XXXXXX04", System.currentTimeMillis(), "🧅")
)


// --- Screen Content Composable Definitions ---

/**
 * Screen for Vendors to list THEIR products (seeds, tools, etc.) for sale.
 */
@Composable
fun VendorSellProductsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "Manage & List Your Products", // TODO: Use stringResource
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = { navController.navigate(ScreenRoutes.AddVendorProduct.route) }) {
            Text("List a New Product") // TODO: Use stringResource
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Your Current Listings:", // TODO: Use stringResource
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        // TODO: Replace with a LazyColumn displaying vendor's actual listed products
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No products listed by you yet.") // TODO: Use stringResource
        }
    }
}

/**
 * Screen for Vendors to buy crops listed BY FARMERS.
 */
@Composable
fun VendorBuyCropsScreen(navController: NavController) {
    val context = LocalContext.current
    // In a real app, fetch this data from a ViewModel connected to your backend
    val cropsForSale by remember { mutableStateOf(sampleFarmerCropsForSale.sortedByDescending { it.dateListed }) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Buy Crops from Farmers", // TODO: Use stringResource
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (cropsForSale.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No crops currently listed by farmers.", // TODO: Use stringResource
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(cropsForSale, key = { it.id }) { crop ->
                    FarmerCropCard(crop = crop, onContactFarmer = {
                        Log.d("VendorBuyCrops", "Attempting to contact farmer for ${crop.name}. Info: ${crop.contactInfo}")
                        Toast.makeText(context, "Contacting farmer for ${crop.name}...", Toast.LENGTH_SHORT).show()
                        // TODO: Implement actual contact/offer logic
                    })
                }
            }
        }
    }
}

@Composable
fun FarmerCropCard(crop: FarmerListedCrop, onContactFarmer: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(crop.emoji, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(crop.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("By: ${crop.farmerName} (${crop.location})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            ProfileInfoRow(label = "Quantity:", value = crop.quantity)
            ProfileInfoRow(label = "Price:", value = crop.expectedPrice)
            crop.contactInfo?.let { ProfileInfoRow(label = "Contact:", value = it) }
            ProfileInfoRow(label = "Listed:", value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(crop.dateListed)))
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onContactFarmer,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Contact Farmer / Make Offer") // TODO: Use stringResource
            }
        }
    }
}

/**
 * Screen for Vendors to manage orders they have received FOR THEIR products.
 */
@Composable
fun VendorOrdersScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Manage Customer Orders", // TODO: Use stringResource
            style = MaterialTheme.typography.headlineSmall
        )
        // TODO: Add UI to display and manage orders received by the vendor.
    }
}


// --- VendorDashboardScreen Definition ---
data class VendorDashboardTabItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val screenContent: @Composable () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(navController: NavHostController) { // Use NavHostController for the main dashboard nav
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        VendorDashboardTabItem(
            title = "Sell Products", // TODO: Use stringResource
            icon = Icons.Default.AddShoppingCart,
            screenContent = { VendorSellProductsScreen(navController) }
        ),
        VendorDashboardTabItem(
            title = "Buy Crops",  // TODO: Use stringResource
            icon = Icons.Default.Storefront,
            screenContent = { VendorBuyCropsScreen(navController) }
        ),
        VendorDashboardTabItem(
            title = "Orders",     // TODO: Use stringResource
            icon = Icons.Default.ShoppingCartCheckout,
            screenContent = { VendorOrdersScreen(navController) }
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Vendor Dashboard", fontWeight = FontWeight.Bold) }, // TODO: Use stringResource
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = {
                        Log.d("VendorDashboard", "Profile icon clicked, navigating to ProfileScreen.")
                        navController.navigate(ScreenRoutes.Profile.route)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Vendor Profile" // TODO: Use stringResource
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, tabItem ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tabItem.title) },
                        icon = { Icon(tabItem.icon, contentDescription = tabItem.title) }
                    )
                }
            }
            // Display content based on selected tab
            tabs[selectedTabIndex].screenContent()
        }
    }
}
