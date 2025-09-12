package com.example.smartcropadvisory.data // Assuming this is your package

import com.example.smartcropadvisory.models.ApiWeatherResponse
import com.example.smartcropadvisory.models.UiWeatherState
// No import for toUiWeatherState needed here anymore
import com.example.smartcropadvisory.network.WeatherApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import android.util.Log // For logging exceptions
import java.text.SimpleDateFormat // For date formatting
import java.util.Date             // For date formatting
import java.util.Locale           // For date formatting

// A simple sealed class for representing results, including errors
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}

class WeatherRepository {

    private val weatherApiService = WeatherApiClient.service
    private val apiKey = WeatherApiClient.getApiKey()

    // --- Private Mapper Function defined inside the Repository ---
    private fun mapApiToUiState(apiResponse: ApiWeatherResponse): UiWeatherState {
        val weatherDesc = apiResponse.weather?.firstOrNull()
        val currentTimestamp = System.currentTimeMillis()
        val apiTimestamp = apiResponse.dt?.let { it * 1000L } ?: currentTimestamp
        val isDataPotentiallyStale = (currentTimestamp - apiTimestamp) > 3600000
        val cityDisplayName = apiResponse.name ?: "Unknown Location"

        return UiWeatherState(
            isLoading = false,
            errorMessage = null,
            lastUpdated = apiResponse.dt?.let {
                val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                "Updated: ${sdf.format(Date(it * 1000L))}${if (isDataPotentiallyStale) " (stale)" else ""}"
            } ?: "Updated: N/A",
            currentCityName = cityDisplayName,
            currentTemperature = apiResponse.mainMetrics?.temperature?.let { "%.1f°C".format(it) },
            currentCondition = weatherDesc?.main,
            currentConditionIconUrl = weatherDesc?.icon?.let { "https://openweathermap.org/img/wn/$it@2x.png" },
            currentHumidity = apiResponse.mainMetrics?.humidity?.let { "$it%" },
            currentWindSpeed = apiResponse.windInfo?.speed?.let { "%.1f m/s".format(it) },
            currentFeelsLike = apiResponse.mainMetrics?.feelsLike?.let { "%.1f°C".format(it) },
            currentTempMinMax = if (apiResponse.mainMetrics?.tempMin != null && apiResponse.mainMetrics.tempMax != null) {
                "Min: %.1f°C / Max: %.1f°C".format(apiResponse.mainMetrics.tempMin, apiResponse.mainMetrics.tempMax)
            } else {
                apiResponse.mainMetrics?.temperature?.let { "Temp: %.1f°C".format(it) }
            },
            currentSunrise = apiResponse.systemInfo?.sunrise?.let {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                sdf.format(Date(it * 1000L))
            },
            currentSunset = apiResponse.systemInfo?.sunset?.let {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                sdf.format(Date(it * 1000L))
            }
        )
    }
    // --- End of Private Mapper Function ---


    fun fetchWeatherByCoordinates(latitude: Double, longitude: Double): Flow<Result<UiWeatherState>> = flow {
        try {
            Log.d("WeatherRepository", "Fetching weather for coords: $latitude, $longitude")
            val response: ApiWeatherResponse = weatherApiService.getCurrentWeatherByCoordinates(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey,
                units = "metric"
            )

            if (response.responseCode != null && response.responseCode != 200) {
                val errorMessage = "API Error ${response.responseCode}: ${response.weather?.firstOrNull()?.description ?: response.name ?: "Unknown API error"}"
                Log.e("WeatherRepository", errorMessage)
                emit(Result.Error(Exception(errorMessage)))
            } else if (response.mainMetrics == null || response.weather.isNullOrEmpty()) {
                val errorMessage = "Incomplete weather data received for $latitude, $longitude"
                Log.e("WeatherRepository", errorMessage)
                emit(Result.Error(Exception(errorMessage)))
            } else {
                // Call the private mapper function
                emit(Result.Success(mapApiToUiState(response)))
            }
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Network error fetching weather by coords for $latitude, $longitude", e)
            emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    fun fetchWeatherByCityName(cityName: String): Flow<Result<UiWeatherState>> = flow {
        try {
            Log.d("WeatherRepository", "Fetching weather for city: $cityName")
            val response: ApiWeatherResponse = weatherApiService.getCurrentWeatherByCityName(
                cityName = cityName,
                apiKey = apiKey,
                units = "metric"
            )

            if (response.responseCode != null && response.responseCode != 200) {
                val errorMessage = "API Error ${response.responseCode}: ${response.weather?.firstOrNull()?.description ?: response.name ?: "City not found or API error"}"
                Log.e("WeatherRepository", errorMessage)
                emit(Result.Error(Exception(errorMessage)))
            } else if (response.mainMetrics == null || response.weather.isNullOrEmpty()) {
                val errorMessage = "Incomplete weather data received for $cityName"
                Log.e("WeatherRepository", errorMessage)
                emit(Result.Error(Exception(errorMessage)))
            } else {
                // Call the private mapper function
                emit(Result.Success(mapApiToUiState(response)))
            }
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Network error fetching weather by city $cityName", e)
            emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)
}

