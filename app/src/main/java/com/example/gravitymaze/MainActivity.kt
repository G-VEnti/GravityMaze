package com.example.gravitymaze

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.gravitymaze.ui.theme.GravityMazeTheme

class MainActivity : ComponentActivity(), SensorEventListener {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        val sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensorAcc != null) {

            sensorManager.registerListener(this, sensorAcc, SensorManager.SENSOR_DELAY_NORMAL)
        }


        enableEdgeToEdge()
        setContent {
            GravityMazeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MazeSetUp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelerometerX = event.values[0]
                accelerometerY = event.values[1]
            }
        }
    }
}
@Composable
fun MazeScreen(modifier: Modifier) {


}

