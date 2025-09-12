package com.example.smartcropadvisory.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline // Example default for ImageVector
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient // Only needed if you choose @Transient strategy

// --- General UI Models ---
@Serializable
data class UiWeatherState(
    val isLoading: Boolean = false,
    val needsPermission: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdated: String? = null,
    val currentCityName: String? = "Sample City",
    val currentTemperature: String? = "25°C",
    val currentCondition: String? = "Sunny",
    val currentConditionIconUrl: String? = null, // Ensure this exists for weather icon
    val currentHumidity: String? = "60%",
    val currentWindSpeed: String? = "10 km/h",
    val currentFeelsLike: String? = "27°C",
    val currentTempMinMax: String? = null,
    val currentSunrise: String? = null,
    val currentSunset: String? = null
)

@Serializable
data class FarmerProfile(
    val name: String,
    val email: String,
    val state: String,
    val district: String,
    val landSize: String,
    val cropType: String
)

// --- Soil Related Models ---
@Serializable
data class SoilHealth(
    val location: String,
    val fertility: String,
    val waterHoldingCapacity: String,
    val organicMatter: String,
    val phLevel: String,
    val note: String
)

@Serializable
data class SoilTestUnique(
    val parameter: String,
    val value: String,
    val recommendation: String,
    val percentage: Int,
    val status: String
)

// --- Crop Recommendation Models ---
@Serializable
data class RecommendedCrop(
    val name: String,
    val suitabilityScore: Int,
    val reasons: List<String>,
    val notes: String? = null,
    val estimatedCostPerAcre: Double? = null,
    val withinBudget: Boolean? = null
)

// --- Irrigation Plan Models ---
data class CropForIrrigation(
    val id: String,
    val name: String
)

@Serializable
data class IrrigationScheduleItem(
    val id: String = UUID.randomUUID().toString(),
    val cropId: String,
    val stage: String,
    val frequencyDays: Int,
    val durationMinutes: Int,
    val waterAmount: String,
    var nextWateringDate: Long,
    var alertEnabled: Boolean = true,
    val notes: String? = null
)

@Serializable
data class UpcomingWateringEvent(
    val cropName: String,
    val stage: String,
    val wateringTime: Long,
    val scheduleId: String
)

// --- Market Screen Models ---
@Serializable
data class CropPrice(
    val emoji: String,
    val name: String,
    val price: String,
    val market: String? = null
)

// --- Government Schemes Models ---
@Serializable
data class GovtScheme(
    val id: String,
    val name: String,
    val description: String,
    val benefits: List<String>,
    val eligibility: String,
    val websiteUrl: String
)

// --- HomeScreen Feature Models ---
data class Feature(
    val title: String,
    val icon: ImageVector,
    val route: String
)

data class FeatureCategory(
    val title: String,
    val features: List<Feature>
)


// --- IoT Sensor Models ---
// REMOVED @Serializable for SensorData and UiIotSensorState to avoid ImageVector serialization issues
// If serialization is needed later, consider using @Transient for the icon or storing an icon identifier.
data class SensorData(
    val name: String,
    val value: String,
    val unit: String,
    val icon: ImageVector, // Keep as ImageVector for UI use
    var isEnabled: Boolean = true
)

data class UiIotSensorState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val sensors: List<SensorData> = emptyList(),
    val masterSwitchOn: Boolean = true
)


// --- API Response Model (for WeatherScreen, if using network calls) ---
@Serializable
data class ApiWeatherResponse(
    val name: String?,
    @SerialName("main")
    val mainMetrics: MainMetrics?,
    val weather: List<WeatherDescription>?,
    @SerialName("wind")
    val windInfo: WindInfo?,
    @SerialName("sys")
    val systemInfo: SystemInfo?,
    val dt: Long?,
    @SerialName("cod")
    val responseCode: Int?
)

@Serializable
data class MainMetrics(
    @SerialName("temp")
    val temperature: Float?,
    @SerialName("feels_like")
    val feelsLike: Float?,
    @SerialName("temp_min")
    val tempMin: Float?,
    @SerialName("temp_max")
    val tempMax: Float?,
    val pressure: Int?,
    val humidity: Int?
)

@Serializable
data class WeatherDescription(
    val id: Int?,
    val main: String?,
    val description: String?,
    val icon: String?
)

@Serializable
data class WindInfo(
    val speed: Float?,
    val deg: Int?
)

@Serializable
data class SystemInfo(
    val country: String?,
    val sunrise: Long?,
    val sunset: Long?
)

