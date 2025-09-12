package com.example.smartcropadvisory.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
// import android.os.Looper // No longer strictly needed if only using getCurrentLocation
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcropadvisory.data.Result
import com.example.smartcropadvisory.data.WeatherRepository
import com.example.smartcropadvisory.models.UiWeatherState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // <<< THIS IS THE CRITICAL IMPORT
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherViewModel(application: Application) : AndroidViewModel(application) {    private val _uiState = MutableStateFlow(UiWeatherState(isLoading = true))
    val uiState: StateFlow<UiWeatherState> = _uiState.asStateFlow()

    private val weatherRepository = WeatherRepository()
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application.applicationContext)

    private var isFetchingLocationOrWeather = false

    init {
        Log.d("WeatherViewModel", "ViewModel initialized.")
        refreshWeatherData()
    }

    fun refreshWeatherData() {
        Log.d("WeatherViewModel", "refreshWeatherData called.")
        if (isFetchingLocationOrWeather) {
            Log.d("WeatherViewModel", "Already fetching location or weather, refresh skipped.")
            return
        }

        if (hasLocationPermission()) {
            _uiState.update { it.copy(isLoading = true, needsPermission = false, errorMessage = null) }
            fetchCurrentLocationAndThenWeather()
        } else {
            Log.d("WeatherViewModel", "Permission check failed in refreshWeatherData. Updating UI to request permission.")
            _uiState.update { it.copy(isLoading = false, needsPermission = true, errorMessage = null) }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocationAndThenWeather() {
        isFetchingLocationOrWeather = true
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        Log.d("WeatherViewModel", "Attempting to fetch current location.")

        viewModelScope.launch {
            try {
                val cancellationTokenSource = CancellationTokenSource()
                // The result of fusedLocationClient.getCurrentLocation(...) is a Task<Location>
                // The .await() extension function (from kotlinx.coroutines.tasks.await)
                // suspends until the Task is complete and returns the Location object.
                val currentLocation: android.location.Location? = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationTokenSource.token
                ).await() // <<< THIS CALLS THE EXTENSION FUNCTION

                if (currentLocation != null) {
                    // currentLocation is now definitely an android.location.Location object
                    Log.d("WeatherViewModel", "Location received: Lat=${currentLocation.latitude}, Lon=${currentLocation.longitude}")
                    fetchWeatherForCoordinates(currentLocation.latitude, currentLocation.longitude)
                } else {
                    Log.e("WeatherViewModel", "Failed to get current location (null).")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Unable to get current location. Ensure location is enabled."
                        )
                    }
                    isFetchingLocationOrWeather = false
                }
            } catch (e: SecurityException) {
                Log.e("WeatherViewModel", "SecurityException while fetching location: ${e.message}")
                _uiState.update { it.copy(isLoading = false, needsPermission = true, errorMessage = "Location permission error.") }
                isFetchingLocationOrWeather = false
            } catch (e: Exception) { // Catch other exceptions like CancellationException if the task is cancelled
                Log.e("WeatherViewModel", "Exception while fetching location: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error getting location: ${e.localizedMessage}") }
                isFetchingLocationOrWeather = false
            }
        }
    }

    // ... (rest of your WeatherViewModel code: fetchWeatherForCoordinates, fetchWeatherForCity, hasLocationPermission, onPermissionGranted, onPermissionDenied, onCleared)
    // Ensure these functions remain as previously corrected.

    private fun fetchWeatherForCoordinates(latitude: Double, longitude: Double) {
        Log.d("WeatherViewModel", "Fetching weather for coordinates: Lat=$latitude, Lon=$longitude")
        viewModelScope.launch {
            try {
                weatherRepository.fetchWeatherByCoordinates(latitude, longitude)
                    .catch { exception ->
                        Log.e("WeatherViewModel", "Error in weather data Flow: ${exception.message}", exception)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Weather data error: ${exception.localizedMessage ?: "Unknown error"}"
                            )
                        }
                        isFetchingLocationOrWeather = false
                    }
                    .collect { result ->
                        when (result) {
                            is Result.Success -> {
                                Log.d("WeatherViewModel", "Weather data success.")
                                _uiState.value = result.data.copy(
                                    isLoading = false,
                                    needsPermission = false,
                                    errorMessage = null,
                                    lastUpdated = "Updated: ${
                                        SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
                                    }"
                                )
                            }
                            is Result.Error -> {
                                Log.e("WeatherViewModel", "Weather data error from repository: ${result.exception.message}")
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = result.exception.localizedMessage ?: "Failed to fetch weather"
                                    )
                                }
                            }
                        }
                        isFetchingLocationOrWeather = false
                    }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Exception starting weather fetch: ${e.message}", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error initializing weather fetch: ${e.localizedMessage}")
                }
                isFetchingLocationOrWeather = false
            }
        }
    }

    fun fetchWeatherForCity(cityName: String) {
        if (cityName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "City name cannot be empty.") }
            return
        }
        if (isFetchingLocationOrWeather) {
            Log.d("WeatherViewModel", "Already fetching, city search for '$cityName' skipped.")
            return
        }
        Log.d("WeatherViewModel", "Fetching weather for city: $cityName")
        isFetchingLocationOrWeather = true
        _uiState.update { it.copy(isLoading = true, errorMessage = null, needsPermission = false) }

        viewModelScope.launch {
            try {
                weatherRepository.fetchWeatherByCityName(cityName)
                    .catch { exception ->
                        Log.e("WeatherViewModel", "Error in city weather data Flow: ${exception.message}", exception)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "City weather error: ${exception.localizedMessage ?: "Unknown error"}"
                            )
                        }
                        isFetchingLocationOrWeather = false
                    }
                    .collect { result ->
                        when (result) {
                            is Result.Success -> {
                                Log.d("WeatherViewModel", "City weather data success.")
                                _uiState.value = result.data.copy(
                                    isLoading = false,
                                    errorMessage = null,
                                    lastUpdated = "Updated: ${ SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date()) }"
                                )
                            }
                            is Result.Error -> {
                                Log.e("WeatherViewModel", "City weather data error from repository: ${result.exception.message}")
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = result.exception.localizedMessage ?: "Failed to fetch weather for $cityName"
                                    )
                                }
                            }
                        }
                        isFetchingLocationOrWeather = false
                    }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Exception starting city weather fetch: ${e.message}", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error initializing city weather fetch: ${e.localizedMessage}")
                }
                isFetchingLocationOrWeather = false
            }
        }
    }

    fun hasLocationPermission(): Boolean {
        val context = getApplication<Application>().applicationContext
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        Log.d("WeatherViewModel", "Fine location: $fineLocationGranted, Coarse location: $coarseLocationGranted")
        return fineLocationGranted || coarseLocationGranted
    }

    fun onPermissionGranted() {
        Log.d("WeatherViewModel", "onPermissionGranted called from UI.")
        _uiState.update { it.copy(needsPermission = false, errorMessage = null) }
        refreshWeatherData()
    }

    fun onPermissionDenied() {
        Log.d("WeatherViewModel", "onPermissionDenied called from UI.")
        _uiState.update {
            it.copy(
                isLoading = false,
                needsPermission = true,
                errorMessage = "Location permission is required to display current weather."
            )
        }
    }
    override fun onCleared() {
        super.onCleared()
        Log.d("WeatherViewModel", "ViewModel cleared.")
    }
}
