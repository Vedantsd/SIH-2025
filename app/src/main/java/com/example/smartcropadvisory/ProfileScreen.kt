package com.example.smartcropadvisory

import android.util.Log // << ENSURE THIS IMPORT IS PRESENT
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Business // For Vendor Icon
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person // For Farmer Icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign // For TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.smartcropadvisory.models.FarmerProfile
// import com.example.smartcropadvisory.models.VendorProfile // If you create this
import com.example.smartcropadvisory.viewmodels.AuthViewModel
import com.example.smartcropadvisory.viewmodels.AuthViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    farmerProfile: FarmerProfile, // Passed for Farmer role, can be ignored by Vendor
    userRole: UserRole,          // Crucial for conditional UI, THIS IS PASSED FROM NAVIGATION.KT
    authViewModel: AuthViewModel   // For user data and logout
) {
    // Log the received userRole when this Composable enters composition
    Log.d("ProfileScreen", "ProfileScreen recomposing. Received userRole = $userRole")
    Log.d("ProfileScreen", "AuthViewModel userRole state: ${authViewModel.userRole.collectAsState().value}")


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (userRole) {
                            UserRole.FARMER -> stringResource(R.string.profile_title_farmer)
                            UserRole.VENDOR -> stringResource(R.string.profile_title_vendor)
                            UserRole.UNKNOWN -> "Profile" // Fallback
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Role-specific icon
            Icon(
                imageVector = when (userRole) {
                    UserRole.FARMER -> Icons.Filled.Person
                    UserRole.VENDOR -> Icons.Filled.Business
                    UserRole.UNKNOWN -> Icons.Filled.AccountCircle // Fallback
                },
                contentDescription = "Profile Icon",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Common User Info from AuthViewModel
            val currentLoggedInUserRoleObserved by authViewModel.userRole.collectAsState()
            val currentUserIdObserved by authViewModel.userId.collectAsState()

            if (currentUserIdObserved != null) {
                Text(
                    text = "Role (from VM): ${currentLoggedInUserRoleObserved.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "User ID (from VM): $currentUserIdObserved",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text("User not identified by ViewModel.", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Role-Specific Details Section
            // This 'when' uses the userRole parameter PASSED INTO ProfileScreen
            when (userRole) {
                UserRole.FARMER -> {
                    Log.d("ProfileScreen", "Conditional UI: Displaying FarmerDetailsSection for role: $userRole")
                    FarmerDetailsSection(farmerProfile = farmerProfile)
                }
                UserRole.VENDOR -> {
                    Log.d("ProfileScreen", "Conditional UI: Displaying VendorDetailsSection for role: $userRole")
                    VendorDetailsSection(authViewModel = authViewModel) // Pass ViewModel for vendor-specific data
                }
                UserRole.UNKNOWN -> {
                    Log.d("ProfileScreen", "Conditional UI: Displaying Unknown user info for role: $userRole")
                    Text(
                        "Profile information is unavailable for the current user (Role: UNKNOWN).",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Pushes Logout to bottom

            // Logout Button - Common for all logged-in roles
            // This 'if' also uses the userRole parameter PASSED INTO ProfileScreen
            if (userRole != UserRole.UNKNOWN) {
                Log.d("ProfileScreen", "Conditional UI: Logout button IS visible for role: $userRole")
                Button(
                    onClick = {
                        Log.d("ProfileScreen", "Logout button clicked.")
                        authViewModel.logoutUser()
                        navController.navigate(ScreenRoutes.RoleSelection.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        Icons.Filled.ExitToApp,
                        contentDescription = stringResource(R.string.button_logout),
                        tint = MaterialTheme.colorScheme.onError
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.button_logout),
                        color = MaterialTheme.colorScheme.onError,
                        fontSize = 16.sp
                    )
                }
            } else {
                Log.d("ProfileScreen", "Conditional UI: Logout button is NOT visible for role: $userRole")
            }
            Spacer(modifier = Modifier.height(16.dp)) // Padding at the very bottom
        }
    }
}

@Composable
fun FarmerDetailsSection(farmerProfile: FarmerProfile) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Farmer Information", // TODO: Use stringResource
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ProfileInfoRow(label = "Name:", value = farmerProfile.name)
        ProfileInfoRow(label = "Email:", value = farmerProfile.email)
        ProfileInfoRow(label = "State:", value = farmerProfile.state)
        ProfileInfoRow(label = "District:", value = farmerProfile.district)
        ProfileInfoRow(label = "Land Size:", value = farmerProfile.landSize)
        ProfileInfoRow(label = "Main Crop:", value = farmerProfile.cropType)
    }
}

@Composable
fun VendorDetailsSection(authViewModel: AuthViewModel) { // Added authViewModel parameter
    // Example: Collect vendor-specific data if you store it in AuthViewModel
    // val businessName by authViewModel.vendorBusinessName.collectAsState() // Hypothetical
    // val gstNumber by authViewModel.vendorGstNumber.collectAsState()       // Hypothetical

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Vendor Information (Displayed!)", // Added (Displayed!) for clear confirmation
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // TODO: Replace with actual vendor data fetching and display
        ProfileInfoRow(label = "Business Name:", value = /* businessName ?: */ "Vendor AgriPro Supplies")
        ProfileInfoRow(label = "Contact Email:", value = /* authViewModel.userEmail.collectAsState().value ?: */ "vendor@example.com")
        ProfileInfoRow(label = "GST Number:", value = /* gstNumber ?: */ "VENDOR_GST_XYZ123")
        ProfileInfoRow(label = "Location:", value = "Central Market Hub - Vendor")
        ProfileInfoRow(label = "Years Active:", value = "5 Years - Vendor")
        Text("This confirms VendorDetailsSection is rendering.", color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, name = "Farmer Profile Preview")
@Composable
fun FarmerProfileScreenPreview() {
    val context = LocalContext.current
    val mockAuthViewModel = AuthViewModelFactory(context.applicationContext).create(AuthViewModel::class.java)
    MaterialTheme {
        ProfileScreen(
            navController = rememberNavController(),
            farmerProfile = FarmerProfile("P. Farmer", "p.farmer@mail.com", "StateX", "DistY", "10Ac", "Corn"),
            userRole = UserRole.FARMER,
            authViewModel = mockAuthViewModel
        )
    }
}

@Preview(showBackground = true, name = "Vendor Profile Preview")
@Composable
fun VendorProfileScreenPreview() {
    val context = LocalContext.current
    val mockAuthViewModel = AuthViewModelFactory(context.applicationContext).create(AuthViewModel::class.java)
    MaterialTheme {
        ProfileScreen(
            navController = rememberNavController(),
            farmerProfile = FarmerProfile("", "", "", "", "", ""), // Dummy for vendor
            userRole = UserRole.VENDOR, // Explicitly VENDOR for this preview
            authViewModel = mockAuthViewModel
        )
    }
}
