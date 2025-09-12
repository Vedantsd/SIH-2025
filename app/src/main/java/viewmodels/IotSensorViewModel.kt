package com.example.smartcropadvisory.viewmodels

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterDrama // Placeholder for Soil Moisture
import androidx.compose.material.icons.filled.Thermostat // Placeholder for pH (reusing)
import androidx.compose.material.icons.filled.ScatterPlot // Placeholder for NPK
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcropadvisory.models.SensorData
import com.example.smartcropadvisory.models.UiIotSensorState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IotSensorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UiIotSensorState(isLoading = true))
    val uiState: StateFlow<UiIotSensorState> = _uiState.asStateFlow()

    init {
        loadSensorData()
    }

    private fun loadSensorData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1000) // Simulate network delay
            _uiState.update {
                it.copy(
                    isLoading = false,
                    sensors = listOf(
                        SensorData("Soil Moisture", "65", "%", Icons.Default.FilterDrama, true),
                        SensorData("Soil pH", "6.8", "", Icons.Default.Thermostat, true),
                        SensorData("NPK Levels", "12-8-10", "ppm", Icons.Default.ScatterPlot, true)
                    ),
                    masterSwitchOn = true
                )
            }
        }
    }

    fun toggleSensor(sensorName: String) {
        _uiState.update { currentState ->
            val updatedSensors = currentState.sensors.map { sensor ->
                if (sensor.name == sensorName) {
                    sensor.copy(isEnabled = !sensor.isEnabled)
                } else {
                    sensor
                }
            }
            // If all individual sensors are off, turn master switch off.
            // If any sensor is turned on, and master was off, turn master on.
            val newMasterSwitchState = if (updatedSensors.all { !it.isEnabled }) false
            else if (updatedSensors.any {it.isEnabled} && !currentState.masterSwitchOn) true
            else currentState.masterSwitchOn

            currentState.copy(sensors = updatedSensors, masterSwitchOn = newMasterSwitchState)
        }
    }

    fun toggleMasterSwitch() {
        _uiState.update { currentState ->
            val newMasterState = !currentState.masterSwitchOn
            val updatedSensors = currentState.sensors.map {
                it.copy(isEnabled = newMasterState) // All sensors follow master switch
            }
            currentState.copy(sensors = updatedSensors, masterSwitchOn = newMasterState)
        }
    }

    fun refreshSensorData() {
        // In a real app, this would re-fetch from a source
        loadSensorData()
    }
}
