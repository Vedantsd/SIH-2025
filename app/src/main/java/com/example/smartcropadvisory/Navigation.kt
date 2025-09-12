@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.smartcropadvisory

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // <<< ENSURE THIS IMPORT
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider // For Factory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartcropadvisory.models.FarmerProfile
import com.example.smartcropadvisory.viewmodels.AuthViewModel
import com.example.smartcropadvisory.viewmodels.AuthViewModelFactory // <<< ENSURE THIS IMPORT

// ENUM FOR USER ROLE (Keep as is)
enum class UserRole {
    FARMER, VENDOR, UNKNOWN
}

// ScreenRoutes object (Keep as is)
object ScreenRoutes {
    object RoleSelection { const val route = "role_selection_route" }
    object FarmerLogin { const val route = "farmer_login_route" }
    object FarmerRegister { const val route = "farmer_register_route" }
    object FarmerHome { const val route = "farmer_home_route" }
    object FarmerSellCrops { const val route = "farmer_sell_crops_route"}
    object FarmerBuyInputs { const val route = "farmer_buy_inputs_route" }
    object VendorLogin { const val route = "vendor_login_route" }
    object VendorRegister { const val route = "vendor_register_route" }
    object VendorDashboard { const val route = "vendor_dashboard_route" }
    object AddVendorProduct { const val route = "add_vendor_product_route" }
    object SoilHome { const val route = "soil_home_route" }
    object SoilHealth { const val route = "soil_health_detail_route" }
    object SoilTesting { const val route = "soil_testing_detail_route" }
    object IrrigationPlan { const val route = "irrigation_plan_route" }
    object CropRecommendation { const val route = "crop_recommendation_route" }
    object PestDetection { const val route = "pest_detection_route" }
    object DiseaseDiagnosis { const val route = "disease_diagnosis_route" }
    object MarketPrices { const val route = "market_prices_route" }
    object GovtSchemes { const val route = "govt_schemes_route" }
    object FarmBudget { const val route = "farm_budget_route" }
    object CropCalendar { const val route = "crop_calendar_route" }
    object CropMapping { const val route = "crop_mapping_route" }
    object Weather { const val route = "weather_detail_route" }
    object Profile { const val route = "profile_route" }
    object Chatbot { const val route = "chatbot_route" }

