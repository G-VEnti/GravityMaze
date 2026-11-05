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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.gravitymaze.ui.theme.GravityMazeTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var  sensorManager : SensorManager
    var aX by mutableFloatStateOf(0f)
    var aY by mutableFloatStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        //enableEdgeToEdge()
        setContent {
            val game = remember { MazeGame() }

            GravityMazeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MazeScreen(game, aX, aY, modifier = Modifier.padding(innerPadding))
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: error("No accelerometer")
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)

    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            aX = event.values[0]
            aY = event.values[1]
        }
    }


}


@Composable
fun MazeScreen(game : MazeGame, aX : Float, aY : Float, modifier : Modifier) {

    val currentAx by rememberUpdatedState(aX)
    val currentAy by rememberUpdatedState(aY)
    var ticks by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while(true) {
            delay(16L)
            ticks++
            game.UpdateMaze(0.016f, currentAx, currentAy)
        }
    }

    Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
        Canvas(modifier = modifier.fillMaxWidth().aspectRatio(1f)) {

            val cellWidth = 110f
            val cellHeight = 110f

            for (y in 0 until game.rows) {
                for (x in 0 until game.cols) {
                    val color = when (game.map[y][x]) {
                        1 -> Color.DarkGray
                        3 -> Color.Green
                        else -> Color.White
                    }
                    drawRect(
                        color = color,
                        topLeft = Offset(x * cellWidth, y * cellHeight),
                        size = Size(cellWidth, cellHeight)
                    )
                }
            }

            drawCircle(
                color = Color.Yellow,
                radius = game.ball.radius * cellWidth,
                center = Offset(
                    x = game.ball.position.x * cellWidth,
                    y = game.ball.position.y * cellHeight
                )
            )
        }
    }

}

