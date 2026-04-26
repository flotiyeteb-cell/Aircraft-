package com.aircraftwar

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.cos
import kotlin.math.sin

class Bullet(
    var x: Float,
    var y: Float,
    val angle: Float,
    val screenWidth: Int,
    val screenHeight: Int
) {
    var radius = 5f
    var speed = 10f
    var damage = 1f
    val color = Color.parseColor("#00d4ff")
    var isDestroyed = false
    
    var vx = cos(angle) * speed
    var vy = sin(angle) * speed

    fun update() {
        x += vx
        y += vy
    }

    fun isAlive(): Boolean {
        return !isDestroyed && x > -50 && x < screenWidth + 50 &&
               y > -50 && y < screenHeight + 50
    }

    fun destroy() {
        isDestroyed = true
    }

    fun collidesWith(x: Float, y: Float, radius: Float): Boolean {
        val dx = this.x - x
        val dy = this.y - y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        return distance < (this.radius + radius)
    }

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = color
        canvas.drawCircle(x, y, radius, paint)
        
        // Traînée
        paint.alpha = 128
        canvas.drawLine(x, y, x - vx * 3, y - vy * 3, paint)
        paint.alpha = 255
    }
}
