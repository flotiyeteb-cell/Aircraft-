package com.aircraftwar

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

class PowerUp(
    var x: Float,
    var y: Float,
    val type: String,
    val screenWidth: Int,
    val screenHeight: Int
) {
    var width = 30f
    var height = 30f
    var lifespan = 300
    var rotation = 0f
    
    fun update() {
        y += 2f
        rotation += 5f
        lifespan--
    }

    fun isAlive(): Boolean {
        return lifespan > 0 && y < screenHeight
    }

    fun collidesWith(player: Player): Boolean {
        val dx = (x + width / 2) - (player.x + player.width / 2)
        val dy = (y + height / 2) - (player.y + player.height / 2)
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        return distance < (width / 2 + player.width / 2)
    }

    fun draw(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = when (type) {
            "shield" -> Color.parseColor("#00d4ff")
            "rapid_fire" -> Color.parseColor("#ff006e")
            "health" -> Color.parseColor("#00ff00")
            "slow" -> Color.parseColor("#ffbe0b")
            else -> Color.parseColor("#00d4ff")
        }
        
        canvas.save()
        canvas.rotate(rotation, x + width / 2, y + height / 2)
        canvas.drawRect(x, y, x + width, y + height, paint)
        canvas.restore()
        
        // Symbole
        paint.color = Color.WHITE
        paint.textSize = 20f
        val symbol = when (type) {
            "shield" -> "🛡"
            "rapid_fire" -> "⚡"
            "health" -> "❤"
            "slow" -> "❄"
            else -> "?"
        }
        canvas.drawText(symbol, x + width / 4, y + height / 2 + 5f, paint)
    }
}