    fun getHomeRouteForRole(role: UserRole): String {
        return when (role) {
            UserRole.FARMER -> FarmerHome.route
            UserRole.VENDOR -> VendorDashboard.route
            UserRole.UNKNOWN -> RoleSelection.route
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController
    // authViewModel parameter removed from here; it's instantiated below
) {
    val applicationContext = LocalContext.current.applicationContext
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(applicationContext)
    )

    // In Navigation.kt, around line 88
    val exampleFarmerProfile = FarmerProfile(
        name = "Kisan Mitra", // Or some default
        email = "kisan.mitra@example.com",
        state = "Default State",
        district = "Default District",
        landSize = "0 Acres",
        cropType = "N/A"
    )


    val userRole by authViewModel.userRole.collectAsState()
    val isLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

    // Determine start destination based on login state and role
    val startDestination = remember(userRole, isLoggedIn) {
        Log.d("AppNavHost", "Recalculating startDestination: isLoggedIn=$isLoggedIn, userRole=$userRole")
        if (isLoggedIn) {
            ScreenRoutes.getHomeRouteForRole(userRole)
        } else {
            ScreenRoutes.RoleSelection.route
        }
    }
    Log.d("AppNavHost", "Final startDestination: $startDestination")


    NavHost(
        navController = navController,
        startDestination = startDestination // Use the dynamically determined start destination
    ) {
        composable(ScreenRoutes.RoleSelection.route) {
            RoleSelectionScreen(navController)
        }

        // --- AUTH ROUTES ---
        composable(ScreenRoutes.FarmerLogin.route) {
            LoginScreen(
                userType = "Farmer",
                authViewModel = authViewModel, // Pass the ViewModel
                onLoginSuccess = { // This callback is still useful for direct navigation if preferred
                    navController.navigate(ScreenRoutes.FarmerHome.route) {
                        popUpTo(ScreenRoutes.RoleSelection.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = { navController.navigate(ScreenRoutes.FarmerRegister.route) }
            )
        }
        composable(ScreenRoutes.FarmerRegister.route) {
            RegisterScreen(
                userType = "Farmer",
                authViewModel = authViewModel, // Pass the ViewModel
                onRegistrationSuccess = {
                    // Typically navigate to Login after registration
                    navController.navigate(ScreenRoutes.FarmerLogin.route) {
                        popUpTo(ScreenRoutes.FarmerRegister.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(ScreenRoutes.VendorLogin.route) {
            LoginScreen(
                userType = "Vendor",
                authViewModel = authViewModel, // Pass the ViewModel
                onLoginSuccess = {
                    navController.navigate(ScreenRoutes.VendorDashboard.route) {
                        popUpTo(ScreenRoutes.RoleSelection.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = { navController.navigate(ScreenRoutes.VendorRegister.route) }
            )
        }
        composable(ScreenRoutes.VendorRegister.route) {
            RegisterScreen(
                userType = "Vendor",
                authViewModel = authViewModel, // Pass the ViewModel
                onRegistrationSuccess = {
                    navController.navigate(ScreenRoutes.VendorLogin.route) {
                        popUpTo(ScreenRoutes.VendorRegister.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // --- FARMER ROUTES ---
        composable(ScreenRoutes.FarmerHome.route) {
            HomeScreen(navController, userRole = UserRole.FARMER, authViewModel = authViewModel) // Pass authViewModel
        }
        composable(ScreenRoutes.FarmerSellCrops.route) { FarmerSellCropsScreen(navController) }
        composable(ScreenRoutes.FarmerBuyInputs.route) { GenericFeatureScreenPlaceholder(navController, "Buy Agri Inputs from Vendors") }

        // --- VENDOR ROUTES ---
        composable(ScreenRoutes.VendorDashboard.route) { VendorDashboardScreen(navController) } // Pass authViewModel if needed
        composable(ScreenRoutes.AddVendorProduct.route) { AddVendorProductScreen(navController) }
// In Navigation.kt -> AppNavHost
// ...
        composable(ScreenRoutes.Profile.route) {
            // Collect the userRole from authViewModel AGAIN here to ensure it's the most current
            val currentRoleInProfileScope by authViewModel.userRole.collectAsState()
            Log.d("AppNavHost", "Navigating to ProfileScreen. Current Role from ViewModel: $currentRoleInProfileScope. Role being passed: $userRole")

            ProfileScreen(
                navController = navController,
                farmerProfile = exampleFarmerProfile, // This is okay, ProfileScreen handles it
                userRole = userRole,                  // THIS IS THE CRUCIAL PARAMETER
                authViewModel = authViewModel
            )
        }
// ...
        // --- SHARED/ADVISORY ROUTES ---
        composable(ScreenRoutes.SoilHome.route) { SoilScreen(navController) }
        composable(ScreenRoutes.Weather.route) { WeatherScreen(navController) } // Pass authViewModel if needed
        composable(ScreenRoutes.Profile.route) {
            ProfileScreen(navController, exampleFarmerProfile, userRole, authViewModel = authViewModel) // Pass authViewModel
        }
        composable(ScreenRoutes.IrrigationPlan.route) { IrrigationPlanScreen(navController) }
        composable(ScreenRoutes.CropRecommendation.route) { CropRecommendationScreen(navController) }
        composable(ScreenRoutes.MarketPrices.route) { MarketScreen(navController) }
        composable(ScreenRoutes.GovtSchemes.route) { GovtSchemesScreen(navController) }
        composable(ScreenRoutes.PestDetection.route) { PestScreen(navController) }
        composable(ScreenRoutes.DiseaseDiagnosis.route) { DiseaseDiagnosisScreenPlaceholder(navController) }
        composable(ScreenRoutes.FarmBudget.route) { FarmBudgetScreenPlaceholder(navController) }
        composable(ScreenRoutes.CropCalendar.route) { CropCalendarScreen(navController) }
        composable(ScreenRoutes.CropMapping.route) { CropMappingScreen(navController) }
        composable(ScreenRoutes.SoilHealth.route) { SoilHealthScreen(navController) }
        composable(ScreenRoutes.SoilTesting.route) { SoilTestingScreen(navController) }
        composable(ScreenRoutes.Chatbot.route) { ChatbotScreen(navController = navController) }
    }
}

// --- Modified LoginScreen (still within Navigation.kt) ---
// ... (imports, ScreenRoutes, AppNavHost) ...

// --- Modified LoginScreen (still within Navigation.kt) ---
@Composable
fun LoginScreen(
    userType: String,
    authViewModel: AuthViewModel, // Accept AuthViewModel
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("$userType Login") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome Back, $userType!", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(32.dp))

            // CORRECTED OutlinedTextField for Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = { Text(stringResource(R.string.label_email_username)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = errorMessage?.contains("email", ignoreCase = true) == true || (errorMessage != null && email.isBlank())
            )
            Spacer(modifier = Modifier.height(16.dp))

            // CORRECTED OutlinedTextField for Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.label_password)) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage?.contains("password", ignoreCase = true) == true || (errorMessage != null && password.isBlank())
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    errorMessage = null
                    isLoading = true
                    // Basic client-side validation for emptiness
                    if (email.isNotBlank() && password.isNotBlank()) {
                        val roleToLogin = if (userType == "Farmer") UserRole.FARMER else UserRole.VENDOR
                        Log.d("LoginScreen", "Calling authViewModel.loginUser with role: $roleToLogin for userType: $userType")
                        authViewModel.loginUser(email, password, roleToLogin)
                        // ...
                        return@Button
                    }

                    val roleToLogin = if (userType == "Farmer") UserRole.FARMER else UserRole.VENDOR
                    authViewModel.loginUser(email, password, roleToLogin)
                    // Navigation will be handled by AppNavHost reacting to isLoggedIn state.
                    // The onLoginSuccess callback can be used for any additional actions needed
                    // immediately after initiating the login, or after it's confirmed.
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !isLoading // Disable button while loading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text(stringResource(R.string.button_login), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.prompt_no_account))
                TextButton(onClick = onNavigateToRegister) {
                    Text(stringResource(R.string.button_register_here))
                }
            }
        }
    }

    val isLoggedIn by authViewModel.isUserLoggedIn.collectAsState()
    // This LaunchedEffect will react when isLoggedIn state changes
    LaunchedEffect(isLoggedIn, authViewModel) { // Add authViewModel as a key if its error state is observed
        if (isLoggedIn) {
            Log.d("LoginScreen", "isLoggedIn is true, triggering onLoginSuccess from LaunchedEffect")
            onLoginSuccess() // Navigate after ViewModel confirms login
            isLoading = false
        } else {
            // Potentially check for an error message from AuthViewModel after a login attempt
            // if (authViewModel.authError.value != null) { // Assuming authViewModel has an error state
            //    errorMessage = authViewModel.authError.value
            //    isLoading = false
            //    authViewModel.clearAuthError() // Clear the error in ViewModel
            // }
            // For now, if not loggedIn and isLoading was true, it means login attempt might have finished (or started)
            // If you want to handle errors from ViewModel, you'd collect that error state.
            if (isLoading && !isLoggedIn) { // If a login attempt was made but it didn't result in loggedIn
                // This is a simplistic way to handle it without a dedicated error state from ViewModel
                // A better approach is to have authViewModel.authError.collectAsState()
                // errorMessage = "Login failed. Please check your credentials." // Generic error
                // isLoading = false
            }
        }
    }
}

// ... (Rest of Navigation.kt: RegisterScreen, Placeholders, Previews)

// --- Modified RegisterScreen (still within Navigation.kt) ---
@Composable
fun RegisterScreen(
    userType: String,
    authViewModel: AuthViewModel, // Accept AuthViewModel
    onRegistrationSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var businessName by remember { mutableStateOf("") } // Vendor specific
    var gstNumber by remember { mutableStateOf("") }    // Vendor specific
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create $userType Account") },
                navigationIcon = { IconButton(onClick = onNavigateToLogin) { Icon(Icons.Filled.ArrowBack, "Back") } }
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
            verticalArrangement = Arrangement.Center
        ) {
            Text("Join as $userType", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(value = name, onValueChange = { name = it.trim() }, label = { Text(stringResource(R.string.label_full_name)) }, isError = errorMessage?.contains("name", ignoreCase = true) == true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = { email = it.trim() }, label = { Text(stringResource(R.string.label_email_address)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), isError = errorMessage?.contains("email", ignoreCase = true) == true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            if (userType == "Vendor") {
                OutlinedTextField(value = businessName, onValueChange = { businessName = it.trim() }, label = { Text(stringResource(R.string.label_business_name)) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = gstNumber, onValueChange = { gstNumber = it.trim() }, label = { Text(stringResource(R.string.label_gst_number)) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password (min 6 chars)") }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, "") } }, isError = errorMessage?.contains("password", ignoreCase = true) == true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm Password") }, visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, "") } }, isError = errorMessage?.contains("match", ignoreCase = true) == true, modifier = Modifier.fillMaxWidth())

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    errorMessage = null // Clear previous error
                    isLoading = true
                    // Basic client-side validation
                    val validationError = when {
                        name.isBlank() -> "Full name cannot be empty."
                        email.isBlank() -> "Email cannot be empty."
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format."
                        password.length < 6 -> "Password must be at least 6 characters."
                        password != confirmPassword -> "Passwords do not match."
                        // TODO: Add validation for vendor-specific fields if necessary
                        else -> null
                    }

                    if (validationError != null) {
                        errorMessage = validationError
                        isLoading = false
                    } else {
                        val roleToRegister = if (userType == "Farmer") UserRole.FARMER else UserRole.VENDOR
                        authViewModel.registerUser(email, password, name, roleToRegister) // Include businessName, gstNumber if vendor
                        // After registration, typically navigate to login screen
                        // The AuthViewModel.registerUser should not change isLoggedIn state directly,
                        // it should just perform the registration.
                        isLoading = false // Registration attempt made
                        onRegistrationSuccess() // Callback to navigate (e.g., to login)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text(stringResource(R.string.button_register), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?")
                TextButton(onClick = onNavigateToLogin) {
                    Text("Login Here")
                }
            }
        }
    }
}


// GenericFeatureScreenPlaceholder (Keep as is)
@Composable
fun GenericFeatureScreenPlaceholder(navController: NavHostController, screenTitle: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Content for $screenTitle", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "This is a placeholder screen. Replace it with your actual screen implementation for '$screenTitle'.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// Specific Placeholders (Keep as is)
@Composable fun PestDetectionScreenPlaceholder(navController: NavHostController) { GenericFeatureScreenPlaceholder(navController, "Pest Detection") }
@Composable fun DiseaseDiagnosisScreenPlaceholder(navController: NavHostController) { GenericFeatureScreenPlaceholder(navController, "Disease Diagnosis") }
@Composable fun FarmBudgetScreenPlaceholder(navController: NavHostController) { GenericFeatureScreenPlaceholder(navController, "Farm Budget") }


// Previews (Keep as is, but ensure AuthViewModel can be previewed or mocked if necessary)
@Preview(showBackground = true, name = "Farmer Login Screen Preview")
@Composable
fun FarmerLoginScreenPreview() {
    MaterialTheme {
        // For preview, you might need to provide a mock AuthViewModel or a simple one that doesn't use context
        val mockAuthViewModel = AuthViewModel(LocalContext.current.applicationContext) // Basic instantiation for preview
        LoginScreen(onLoginSuccess = {}, onNavigateToRegister = {}, userType = "Farmer", authViewModel = mockAuthViewModel)
    }
}

@Preview(showBackground = true, name = "Vendor Register Screen Preview")
@Composable
fun VendorRegisterScreenPreview() {
    MaterialTheme {
        val mockAuthViewModel = AuthViewModel(LocalContext.current.applicationContext) // Basic for preview
        RegisterScreen(onRegistrationSuccess = {}, onNavigateToLogin = {}, userType = "Vendor", authViewModel = mockAuthViewModel)
    }
}

