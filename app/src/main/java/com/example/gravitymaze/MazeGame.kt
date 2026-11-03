package com.example.gravitymaze

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import kotlin.math.floor

class MazeGame {

    val map = arrayOf(
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 2, 0, 0, 1, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 1, 0, 1, 0, 1, 1, 0, 1),
        intArrayOf(1, 0, 1, 0, 0, 0, 1, 0, 0, 1),
        intArrayOf(1, 0, 1, 1, 1, 1, 1, 0, 1, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 1, 1, 1, 1, 0, 1, 1, 0, 1),
        intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 1, 0, 0, 0, 1, 1, 3, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
    )

    class Ball(startX : Float, startY : Float) {

        val radius = 0.3f
        var position by mutableStateOf(Offset(startX,startY))
        var velocityX = 0f
        var velocityY = 0f

    }

    private companion object {
        const val TILT_STRENGTH = 12f
        const val FRICTION = .98f
        const val BOUNCE = .3f
        const val MAX_DT = .05f
    }

    val rows = map.size
    val cols = map[0].size
    val ball = createBallAtStart()
    var won by mutableStateOf(false)

    private fun createBallAtStart() : Ball {
        for (y in map.indices)
            for (x in map[0].indices) {
                if (map[y][x] == 2) return Ball(x + 0.5f, y + 0.5f)
            }
        error("Map has no ducking start cell")
    }

    private fun inBounds(x : Int, y : Int) = y in 0 until rows && x in 0 until cols

    fun isWall(x: Int, y: Int) = !inBounds(x, y) || map[y][x] == 1
    fun isEnd(x: Int, y: Int) = inBounds(x, y) && map[y][x] == 3

    private fun collides(colX: Float, colY: Float): Boolean {

        val r = ball.radius
        return isWall(floor(colX + r).toInt(), floor(colY + r).toInt()) ||
                isWall(floor(colX - r).toInt(), floor(colY - r).toInt()) ||
                isWall(floor(colX + r).toInt(), floor(colY - r).toInt()) ||
                isWall(floor(colX - r).toInt(), floor(colY + r).toInt())
    }

    fun UpdateMaze(dt : Float, aX : Float, aY : Float) {

        if (won) return
        val movement = dt.coerceAtMost(MAX_DT)

        ball.velocityX += -aX * TILT_STRENGTH * dt
        ball.velocityY += aY * TILT_STRENGTH * dt

        ball.velocityX *= FRICTION
        ball.velocityY *= FRICTION

        var x = ball.position.x
        var y = ball.position.y

        val newX = x + ball.velocityX * movement
        val newY = y + ball.velocityY * movement

        if (collides(newX, y)) ball.velocityX = -ball.velocityX * BOUNCE else x = newX
        if (collides(x, newY)) ball.velocityY = -ball.velocityY * BOUNCE else y = newY

        ball.position = Offset(x, y)

        if (isEnd(floor(x).toInt(), floor(y).toInt())) won = true

    }

}
