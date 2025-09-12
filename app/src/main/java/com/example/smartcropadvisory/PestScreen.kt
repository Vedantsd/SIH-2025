package com.example.smartcropadvisory

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import org.tensorflow.lite.Interpreter
import org.json.JSONObject
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PestScreen(navController: NavController) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var resultText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var detectedPest by remember { mutableStateOf<String?>(null) }
    var confidence by remember { mutableStateOf(0f) }
    var hasCameraPermission by remember { mutableStateOf(checkCameraPermission(context)) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            showPermissionRationale = true
        } else {
            showPermissionRationale = false
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { capturedBitmap ->
        bitmap = capturedBitmap
        if (capturedBitmap != null) {
            isProcessing = true
            detectedPest = null
            confidence = 0f
            classifyImage(context, capturedBitmap) { pest, conf ->
                detectedPest = pest
                confidence = conf
                isProcessing = false
            }
        } else {
            resultText = "No image captured"
            isProcessing = false
        }
    }

    // Function to handle camera button click
    val handleCameraClick = {
        when {
            hasCameraPermission -> {
                cameraLauncher.launch(null)
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Pest & Disease Detection",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Permission Warning Card (shown when permission is denied)
            if (!hasCameraPermission && showPermissionRationale) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = "Camera Permission Required",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Camera access is needed to capture images for pest and disease detection. Please grant camera permission to continue.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Instructions Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = "How to use:",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "• Take a clear photo of affected crop leaves\n• Ensure good lighting and focus\n• Hold camera steady for best results",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Image Display Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Captured crop image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = if (hasCameraPermission) "No image captured yet" else "Camera permission needed",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // Results Card
            if (bitmap != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Detection Results",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        when {
                            isProcessing -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 3.dp
                                    )
                                    Text(
                                        text = "Analyzing image...",
                                        modifier = Modifier.padding(start = 12.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            detectedPest != null -> {
                                if (confidence > 20) {
                                    // High confidence detection
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (detectedPest!!.contains("healthy", ignoreCase = true))
                                                MaterialTheme.colorScheme.tertiaryContainer
                                            else
                                                MaterialTheme.colorScheme.errorContainer
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (detectedPest!!.contains("healthy", ignoreCase = true)) "✅" else "🪲",
                                                    fontSize = 24.sp,
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = if (detectedPest!!.contains("healthy", ignoreCase = true))
                                                            "Healthy Crop Detected"
                                                        else
                                                            "Pest/Disease Detected",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = if (detectedPest!!.contains("healthy", ignoreCase = true))
                                                            MaterialTheme.colorScheme.onTertiaryContainer
                                                        else
                                                            MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                    Text(
                                                        text = detectedPest!!,
                                                        fontSize = 14.sp,
                                                        color = if (detectedPest!!.contains("healthy", ignoreCase = true))
                                                            MaterialTheme.colorScheme.onTertiaryContainer
                                                        else
                                                            MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            LinearProgressIndicator(
                                                progress = confidence / 100f,
                                                modifier = Modifier.fillMaxWidth(),
                                                color = if (detectedPest!!.contains("healthy", ignoreCase = true))
                                                    MaterialTheme.colorScheme.tertiary
                                                else
                                                    MaterialTheme.colorScheme.error
                                            )

                                            Text(
                                                text = "Confidence: ${String.format("%.1f", confidence)}%",
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(top = 4.dp),
                                                color = if (detectedPest!!.contains("healthy", ignoreCase = true))
                                                    MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                                else
                                                    MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                } else {
                                    // Low confidence
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "⚠️",
                                                fontSize = 32.sp
                                            )
                                            Text(
                                                text = "Unable to detect with confidence",
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "Try taking another photo with better lighting or focus",
                                                fontSize = 14.sp,
                                                textAlign = TextAlign.Center,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Capture Button
            Button(
                onClick = handleCameraClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isProcessing
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = when {
                        !hasCameraPermission -> "Grant Camera Permission"
                        bitmap == null -> "Capture Image"
                        else -> "Take Another Photo"
                    },
                    fontSize = 16.sp
                )
            }

            // Tips Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📸 Photography Tips",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• Focus on affected leaves or areas\n• Avoid shadows and reflections\n• Keep the camera steady\n• Take photos in natural daylight when possible",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

// -------------------- Helper Functions --------------------

// Check camera permission
private fun checkCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

// Load TFLite model
private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
    val fileDescriptor = context.assets.openFd(modelName)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    return fileChannel.map(
        FileChannel.MapMode.READ_ONLY,
        fileDescriptor.startOffset,
        fileDescriptor.declaredLength
    )
}

// Load labels from class_info.json
private fun loadLabels(context: Context, fileName: String = "class_info.json"): List<String> {
    return try {
        val jsonStr = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val jsonObj = JSONObject(jsonStr)
        val idxToClass = jsonObj.getJSONObject("idx_to_class")

        val labels = mutableListOf<String>()
        for (i in 0 until idxToClass.length()) {
            labels.add(idxToClass.getString(i.toString()))
        }
        labels
    } catch (e: Exception) {
        listOf("Unknown")
    }
}

// Run model inference - Modified to return only the highest prediction
private fun classifyImage(context: Context, bitmap: Bitmap, callback: (String?, Float) -> Unit) {
    try {
        val modelBuffer = loadModelFile(context, "pest_detector.tflite")
        val interpreter = Interpreter(modelBuffer)

        // Get input size dynamically
        val inputShape = interpreter.getInputTensor(0).shape() // [1, h, w, 3]
        val inputSize = inputShape[1]

        // Resize bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        // Prepare input buffer
        val inputBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputSize * inputSize)
        scaledBitmap.getPixels(
            intValues,
            0,
            scaledBitmap.width,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height
        )

        var pixelIndex = 0
        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val pixel = intValues[pixelIndex++]
                val r = (pixel shr 16 and 0xFF) / 255.0f
                val g = (pixel shr 8 and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f
                inputBuffer.putFloat(r)
                inputBuffer.putFloat(g)
                inputBuffer.putFloat(b)
            }
        }

        // Get output size dynamically
        val outputShape = interpreter.getOutputTensor(0).shape() // [1, numClasses]
        val numClasses = outputShape[1]
        val outputBuffer = Array(1) { FloatArray(numClasses) }

        // Run inference
        interpreter.run(inputBuffer, outputBuffer)

        // Load labels
        val labels = loadLabels(context)

        // Get highest prediction only
        val predictions = outputBuffer[0]
        val maxIndex = predictions.indices.maxByOrNull { predictions[it] } ?: -1
        val confidence = if (maxIndex != -1) predictions[maxIndex] * 100 else 0f

        val detectedClass = if (maxIndex != -1) {
            labels.getOrNull(maxIndex) ?: "Unknown"
        } else {
            null
        }

        callback(detectedClass, confidence)
        interpreter.close()

    } catch (e: IOException) {
        callback(null, 0f)
    } catch (e: Exception) {
        callback(null, 0f)
    }
}