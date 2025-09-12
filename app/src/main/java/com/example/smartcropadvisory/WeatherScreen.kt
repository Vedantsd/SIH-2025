package com.example.smartcropadvisory

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.smartcropadvisory.models.UiWeatherState
import com.example.smartcropadvisory.viewmodels.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    navController: NavController,
    weatherViewModel: WeatherViewModel = viewModel() // Hilt or manual VM factory for production
) {
    val uiState by weatherViewModel.uiState.collectAsState() // Use collectAsStateWithLifecycle in Fragments/Activities

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                weatherViewModel.refreshWeatherData() // Permission granted, try fetching
            } else {
                // User denied permission, uiState.needsPermission will be true
                // You might show a Snackbar or a more persistent message
            }
        }
    )

    // Effect to request permission if needed when the screen becomes visible
    // or when uiState.needsPermission becomes true
    LaunchedEffect(uiState.needsPermission) {
        if (uiState.needsPermission) {
            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🌦️ Weather Forecast") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { weatherViewModel.refreshWeatherData() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh Weather")
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
            verticalArrangement = Arrangement.Top
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 32.dp))
                Text("Fetching weather data...", style = MaterialTheme.typography.bodyLarge)
            } else if (uiState.needsPermission) {
                PermissionNeededView(onRequestPermission = {
                    permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                })
            } else if (uiState.errorMessage != null) {
                ErrorView(
                    message = uiState.errorMessage ?: "An unknown error occurred.",
                    onRetry = { weatherViewModel.refreshWeatherData() }
                )
            } else {
                CurrentWeatherDetails(uiState)
                // Optionally, add a section for city search if you keep that functionality
                // CitySearchView(onSearch = { cityName -> weatherViewModel.fetchWeatherForCity(cityName) })
            }
        }
    }
}

@Composable
fun PermissionNeededView(onRequestPermission: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Icon(
            Icons.Filled.LocationOff,
            contentDescription = "Location Permission Needed",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Location permission is required to fetch weather for your current location.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Please grant the permission to continue.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Icon(
            Icons.Filled.WarningAmber,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Oops! Something went wrong:",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

@Composable
fun CurrentWeatherDetails(weatherState: UiWeatherState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        weatherState.currentCityName ?: "Loading City...",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        weatherState.currentCondition ?: "Loading Condition...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    weatherState.lastUpdated?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                weatherState.currentConditionIconUrl?.let {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(it)
                            .crossfade(true)
                            .build(),
                        contentDescription = weatherState.currentCondition,
                        modifier = Modifier.size(72.dp),
                        contentScale = ContentScale.Fit
                    )
                } ?: Spacer(modifier = Modifier.size(72.dp)) // Placeholder if no icon
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                weatherState.currentTemperature ?: "--°C",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Light,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            weatherState.currentFeelsLike?.let {
                Text(
                    "Feels like: $it",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            weatherState.currentTempMinMax?.let {
                Text(
                    "Min/Max: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }


            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                WeatherDetailItem(
                    icon = Icons.Filled.WaterDrop,
                    label = "Humidity",
                    value = weatherState.currentHumidity ?: "--"
                )
                WeatherDetailItem(
                    icon = Icons.Filled.Air,
                    label = "Wind",
                    value = weatherState.currentWindSpeed ?: "--"
                )
            }
            if (weatherState.currentSunrise != null || weatherState.currentSunset != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    weatherState.currentSunrise?.let {
                        WeatherDetailItem(
                            icon = Icons.Filled.WbSunny, // Or a custom sunrise icon
                            label = "Sunrise",
                            value = it
                        )
                    }
                    weatherState.currentSunset?.let {
                        WeatherDetailItem(
                            icon = Icons.Filled.Brightness3, // Or a custom sunset icon
                            label = "Sunset",
                            value = it
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherDetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/*
// Optional: If you want to keep city search
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySearchView(modifier: Modifier = Modifier, onSearch: (String) -> Unit) {
    var cityInput by remember { mutableStateOf("") }
    OutlinedTextField(
        value = cityInput,
        onValueChange = { cityInput = it },
        label = { Text("Search City (Optional)") },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        trailingIcon = {
            IconButton(onClick = { if (cityInput.isNotBlank()) onSearch(cityInput) }) {
                Icon(Icons.Filled.Search, contentDescription = "Search City")
            }
        }
    )
}
*/
