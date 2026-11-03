package com.example.gravitymaze

import android.R
import android.content.res.Resources
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.SystemClock
import android.view.Display
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gravitymaze.ui.theme.GravityMazeTheme
import kotlinx.coroutines.delay

val mazeGrid = arrayOf(
    intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    intArrayOf(1, 2, 0, 0, 1, 0, 0, 0, 0, 1),
    intArrayOf(1, 0, 1, 0, 1, 0, 1, 1, 0, 1),
    intArrayOf(1, 0, 1, 0, 0, 0, 1, 0, 0, 1),
    intArrayOf(1, 0, 1, 1, 1, 1, 1, 0, 1, 1),
    intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 1),
    intArrayOf(1, 1, 1, 1, 1, 0, 1, 1, 0, 1),
    intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 1),
    intArrayOf(1, 0, 1, 0, 0, 0, 1, 1, 2, 1),
    intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
)

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    private var xA by mutableFloatStateOf(0f)
    private var yA by mutableStateOf(0f)
    private var zA by mutableStateOf(0f)

    private var xG by mutableFloatStateOf(0f)
    private var yG by mutableStateOf(0f)
    private var zG by mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensorAcc != null) {
            sensorManager.registerListener(this, sensorAcc, SensorManager.SENSOR_DELAY_NORMAL)
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (sensorGyro != null) {
            sensorManager.registerListener(this, sensorGyro, SensorManager.SENSOR_DELAY_NORMAL)
        }

        enableEdgeToEdge()
        setContent {
            GravityMazeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    SensorScreen(
//                        modifier = Modifier.padding(innerPadding),
//                        xA,
//                        yA,
//                        zA,
//                        xG,
//                        yG,
//                        zG
//                    )
//                    ShowTime()
//                    SimpleGame()
                    CustomCanvas(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                xA = event.values[0]
                yA = event.values[1]
                zA = event.values[2]
            }

            Sensor.TYPE_GYROSCOPE -> {
                xG = event.values[0]
                yG = event.values[1]
                zG = event.values[2]
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GravityMazeTheme {

    }
}

//-------------------------------------

@Composable
fun SensorScreen(modifier: Modifier, xA: Float, yA: Float, zA: Float, xG: Float, yG: Float, zG: Float) {
    Column(modifier = Modifier.padding(16.dp)) {
        TextField(value = xA.toString(), onValueChange = {}, label = { Text("XA") }, readOnly = true)
        TextField(value = yA.toString(), onValueChange = {}, label = { Text("YA") }, readOnly = true)
        TextField(value = zA.toString(), onValueChange = {}, label = { Text("ZA") }, readOnly = true)

        Spacer(modifier = Modifier.padding(30.dp))

        TextField(value = xG.toString(), onValueChange = {}, label = { Text("XG") }, readOnly = true)
        TextField(value = yG.toString(), onValueChange = {}, label = { Text("YG") }, readOnly = true)
        TextField(value = zG.toString(), onValueChange = {}, label = { Text("ZG") }, readOnly = true)
    }
}


@Composable
fun CustomCanvas(modifier: Modifier) {

    Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) { // aspectRatio força el canvas a ocupar lo mateix entre adalt i abaix

            val squareSize = size.width / 10f;

            for (i in 0..9)
            {
                for (j in 0..9)
                {
                    drawRect(
                        topLeft = Offset(x = j * squareSize, y = i * squareSize),
                        color = if (mazeGrid[i][j] == 0) Color.White
                        else if (mazeGrid[i][j] == 2) Color.Red
                        else Color.Blue,
                        size = Size(squareSize, squareSize)
                    )
                }
            }

            val circleSize = size.width / 10f;

            drawCircle(color = Color.Yellow, radius = circleSize / 2,
                       center = Offset(size.width * .15f, size.height * .15f))
        }
    }
}

@Composable
fun ShowTime() {
    val startTime = remember { SystemClock.elapsedRealtime() }
    var elapsed by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            elapsed = SystemClock.elapsedRealtime() - startTime
        }
    }

    Text("Time: ${elapsed}")
}

@Composable
fun SimpleGame() {
    var circleX by remember { mutableStateOf(200f) }
    var speedX by remember { mutableStateOf(0f) }

    var moveLeft by remember { mutableStateOf(false) }
    var moveRight by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            speedX = 0f
            if (moveLeft) {
                speedX = -5f
            }
            if (moveRight) {
                speedX = 5f
            }
            circleX += speedX
        }
    }

    Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
        detectTapGestures { offset ->
            val w = size.width
            moveRight = false
            moveLeft = false
            if (offset.x < w / 3) {
                moveLeft = true
            } else if (offset.x > 2 * w / 3) {
                moveRight = true
            }
        }
    }
    ){
        drawCircle(color = Color.Green, radius = 40f,
            center = Offset(circleX, size.height / 2))
    }
}