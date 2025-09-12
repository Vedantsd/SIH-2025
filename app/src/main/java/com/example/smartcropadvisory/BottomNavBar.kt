// In BottomNavBar.kt
package com.example.smartcropadvisory

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Ensure all needed icons are here
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

// Data class for Bottom Navigation items
data class BottomNavItem(val title: String, val route: String, val icon: ImageVector, val contentDescription: String)

@Composable
fun BottomNavBar(
    navController: NavHostController,
    currentRoute: String?,
    userRole: UserRole // NEW: To determine which items to show
) {
    val items = remember(userRole) { // Rebuild items list if userRole changes
        when (userRole) {
            UserRole.FARMER -> listOf(
                BottomNavItem("Home", ScreenRoutes.FarmerHome.route, Icons.Default.Home, "Farmer Home"),
                BottomNavItem("Sell Crops", ScreenRoutes.FarmerSellCrops.route, Icons.Default.Agriculture, "Sell Your Crops"),
                BottomNavItem("Buy Inputs", ScreenRoutes.FarmerBuyInputs.route, Icons.Default.ShoppingCart, "Buy Agri Inputs"),
                BottomNavItem("Chatbot", ScreenRoutes.Chatbot.route, Icons.Default.Chat, "AI Chatbot"),
                BottomNavItem("Profile", ScreenRoutes.Profile.route, Icons.Default.AccountCircle, "My Profile")
            )
            UserRole.VENDOR -> listOf(
                // For VendorDashboard, the tabs are internal. The bottom nav might be simpler or different.
                // This example assumes VendorDashboard has its own internal tab navigation.
                // So, the bottom nav might primarily be for Profile or other top-level vendor sections if any.
                BottomNavItem("Dashboard", ScreenRoutes.VendorDashboard.route, Icons.Default.Dashboard, "Vendor Dashboard"),
                // Add more vendor-specific top-level navigation if needed, e.g., Analytics, Settings
                BottomNavItem("Profile", ScreenRoutes.Profile.route, Icons.Default.AccountCircle, "Vendor Profile")
            )
            UserRole.UNKNOWN -> emptyList() // No bottom bar if role is unknown or not logged in
        }
    }

    if (items.isNotEmpty()) {
        NavigationBar {
            items.forEach { item ->
                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.contentDescription) },
                    label = { Text(item.title) },
                    selected = currentRoute == item.route ||
                            // For VendorDashboard, make "Dashboard" selected if any of its internal tabs are active (more complex)
                            (userRole == UserRole.VENDOR && item.route == ScreenRoutes.VendorDashboard.route && currentRoute?.startsWith(ScreenRoutes.VendorDashboard.route) == true),
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

