package com.aircraftwar

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Player(
    var x: Float,
    var y: Float,
    val screenWidth: Int,
    val screenHeight: Int
) {
    var width = 40f
    var height = 50f
    var speed = 8f
    
    var health = 3
    var maxHealth = 3
    var shield = false
    var shieldTime = 0
    var invincible = false
    var invincibleTime = 0
    
    var score = 0
    var combo = 0
    
    var targetX = x
    var targetY = y
    
    var rapidFireTime = 0
    var slowTime = 0
    
    var fireRate = 10
    var fireCounter = 0
    var weaponType = "basic"
    
    fun update() {
        // Mouvement vers la cible
        val dx = targetX - x
        val dy = targetY - y
        val distance = sqrt(dx * dx + dy * dy)
        
        if (distance > 10f) {
            val moveX = (dx / distance) * speed
            val moveY = (dy / distance) * speed
            x += moveX
            y += moveY
        }

        // Limites
        x = x.coerceIn(0f, (screenWidth - width))
        y = y.coerceIn(0f, (screenHeight - height))

        // Invincibilité
        if (invincible) {
            invincibleTime--
            if (invincibleTime <= 0) {
                invincible = false
            }
        }

        // Shield
        if (shield) {
            shieldTime--
            if (shieldTime <= 0) {
                shield = false
            }
        }

        // Rapid Fire
        if (rapidFireTime > 0) {
            rapidFireTime--
            fireRate = 5
        } else {
            fireRate = 10
        }

        fireCounter--
    }

    fun fire(): Boolean {
        if (fireCounter <= 0) {
            fireCounter = fireRate
            return true
        }
        return false
    }

    fun takeDamage(amount: Int) {
        if (invincible || shield) {
            shield = false
            return
        }
        health -= amount
        invincible = true
        invincibleTime = 60
    }

    fun addScore(amount: Int) {
        score += amount * (1 + combo / 10)
        combo++
    }

    fun draw(canvas: Canvas, paint: Paint) {
        // Shield
        if (shield) {
            paint.color = Color.parseColor("#00d4ff")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            canvas.drawCircle(x + width / 2, y + height / 2, width * 1.5f, paint)
        }

        // Invincibilité (clignotement)
        if (invincible && (invincibleTime / 5) % 2 == 0) {
            paint.alpha = 128
        } else {
            paint.alpha = 255
        }

        // Corps de l'avion
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#00d4ff")
        
        val centerX = x + width / 2
        val centerY = y + height / 2
        
        // Triangle avion
        val points = floatArrayOf(
            centerX, y,                          // Haut
            x + width, y + height,               // Bas droit
            centerX, y + height * 0.6f,          // Milieu
            x, y + height                        // Bas gauche
        )
        
        canvas.drawCircle(centerX, centerY, width / 2, paint)
        
        // Cockpit
        paint.color = Color.parseColor("#ffff00")
        canvas.drawCircle(centerX, y + height * 0.3f, 4f, paint)

        paint.alpha = 255
    }

    fun collidesWith(x: Float, y: Float, radius: Float): Boolean {
        val dx = (this.x + width / 2) - x
        val dy = (this.y + height / 2) - y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < (width / 2 + radius)
    }
}
