package com.aircraftwar

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Enemy(
    var x: Float,
    var y: Float,
    val screenWidth: Int,
    val screenHeight: Int,
    val type: String = "scout",
    val level: Int = 1
) {
    var width = 35f
    var height = 35f
    var health = 1f
    var maxHealth = 1f
    var speed = 2f
    var speedMultiplier = 1f
    var lifespan = 5000
    
    var vx = 0f
    var vy = 0f
    var angle = 0f
    
    var fireCounter = 0
    var fireRate = 60
    
    init {
        when (type) {
            "scout" -> {
                maxHealth = 1f
                health = 1f
                speed = 3f
                width = 30f
                height = 30f
                fireRate = 100
            }
            "tank" -> {
                maxHealth = 5f
                health = 5f
                speed = 1.5f
                width = 50f
                height = 50f
                fireRate = 80
            }
            "shooter" -> {
                maxHealth = 2f
                health = 2f
                speed = 2.5f
                width = 35f
                height = 35f
                fireRate = 40
            }
            "boss" -> {
                maxHealth = 20f
                health = 20f
                speed = 1f
                width = 80f
                height = 80f
                fireRate = 30
            }
        }
        maxHealth += level
        health = maxHealth
        speed += level * 0.5f
    }

    fun update(player: Player) {
        // IA simple
        val dx = player.x - x
        val dy = player.y - y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        
        if (distance > 0) {
            vx = (dx / distance) * speed * speedMultiplier
            vy = (dy / distance) * speed * speedMultiplier
        }
        
        x += vx
        y += vy
        
        // Limites
        x = x.coerceIn(0f, (screenWidth - width))
        y = y.coerceIn(0f, (screenHeight - height))
        
        lifespan--
        fireCounter--
    }

    fun takeDamage(amount: Float) {
        health -= amount
    }

    fun isAlive(): Boolean {
        return health > 0 && lifespan > 0
    }

    fun collidesWith(other: Player): Boolean {
        val dx = (x + width / 2) - (other.x + other.width / 2)
        val dy = (y + height / 2) - (other.y + other.height / 2)
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        return distance < (width / 2 + other.width / 2)
    }

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = when (type) {
            "scout" -> Color.parseColor("#ff5555")
            "tank" -> Color.parseColor("#ff8800")
            "shooter" -> Color.parseColor("#ff00ff")
            "boss" -> Color.parseColor("#ffff00")
            else -> Color.parseColor("#ff5555")
        }
        
        paint.style = Paint.Style.FILL
        canvas.drawRect(x, y, x + width, y + height, paint)
        
        // Barre de vie
        paint.color = Color.RED
        val healthBarWidth = (width * health / maxHealth)
        canvas.drawRect(x, y - 10f, x + healthBarWidth, y - 5f, paint)
        
        paint.color = Color.GREEN
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(x, y - 10f, x + width, y - 5f, paint)
    }
}
