package com.example.gravitymaze

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gravitymaze.ui.theme.GravityMazeTheme
import kotlinx.coroutines.delay

var btnColor = 0xFF999999

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
            val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController, "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                onBtnClick = {
                                    game.restart()
                                    navController.navigate("mazeScreen")
                                },
                                tutoOnClick = {navController.navigate("tutoScreen")},
                            )
                        }
                        composable("tutoScreen") {
                            TutorialScreen(
                                backOnClick = {navController.popBackStack()},
                                startOnClick = {navController.navigate("mazeScreen")},
                            )
                        }
                        composable("mazeScreen"){
                            MazeScreen(
                                backOnClick = {
                                    navController.navigate("home") {
                                        popUpTo("home") {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                },
                                onFinishClick = {timer -> navController.navigate("gameOver/$timer")},
                                game,
                                aX,
                                aY,
                                modifier = Modifier.padding(innerPadding))
                        }
                        composable("gameOver/{timer}") { backStackEntry ->
                            val timer = backStackEntry.arguments?.getString("timer")?: "00.00"
                            GameOverScreen(
                                finalTime = timer,
                                homeOnClick = {
                                    navController.navigate("home") {
                                        popUpTo("home") {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                },
                                restartOnClick = {
                                    game.restart()
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: error("No accelerometer available")
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
fun  HomeScreen(
    modifier: Modifier = Modifier,
    onBtnClick: () -> Unit,
    tutoOnClick: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to the Gravity Maze")

            LargeFloatingActionButton(
                onClick = onBtnClick,
                containerColor = Color(0xFFA2BD2A),
                modifier = Modifier.size(width = 200.dp, height = 80.dp)
            ) {
                Text("Start")
            }

            ExtendedFloatingActionButton(
                onClick = tutoOnClick,
                containerColor = Color(btnColor)
            ) {
                Text("Tutorial")
            }
        }
    }
}

@Composable
fun  TutorialScreen(
    modifier: Modifier = Modifier,
    backOnClick: () -> Unit,
    startOnClick: () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.spacedBy(150.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.padding(50.dp)) {
                Text(
                    text = "Tilt your phone to control the ball and try to reach the green square as fast as possible :)",
                    textAlign = TextAlign.Center)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = startOnClick,
                    containerColor = Color(0xFFA2BD2A)

                ) {
                    Text("Start")
                }
                ExtendedFloatingActionButton(
                    onClick = backOnClick,
                    containerColor = Color(btnColor)
                ) {
                    Text("Main menu")
                }
            }
        }
    }
}


@Composable
fun MazeScreen(backOnClick: () -> Unit,
               onFinishClick: (String) -> Unit,
               game : MazeGame,
               aX : Float,
               aY : Float,
               modifier : Modifier) {

    val currentAx by rememberUpdatedState(aX)
    val currentAy by rememberUpdatedState(aY)
    var ticks by remember { mutableStateOf(0) }
    var timer = android.text.format.DateUtils.formatElapsedTime((ticks/60).toLong())


    LaunchedEffect(Unit) {
        while (true) {
            delay(16L)
            ticks++
            game.UpdateMaze(0.016f, currentAx, currentAy)
        }
    }

    LaunchedEffect(game.won) {
        if (game.won) onFinishClick(timer)
    }

    Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(timer)

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

            Row(
                modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExtendedFloatingActionButton(
                    onClick = backOnClick,
                    containerColor = Color(btnColor)
                ) {
                    Text("Back")
                }

                // TEMPORAL BUTTON FOR TEST PURPOSE
                ExtendedFloatingActionButton(
                    onClick = { onFinishClick(timer) },
                    containerColor = Color(0xFF962D2D)
                ) {
                    Text("Finish")
                }
            }
        }
    }
}


@Composable
fun  GameOverScreen(
    modifier: Modifier = Modifier,
    homeOnClick: () -> Unit,
    restartOnClick: () -> Unit,
    finalTime: String
) {
    Box(modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Finished in $finalTime")
            Text("Do you want to play again?")
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = restartOnClick,
                    containerColor = Color(0xFFA2BD2A)

                ) {
                    Text("Restart")

                }
                ExtendedFloatingActionButton(
                    onClick = homeOnClick,
                    containerColor = Color(btnColor)
                ) {
                    Text("Main menu")
                }
            }
        }
    }
}


