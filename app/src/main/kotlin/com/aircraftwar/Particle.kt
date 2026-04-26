package com.aircraftwar

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: String,
    var size: Float
) {
    var life = 1.0f
    var maxLife = 1.0f
    val gravity = 0.1f
    val friction = 0.99f

    fun update() {
        vx *= friction
        vy *= friction
        vy += gravity
        x += vx
        y += vy
        life -= 0.02f
    }

    fun isAlive(): Boolean {
        return life > 0
    }

    fun draw(canvas: Canvas, paint: Paint) {
        paint.alpha = (life * 255).toInt()
        paint.color = Color.parseColor(color)
        canvas.drawCircle(x, y, size, paint)
        paint.alpha = 255
    }
}
