package com.example.smartcropadvisory

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

// --- Data Models (Defined directly in this file) ---
data class CropForIrrigation(
    val id: String,
    val name: String,
    val icon: @Composable () -> Unit // For a Compose icon or image
)

data class IrrigationScheduleItem(
    val id: String = UUID.randomUUID().toString(),
    val cropId: String,
    val stage: String, // e.g., "Seedling", "Vegetative", "Flowering"
    val frequencyDays: Int,
    val durationMinutes: Int,
    val waterAmount: String, // e.g., "2 inches", "5 liters/plant"
    var nextWateringDate: Long, // Timestamp
    var alertEnabled: Boolean = true,
    val notes: String? = null
)

data class UpcomingWateringEvent(
    val cropName: String,
    val stage: String,
    val wateringTime: Long, // Timestamp
    val scheduleId: String
)

// --- Sample Data (Defined directly in this file) ---
val sampleCropsForIrrigation = listOf(
    CropForIrrigation("tomato", "Tomato") { Icon(Icons.Filled.LocalFlorist, contentDescription = "Tomato", tint = Color(0xFFE53935)) },
    CropForIrrigation("maize", "Maize") { Icon(Icons.Filled.Grass, contentDescription = "Maize", tint = Color(0xFF7CB342)) },
    CropForIrrigation("lettuce", "Lettuce") { Icon(Icons.Filled.Eco, contentDescription = "Lettuce", tint = Color(0xFF4CAF50)) },
    CropForIrrigation("peppers", "Peppers") { Icon(Icons.Filled.LocalFireDepartment, contentDescription = "Peppers", tint = Color(0xFFFB8C00)) }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IrrigationPlanScreen(
    navController: NavController
) {
    // --- State Management directly in Composable ---
    var selectedCrop by remember { mutableStateOf<CropForIrrigation?>(sampleCropsForIrrigation.firstOrNull()) }
    val schedules = remember {
        mutableStateListOf(
            // Sample schedules - In a real app, this would be loaded from a persistent source
            IrrigationScheduleItem(
                cropId = "tomato",
                stage = "Fruiting",
                frequencyDays = 2,
                durationMinutes = 30,
                waterAmount = "Deep watering",
                nextWateringDate = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000L),
                alertEnabled = true,
                notes = "Ensure soil is moist but not waterlogged."
            ),
            IrrigationScheduleItem(
                cropId = "tomato",
                stage = "Vegetative",
                frequencyDays = 3,
                durationMinutes = 20,
                waterAmount = "Moderate",
                nextWateringDate = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L),
                alertEnabled = false
            ),
            IrrigationScheduleItem(
                cropId = "maize",
                stage = "Tasseling",
                frequencyDays = 1,
                durationMinutes = 45,
                waterAmount = "Heavy, ensure good drainage",
                nextWateringDate = System.currentTimeMillis() + (1 * 24 * 60 * 60 * 1000L),
                alertEnabled = true,
                notes = "Critical stage for water."
            )
        )
    }
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<IrrigationScheduleItem?>(null) }


    // --- Derived State Calculations ---
    val filteredSchedules = remember(selectedCrop, schedules.toList()) { // Observe schedules.toList() for changes
        schedules.filter { it.cropId == selectedCrop?.id }
    }

    val upcomingWaterings = remember(schedules.toList()) { // Observe schedules.toList() for changes
        schedules
            .filter { it.nextWateringDate >= System.currentTimeMillis() }
            .sortedBy { it.nextWateringDate }
            .take(3) // Show next 3
            .mapNotNull { schedule ->
                val crop = sampleCropsForIrrigation.find { it.id == schedule.cropId }
                crop?.let {
                    UpcomingWateringEvent(
                        cropName = it.name,
                        stage = schedule.stage,
                        wateringTime = schedule.nextWateringDate,
                        scheduleId = schedule.id
                    )
                }
            }
    }


    // --- UI Logic ---
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("💧 Irrigation Plan & Schedule") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            if (selectedCrop != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingSchedule = null // Clear for new schedule
                        showAddScheduleDialog = true
                    },
                    icon = { Icon(Icons.Filled.AddAlarm, "Add Schedule") },
                    text = { Text("Add Schedule") }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp) // Space for FAB
        ) {
            // --- Crop Selection ---
            item {
                Text("Select Crop to Manage:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sampleCropsForIrrigation) { crop ->
                        CropChip(
                            crop = crop,
                            isSelected = selectedCrop?.id == crop.id,
                            onClick = { selectedCrop = crop }
                        )
                    }
                }
            }

            // --- Upcoming Waterings Section ---
            if (upcomingWaterings.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Upcoming Waterings (Next 3):", style = MaterialTheme.typography.titleMedium)
                }
                items(upcomingWaterings) { event ->
                    UpcomingWateringCard(event = event, onMarkAsDone = { scheduleId ->
                        val scheduleIndex = schedules.indexOfFirst { it.id == scheduleId }
                        if (scheduleIndex != -1) {
                            val oldSchedule = schedules[scheduleIndex]
                            val newNextWateringDate = System.currentTimeMillis() + (oldSchedule.frequencyDays * 24 * 60 * 60 * 1000L)
                            schedules[scheduleIndex] = oldSchedule.copy(nextWateringDate = newNextWateringDate)
                            // TODO: In a real app with ViewModel and alerts, you would also reschedule the alert here.
                        }
                    })
                }
            }


            // --- Schedules for Selected Crop ---
            selectedCrop?.let { crop ->
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Schedule for ${crop.name}:",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (filteredSchedules.isNotEmpty()) {
                    items(filteredSchedules) { schedule ->
                        IrrigationScheduleCard(
                            schedule = schedule,
                            onEdit = {
                                editingSchedule = schedule
                                showAddScheduleDialog = true
                            },
                            onToggleAlert = { enabled ->
                                val scheduleIndex = schedules.indexOfFirst { it.id == schedule.id }
                                if (scheduleIndex != -1) {
                                    schedules[scheduleIndex] = schedules[scheduleIndex].copy(alertEnabled = enabled)
                                    // TODO: In a real app with alerts, schedule/cancel the system alert here.
                                    // For now, this just updates the UI state.
                                }
                            },
                            onDelete = {
                                schedules.removeAll { it.id == schedule.id }
                                // TODO: In a real app with alerts, cancel the system alert here.
                            }
                        )
                    }
                } else {
                    item {
                        Text(
                            "No irrigation schedules found for ${crop.name}. Tap 'Add Schedule' to create one.",
                            modifier = Modifier.padding(vertical = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showAddScheduleDialog && selectedCrop != null) {
        AddEditScheduleDialog(
            scheduleToEdit = editingSchedule,
            cropId = selectedCrop!!.id, // selectedCrop is non-null here due to the check
            onDismiss = { showAddScheduleDialog = false },
            onSave = { newSchedule ->
                val existingIndex = schedules.indexOfFirst { it.id == newSchedule.id }
                if (existingIndex != -1) {
                    schedules[existingIndex] = newSchedule // Update existing
                } else {
                    schedules.add(newSchedule) // Add new
                }
                showAddScheduleDialog = false
                editingSchedule = null
                // TODO: In a real app with alerts, schedule/update the system alert here.
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropChip(crop: CropForIrrigation, isSelected: Boolean, onClick: () -> Unit) {
    ElevatedFilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(crop.name) },
        leadingIcon = crop.icon,
        colors = FilterChipDefaults.elevatedFilterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = FilterChipDefaults.elevatedFilterChipElevation(elevation = if (isSelected) 2.dp else 1.dp)
    )
}

@Composable
fun IrrigationScheduleCard(
    schedule: IrrigationScheduleItem,
    onEdit: () -> Unit,
    onToggleAlert: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(schedule.stage, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            InfoRow(icon = Icons.Default.Schedule, label = "Frequency:", value = "Every ${schedule.frequencyDays} days")
            InfoRow(icon = Icons.Default.Timer, label = "Duration:", value = "${schedule.durationMinutes} minutes")
            InfoRow(icon = Icons.Default.WaterDrop, label = "Amount:", value = schedule.waterAmount)
            InfoRow(
                icon = Icons.Default.Event,
                label = "Next Watering:",
                value = SimpleDateFormat("EEE, MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()).format(Date(schedule.nextWateringDate))
            )
            schedule.notes?.let {
                InfoRow(icon = Icons.Default.Notes, label = "Notes:", value = it, isNote = true)
            }

            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onToggleAlert(!schedule.alertEnabled) }) {
                    Checkbox(
                        checked = schedule.alertEnabled,
                        onCheckedChange = null // Controlled by Row click
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Enable Alert", style = MaterialTheme.typography.bodyMedium)
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Schedule", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete Schedule", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingWateringCard(event: UpcomingWateringEvent, onMarkAsDone: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${event.cropName} (${event.stage})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(event.wateringTime)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { onMarkAsDone(event.scheduleId) },
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp), // Minimal padding
                modifier = Modifier.size(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Mark as done", tint = MaterialTheme.colorScheme.onTertiary)
            }
        }
    }
}


@Composable
fun InfoRow(icon: ImageVector, label: String, value: String, isNote: Boolean = false) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = if (isNote) Alignment.Top else Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(10.dp))
        Text(
            "$label ",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScheduleDialog(
    scheduleToEdit: IrrigationScheduleItem?,
    cropId: String,
    onDismiss: () -> Unit,
    onSave: (IrrigationScheduleItem) -> Unit
) {
    var stage by remember { mutableStateOf(scheduleToEdit?.stage ?: "") }
    var frequencyDays by remember { mutableStateOf(scheduleToEdit?.frequencyDays?.toString() ?: "3") }
    var durationMinutes by remember { mutableStateOf(scheduleToEdit?.durationMinutes?.toString() ?: "20") }
    var waterAmount by remember { mutableStateOf(scheduleToEdit?.waterAmount ?: "") }
    var notes by remember { mutableStateOf(scheduleToEdit?.notes ?: "") }
    var alertEnabled by remember { mutableStateOf(scheduleToEdit?.alertEnabled ?: true) }

    // State for DatePicker
    val mContext = LocalContext.current
    val calendar = Calendar.getInstance()
    if (scheduleToEdit != null) {
        calendar.timeInMillis = scheduleToEdit.nextWateringDate
    } else {
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Default to tomorrow
    }

    var selectedDateMillis by remember { mutableStateOf(calendar.timeInMillis) }

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        mContext,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
            val cal = Calendar.getInstance()
            cal.set(selectedYear, selectedMonth, selectedDayOfMonth)
            // Preserve time if editing, or set a default time (e.g., 8 AM)
            val existingCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
            cal.set(Calendar.HOUR_OF_DAY, existingCal.get(Calendar.HOUR_OF_DAY)) // Keep original hour
            cal.set(Calendar.MINUTE, existingCal.get(Calendar.MINUTE))         // Keep original minute
            selectedDateMillis = cal.timeInMillis
        }, year, month, day
    )
    datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000 // Allow today


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (scheduleToEdit == null) "Add New Schedule" else "Edit Schedule for ${sampleCropsForIrrigation.find{it.id == cropId}?.name ?: ""}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = stage, onValueChange = { stage = it }, label = { Text("Growth Stage (e.g., Seedling)") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                    OutlinedTextField(
                        value = frequencyDays,
                        onValueChange = { if (it.length <= 2) frequencyDays = it.filter { char -> char.isDigit() } },
                        label = { Text("Freq (days)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = durationMinutes,
                        onValueChange = { if (it.length <= 3) durationMinutes = it.filter { char -> char.isDigit() } },
                        label = { Text("Duration (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(value = waterAmount, onValueChange = { waterAmount = it }, label = { Text("Water Amount (e.g., 2 inches)") }, singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.heightIn(min = 80.dp))

                Spacer(Modifier.height(8.dp))
                Text("Next Watering Date & Time:", style = MaterialTheme.typography.labelLarge)
                Button(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
                    Text(SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDateMillis)))
                    // TODO: Add a TimePicker as well for more precise scheduling
                }
                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { alertEnabled = !alertEnabled }) {
                    Checkbox(checked = alertEnabled, onCheckedChange = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Enable Alert")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val freq = frequencyDays.toIntOrNull() ?: 3
                    val dur = durationMinutes.toIntOrNull() ?: 20
                    val newSchedule = IrrigationScheduleItem(
                        id = scheduleToEdit?.id ?: UUID.randomUUID().toString(),
                        cropId = cropId,
                        stage = stage.trim(),
                        frequencyDays = freq,
                        durationMinutes = dur,
                        waterAmount = waterAmount.trim(),
                        nextWateringDate = selectedDateMillis,
                        alertEnabled = alertEnabled,
                        notes = notes.trim().takeIf { it.isNotBlank() }
                    )
                    onSave(newSchedule)
                },
                enabled = stage.isNotBlank() && frequencyDays.isNotBlank() && durationMinutes.isNotBlank() && waterAmount.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

