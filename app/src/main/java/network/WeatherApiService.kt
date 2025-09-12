package com.example.smartcropadvisory.network

import com.example.smartcropadvisory.models.ApiWeatherResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather") // For OpenWeatherMap, the endpoint is data/2.5/weather
    suspend fun getCurrentWeatherByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric" // Default to metric (Celsius, meter/sec)
    ): ApiWeatherResponse

    @GET("weather")
    suspend fun getCurrentWeatherByCityName(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,@Query("units") units: String = "metric"
    ): ApiWeatherResponse
}

object WeatherApiClient {
    // IMPORTANT: REPLACE WITH YOUR ACTUAL OpenWeatherMap API KEY
    private const val API_KEY = "ab4212dfbb0632bef85a09d6c31ef23f" // <--- REPLACE THIS!!!

    // Base URL for OpenWeatherMap API
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true // Helpful if API sometimes returns slightly unexpected fields
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Logs request and response bodies
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val service: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }

    fun getApiKey(): String {
        if (API_KEY == "ab4212dfbb0632bef85a09d6c31ef23f") {
            // This is a developer warning, in a real app you might throw an error
            // or have a more robust way to handle missing keys.
            println("WARNING: OpenWeatherMap API Key is not set in WeatherApiClient.kt")
        }
        return API_KEY
    }
}
