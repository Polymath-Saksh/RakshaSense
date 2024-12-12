package com.example.rakshasensev1

import android.Manifest
import android.content.Context
//import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.rakshasensev1.ui.theme.RakshaSenseV1Theme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null
    private var accelerometer: Sensor? = null

    // Sensor data
    private var xGyro by mutableFloatStateOf(0f)
    private var yGyro by mutableFloatStateOf(0f)
    private var zGyro by mutableFloatStateOf(0f)
    private var xAccel by mutableFloatStateOf(0f)
    private var yAccel by mutableFloatStateOf(0f)
    private var zAccel by mutableFloatStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        requestPermissions()
        setupSensors()

        setContent {
            RakshaSenseV1Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) { // Background set to white
                    SensorValuesDisplayHandler()
                }
            }
        }
    }

    private fun setupSensors() {
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        gyroscope?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        accelerometer?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
                xGyro = event.values[0]
                yGyro = event.values[1]
                zGyro = event.values[2]
            }
            Sensor.TYPE_ACCELEROMETER -> {
                xAccel = event.values[0]
                yAccel = event.values[1]
                zAccel = event.values[2]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
    }

    @Composable
    fun SensorValuesDisplayHandler() {
        val accXBuffer = remember { mutableStateListOf<Float>() }
        val accYBuffer = remember { mutableStateListOf<Float>() }
        val accZBuffer = remember { mutableStateListOf<Float>() }
        val gyroXBuffer = remember { mutableStateListOf<Float>() }
        val gyroYBuffer = remember { mutableStateListOf<Float>() }
        val gyroZBuffer = remember { mutableStateListOf<Float>() }

        // Buffer size for 10 seconds of data at 100Hz sampling rate (1000 samples)
        val bufferSize = 1000

        LaunchedEffect(Unit) {
            while (true) {
                delay(10L) // Assume 100Hz update frequency

                // Update buffers for accelerometer
                if (accXBuffer.size >= bufferSize) accXBuffer.removeAt(0)
                if (accYBuffer.size >= bufferSize) accYBuffer.removeAt(0)
                if (accZBuffer.size >= bufferSize) accZBuffer.removeAt(0)
                accXBuffer.add(xAccel)
                accYBuffer.add(yAccel)
                accZBuffer.add(zAccel)

                // Update buffers for gyroscope
                if (gyroXBuffer.size >= bufferSize) gyroXBuffer.removeAt(0)
                if (gyroYBuffer.size >= bufferSize) gyroYBuffer.removeAt(0)
                if (gyroZBuffer.size >= bufferSize) gyroZBuffer.removeAt(0)
                gyroXBuffer.add(xGyro)
                gyroYBuffer.add(yGyro)
                gyroZBuffer.add(zGyro)
            }
        }

        SensorValuesDisplay(
            accXData = accXBuffer,
            accYData = accYBuffer,
            accZData = accZBuffer,
            gyroXData = gyroXBuffer,
            gyroYData = gyroYBuffer,
            gyroZData = gyroZBuffer
        )
    }

    @Composable
    fun SensorValuesDisplay(
        accXData: List<Float>,
        accYData: List<Float>,
        accZData: List<Float>,
        gyroXData: List<Float>,
        gyroYData: List<Float>,
        gyroZData: List<Float>
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text("Accelerometer - X (m/s²)", style = MaterialTheme.typography.headlineSmall)
            SingleLineTimeChart(accXData, Color.Red, "Acceleration X", 10f)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Accelerometer - Y (m/s²)", style = MaterialTheme.typography.headlineSmall)
            SingleLineTimeChart(accYData, Color.Green, "Acceleration Y", 10f)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Accelerometer - Z (m/s²)", style = MaterialTheme.typography.headlineSmall)
            SingleLineTimeChart(accZData, Color.Blue, "Acceleration Z", 10f)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Gyroscope - X (rad/s)", style = MaterialTheme.typography.headlineSmall)
            SingleLineTimeChart(gyroXData, Color.Red, "Gyroscope X", 10f)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Gyroscope - Y (rad/s)", style = MaterialTheme.typography.headlineSmall)
            SingleLineTimeChart(gyroYData, Color.Green, "Gyroscope Y", 10f)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Gyroscope - Z (rad/s)", style = MaterialTheme.typography.headlineSmall)
            SingleLineTimeChart(gyroZData, Color.Blue, "Gyroscope Z", 10f)
        }
    }

    @Composable
    fun SingleLineTimeChart(
        data: List<Float>,
        color: Color,
        label: String,
        maxTime: Float
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(8.dp)
        ) {
            val padding = 50f
            val graphWidth = size.width - padding * 2
            val graphHeight = size.height - padding * 2
            val maxDataValue = data.maxOrNull() ?: 1f
            val minDataValue = data.minOrNull() ?: 0f

            // Draw X and Y axes
            drawLine(
                color = Color.Black,
                start = Offset(padding, size.height - padding),
                end = Offset(size.width - padding, size.height - padding),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.Black,
                start = Offset(padding, size.height - padding),
                end = Offset(padding, padding),
                strokeWidth = 2f
            )

            // Scale data to graph dimensions
            val timeStep = graphWidth / maxTime
            val range = maxDataValue - minDataValue
            val scaleFactor = if (range == 0f) 1f else graphHeight / range

            data.forEachIndexed { index, value ->
                if (index < data.size - 1) {
                    val startX = padding + index * timeStep / 100
                    val startY = size.height - padding - (value - minDataValue) * scaleFactor
                    val endX = padding + (index + 1) * timeStep / 100
                    val endY = size.height - padding - (data[index + 1] - minDataValue) * scaleFactor

                    drawLine(
                        color = color,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }
}
