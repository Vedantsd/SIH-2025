package com.example.smartcropadvisory

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity // For context casting
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartcropadvisory.models.UiWeatherState
import com.example.smartcropadvisory.models.Feature
import com.example.smartcropadvisory.models.FeatureCategory
import com.example.smartcropadvisory.models.UiIotSensorState
import com.example.smartcropadvisory.models.SensorData
import com.example.smartcropadvisory.viewmodels.AuthViewModel
import com.example.smartcropadvisory.viewmodels.WeatherViewModel
import com.example.smartcropadvisory.viewmodels.IotSensorViewModel
import com.google.accompanist.flowlayout.FlowRow
// import com.example.smartcropadvisory.utils.LocaleHelper // If you implement full language change

const val IOT_DASHBOARD_EXPAND_KEY = "iot_dashboard_expand_route_farmer"

// This list is for the Farmer's Home Screen
private val farmerHomeScreenFeatureCategoriesList: List<FeatureCategory> = listOf(
    FeatureCategory(
        "Soil, Water & Irrigation",
        listOf(
            Feature("Soil Services", Icons.Filled.Spa, ScreenRoutes.SoilHome.route),
            Feature("Irrigation Plan", Icons.Filled.WaterDrop, ScreenRoutes.IrrigationPlan.route)
        )
    ),
    FeatureCategory(
        "Crop Health & Management",
        listOf(
            Feature("Crop Recommendation", Icons.Filled.Recommend, ScreenRoutes.CropRecommendation.route),
            Feature("Pest Detection", Icons.Filled.BugReport, ScreenRoutes.PestDetection.route),

        )
    ),
    FeatureCategory(
        "Marketplace & Support",
        listOf(
            Feature("Market Prices", Icons.Filled.TrendingUp, ScreenRoutes.MarketPrices.route),
            Feature("Sell My Crops", Icons.Filled.Agriculture, ScreenRoutes.FarmerSellCrops.route),
            Feature("Buy Agri Inputs", Icons.Filled.ShoppingCart, ScreenRoutes.FarmerBuyInputs.route),
            Feature("Govt. Schemes", Icons.Filled.Policy, ScreenRoutes.GovtSchemes.route)
        )
    ),
    FeatureCategory(
        "Tools & Utilities",
        listOf(
            Feature("IoT Dashboard", Icons.Filled.Sensors, IOT_DASHBOARD_EXPAND_KEY),

            Feature("Crop Calendar", Icons.Filled.CalendarToday, ScreenRoutes.CropCalendar.route),
            Feature("Crop Mapping", Icons.Filled.Map, ScreenRoutes.CropMapping.route)
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen( // This is now effectively FarmerHomeScreen
    navController: NavHostController,
    userRole: UserRole, // Passed in from Navigation.kt
    weatherViewModel: WeatherViewModel = viewModel(),
    iotSensorViewModel: IotSensorViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    categories: List<FeatureCategory> = farmerHomeScreenFeatureCategoriesList
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val activityContext = LocalContext.current as ComponentActivity // Context for permission and activity results

    val weatherUiState by weatherViewModel.uiState.collectAsState()
    val iotSensorUiState by iotSensorViewModel.uiState.collectAsState()
    var isIotDashboardExpanded by rememberSaveable { mutableStateOf(false) }

    // --- Location Permission Launcher ---
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (fineLocationGranted || coarseLocationGranted) {
                weatherViewModel.onPermissionGranted()
                Log.d("HomeScreen", "Location permission GRANTED")
            } else {
                weatherViewModel.onPermissionDenied()
                Log.d("HomeScreen", "Location permission DENIED")
                Toast.makeText(activityContext, "Location permission is required for weather updates.", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(weatherUiState.needsPermission) {
        if (weatherUiState.needsPermission) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    LaunchedEffect(Unit, isIotDashboardExpanded) { // Also re-fetch on IoT expand/collapse change
        weatherViewModel.refreshWeatherData()
        if (isIotDashboardExpanded) {
            iotSensorViewModel.refreshSensorData()
        }
    }

    // --- Speech Recognition Launcher ---
    val speechRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val spokenText: ArrayList<String>? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!spokenText.isNullOrEmpty()) {
                val command = spokenText[0]
                Log.d("HomeScreen", "Voice Command Heard: $command")
                Toast.makeText(activityContext, "Heard: $command", Toast.LENGTH_LONG).show()
                // TODO: Process the voice command (e.g., navigate, call ViewModel function)
            }
        } else {
            Log.d("HomeScreen", "Speech recognition cancelled or failed. Result: ${result.resultCode}")
            Toast.makeText(activityContext, "Speech recognition did not complete.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Audio Permission Launcher (for voice input) ---
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, now launch speech recognizer
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...") // TODO: Use string resource
            }
            try {
                speechRecognitionLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(activityContext, "Speech recognition not available.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(activityContext, "Audio permission denied. Voice support unavailable.", Toast.LENGTH_LONG).show()
        }
    }

    fun startVoiceInput() {
        when {
            ContextCompat.checkSelfPermission(
                activityContext,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...") // TODO: Use string resource
                }
                try {
                    speechRecognitionLauncher.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(activityContext, "Speech recognition not available on this device.", Toast.LENGTH_SHORT).show()
                    Log.e("HomeScreen", "Speech recognition error", e)
                }
            }
            ActivityCompat.shouldShowRequestPermissionRationale( // Use activityContext for ActivityCompat
                activityContext,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                // TODO: Show a rationale dialog explaining why the permission is needed
                Toast.makeText(activityContext, "Audio permission needed for voice support.", Toast.LENGTH_LONG).show()
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) // Request after showing rationale (or directly)
            }
            else -> {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // --- Placeholder for Language Dialog ---
    var showLanguageDialog by remember { mutableStateOf(false) }
    if (showLanguageDialog) {
        // TODO: Replace with your actual LanguageSelectionDialog (from previous examples)
        // LanguageSelectionDialog(
        //     currentLanguage = LocaleHelper.getLanguage(activityContext),
        //     onLanguageSelected = { languageCode ->
        //         LocaleHelper.setLocale(activityContext, languageCode)
        //         showLanguageDialog = false
        //         activityContext.recreate() // To apply language changes
        //     },
        //     onDismiss = { showLanguageDialog = false }
        // )
        Toast.makeText(activityContext, "Language selection dialog placeholder", Toast.LENGTH_SHORT).show()
        showLanguageDialog = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.farmer_dashboard_title)) },
                actions = {
                    IconButton(onClick = { startVoiceInput() }) {
                        Icon(Icons.Filled.RecordVoiceOver, stringResource(R.string.action_voice_support))
                    }
                    IconButton(onClick = {
                        // TODO: Implement actual language change functionality
                        // For now, it just shows a placeholder Toast via showLanguageDialog.
                        showLanguageDialog = true
                        Log.d("HomeScreen", "Language icon clicked.")
                    }) {
                        Icon(Icons.Filled.Translate, stringResource(R.string.action_change_language))
                    }
                    IconButton(onClick = {
                        weatherViewModel.refreshWeatherData()
                        if (isIotDashboardExpanded) iotSensorViewModel.refreshSensorData()
                    }) {
                        Icon(Icons.Filled.Refresh, stringResource(R.string.action_refresh))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController, currentRoute = currentRoute, userRole = userRole)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            WeatherCard(
                weatherState = weatherUiState,
                onClick = { navController.navigate(ScreenRoutes.Weather.route) },
                onRequestPermission = {
                    locationPermissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                },
                onRetry = { weatherViewModel.refreshWeatherData() }
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (categories.isEmpty()) {
                Text(stringResource(R.string.info_no_features_available))
            } else {
                categories.forEach { category ->
                    val featuresInThisCategory = category.features
                    if (featuresInThisCategory.isNotEmpty()) {
                        Text(
                            text = category.title, // TODO: Make category titles string resources
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            mainAxisSpacing = 10.dp,
                            crossAxisSpacing = 10.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            featuresInThisCategory.forEach { feature ->
                                if (feature.route == IOT_DASHBOARD_EXPAND_KEY) {
                                    IoTFeatureTile(
                                        feature = feature,
                                        iotState = iotSensorUiState,
                                        isExpanded = isIotDashboardExpanded,
                                        onToggleExpand = { isIotDashboardExpanded = !isIotDashboardExpanded },
                                        onMasterSwitchToggle = { iotSensorViewModel.toggleMasterSwitch() },
                                        onSensorToggle = { sensorName -> iotSensorViewModel.toggleSensor(sensorName) },
                                        onRetry = { iotSensorViewModel.refreshSensorData() }
                                    )
                                } else {
                                    FeatureTile(
                                        feature = feature,
                                        onClick = {
                                            if (feature.route.isNotBlank() && feature.route != "placeholder_route") {
                                                navController.navigate(feature.route)
                                            } else {
                                                Toast.makeText(activityContext, "${feature.title} coming soon!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        // Simpler check for last item to avoid divider after the absolute last category's content
                        if (categories.indexOf(category) < categories.filter { it.features.isNotEmpty() }.size -1 ) {
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Added padding at the very bottom
        }
    }
}

// Regular FeatureTile
@Composable
fun FeatureTile(feature: Feature, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(90.dp) // Consider adjusting width based on text length or screen size
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(60.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title, // TODO: Use stringResource for content description
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = feature.title, // TODO: Use stringResource if feature titles are dynamic
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// IoTFeatureTile that can expand
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IoTFeatureTile(
    feature: Feature,
    iotState: UiIotSensorState,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onMasterSwitchToggle: () -> Unit,
    onSensorToggle: (String) -> Unit,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .then(if (isExpanded) Modifier.fillMaxWidth() else Modifier.width(90.dp))
            .clickable(onClick = onToggleExpand)
            .padding(vertical = 8.dp, horizontal = 4.dp), // Common padding
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 6.dp else 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(if (isExpanded) 16.dp else 8.dp), // Dynamic padding inside
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isExpanded) {
                // Unexpanded state: Icon and Title
                Card( // Inner card for icon background
                    modifier = Modifier.size(60.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = feature.icon,
                            contentDescription = feature.title, // TODO: Use stringResource
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = feature.title, // TODO: Use stringResource
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            } else {
                // Expanded state: Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        feature.title, // "IoT Dashboard" // TODO: Use stringResource
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Filled.ExpandLess,
                        contentDescription = "Collapse IoT Dashboard" // TODO: Use stringResource
                    )
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Master Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End // Or SpaceBetween if you want text on left
                ) {
                    Text(
                        if (iotState.masterSwitchOn) "All Sensors On" else "All Sensors Off", // TODO: Use stringResource
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = iotState.masterSwitchOn,
                        onCheckedChange = { onMasterSwitchToggle() },
                        thumbContent = if (iotState.masterSwitchOn) {
                            { Icon(Icons.Filled.Done, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
                        } else null
                    )
                }
            }

            // Expanded content (Sensor Data) - only visible if isExpanded is true
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp), // Add padding if header elements are not present
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (iotState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Text(
                            "Loading sensor data...", // TODO: Use stringResource
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 8.dp)
                        )
                    } else if (iotState.errorMessage != null) {
                        Text(
                            text = iotState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                        )
                        Button(
                            onClick = onRetry,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 8.dp)
                        ) {
                            Text("Retry") // TODO: Use stringResource
                        }
                    } else if (iotState.sensors.isEmpty()) {
                        Text("No sensor data available.", modifier = Modifier.align(Alignment.CenterHorizontally)) // TODO: Use stringResource
                    } else {
                        iotState.sensors.forEach { sensor ->
                            SensorRow(sensor = sensor, onToggle = { onSensorToggle(sensor.name) })
                        }
                    }
                }
            }
        }
    }
}

// WeatherCard definition
@Composable
fun WeatherCard(
    weatherState: UiWeatherState,
    onClick: () -> Unit,
    onRequestPermission: () -> Unit,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                // Allow click to weather detail only if data is loaded and no errors/permission issues
                if (!weatherState.isLoading && !weatherState.needsPermission && weatherState.errorMessage == null) {
                    onClick()
                }
            }),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (weatherState.isLoading) {
                CircularProgressIndicator()
                Text("Loading weather...", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            } else if (weatherState.needsPermission) {
                Text(
                    "Location permission is needed to display current weather.", // TODO: Use stringResource
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onRequestPermission, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Grant Permission") // TODO: Use stringResource
                }
            } else if (weatherState.errorMessage != null) {
                Text(
                    text = weatherState.errorMessage, // This comes from ViewModel, might be already localized or general
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = onRetry, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Retry") // TODO: Use stringResource
                }
            } else {
                Text(
                    text = weatherState.currentCityName ?: "Loading City...", // TODO: Use stringResource for "Loading City"
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    weatherState.currentConditionIconUrl?.let { iconUrl ->
                        coil.compose.AsyncImage( // Using Coil to load image from URL
                            model = iconUrl,
                            contentDescription = weatherState.currentCondition, // This can be the alt text
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = "${weatherState.currentTemperature ?: "--"} - ${weatherState.currentCondition ?: "N/A"}",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                weatherState.lastUpdated?.let {
                    Text(
                        text = it, // Format this in ViewModel ideally
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.height(12.dp)) // Increased spacing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeatherInfoItem(Icons.Filled.Opacity, weatherState.currentHumidity ?: "--", "Humidity")
                    WeatherInfoItem(Icons.Filled.Air, weatherState.currentWindSpeed ?: "--", "Wind")
                    WeatherInfoItem(Icons.Filled.Thermostat, weatherState.currentFeelsLike ?: "--", "Feels Like")
                }
            }
        }
    }
}

// WeatherInfoItem definition
@Composable
fun WeatherInfoItem(icon: ImageVector, value: String, label: String) { // TODO: label should be stringResource
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

// SensorRow definition
@Composable
fun SensorRow(sensor: SensorData, onToggle: (Boolean) -> Unit) { // sensor.name can be made stringResource key
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = sensor.icon,
                contentDescription = sensor.name,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 12.dp),
                tint = if (sensor.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Column {
                Text(
                    sensor.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (sensor.isEnabled) LocalContentColor.current else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                AnimatedVisibility(visible = sensor.isEnabled) { // Show value only if enabled
                    Text(
                        "${sensor.value} ${sensor.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
        Switch(
            checked = sensor.isEnabled,
            onCheckedChange = onToggle, // This already passes the new state
            thumbContent = if (sensor.isEnabled) {
                { Icon(Icons.Filled.Done, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
            } else null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FarmerHomeScreenPreview() {
    MaterialTheme {
        // Provide a NavController and a UserRole for the preview
        HomeScreen(navController = rememberNavController(), userRole = UserRole.FARMER)
    }
}
