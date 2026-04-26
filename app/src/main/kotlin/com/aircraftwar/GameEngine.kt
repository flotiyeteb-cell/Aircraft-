package com.aircraftwar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Vibrator
import android.util.Log
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameEngine(
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val context: Context
) {
    private lateinit var player: Player
    private val enemies = mutableListOf<Enemy>()
    private val bullets = mutableListOf<Bullet>()
    private val powerUps = mutableListOf<PowerUp>()
    private val particles = mutableListOf<Particle>()
    
    private var score = 0
    private var level = 1
    private var wave = 1
    private var waveEnemyCount = 5
    private var spawnCounter = 0
    private var gameRunning = true
    private var gamePaused = false
    private var gameOver = false
    private var gameTime = 0
    
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private val paint = Paint().apply {
        isAntiAlias = true
    }

    init {
        player = Player(screenWidth / 2f, screenHeight - 100f, screenWidth, screenHeight)
    }

    fun update() {
        if (!gameRunning || gamePaused) return

        gameTime++

        // Mise à jour du joueur
        player.update()

        // Mise à jour des balles
        bullets.removeAll { !it.isAlive() }
        bullets.forEach { it.update() }

        // Spawn des ennemis
        spawnCounter++
        val spawnThreshold = (100 - (level * 5)).coerceAtLeast(30)
        if (spawnCounter > spawnThreshold && enemies.size < waveEnemyCount + level) {
            spawnEnemy()
            spawnCounter = 0
        }

        // Mise à jour des ennemis
        enemies.removeAll { !it.isAlive() }
        enemies.forEach { enemy ->
            enemy.update(player)
            
            // Collision avec le joueur
            if (enemy.collidesWith(player)) {
                player.takeDamage(1)
                enemy.takeDamage(100f)
            }

            // Collision avec les balles
            bullets.forEach { bullet ->
                if (bullet.collidesWith(enemy.x + enemy.width / 2, enemy.y + enemy.height / 2, enemy.width / 2)) {
                    enemy.takeDamage(bullet.damage)
                    bullet.destroy()
                    score += (10 * level)
                    createParticles(bullet.x, bullet.y, 15, "#00ff00")
                }
            }
        }

        // Mise à jour des power-ups
        powerUps.removeAll { !it.isAlive() }
        powerUps.forEach { powerUp ->
            powerUp.update()
            if (powerUp.collidesWith(player)) {
                applyPowerUp(powerUp)
                powerUp.lifespan = 0
            }
        }

        // Mise à jour des particules
        particles.removeAll { !it.isAlive() }
        particles.forEach { it.update() }

        // Vérifier la fin de vague
        if (enemies.isEmpty() && spawnCounter > 100) {
            nextWave()
        }

        // Vérifier Game Over
        if (player.health <= 0) {
            endGame()
        }

        // Tir du joueur
        if (player.fire()) {
            createBulletFromPlayer()
        }
    }

    private fun spawnEnemy() {
        val type = when (Random.nextInt(0, 5)) {
            0 -> "scout"
            1 -> "tank"
            2 -> "shooter"
            3 -> "boss"
            else -> "scout"
        }
        val x = Random.nextFloat() * (screenWidth - 50)
        val y = Random.nextFloat() * 200f
        enemies.add(Enemy(x, y, screenWidth, screenHeight, type, level))
    }

    private fun nextWave() {
        wave++
        level = (wave / 3) + 1
        waveEnemyCount = 5 + (wave * 2)
        createParticles(screenWidth / 2f, screenHeight / 2f, 50f, "#00ff00")
    }

    private fun applyPowerUp(powerUp: PowerUp) {
        when (powerUp.type) {
            "shield" -> {
                player.shield = true
                player.shieldTime = 300
            }
            "rapid_fire" -> {
                player.rapidFireTime = 300
            }
            "health" -> {
                player.health = minOf(player.maxHealth, player.health + 1)
            }
            "slow" -> {
                enemies.forEach { it.speedMultiplier = 0.5f }
                player.slowTime = 200
            }
        }
        vibrate(50L)
        createParticles(powerUp.x, powerUp.y, 30f, "#ffff00")
    }

    fun onTouchDown(x: Float, y: Float) {
        player.targetX = x
        player.targetY = y
    }

    fun onTouchMove(x: Float, y: Float) {
        player.targetX = x
        player.targetY = y
    }

    fun onTouchUp() {
        // Continuer à se déplacer vers la dernière position
    }

    fun draw(canvas: Canvas) {
        // Fond dégradé
        drawBackground(canvas)

        // Dessiner les ennemis
        enemies.forEach { it.draw(canvas, paint) }

        // Dessiner les balles
        bullets.forEach { it.draw(canvas, paint) }

        // Dessiner les power-ups
        powerUps.forEach { it.draw(canvas, paint) }

        // Dessiner les particules
        particles.forEach { it.draw(canvas, paint) }

        // Dessiner le joueur
        player.draw(canvas, paint)

        // Dessiner l'HUD
        drawHUD(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        paint.color = Color.parseColor("#0a0e27")
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), paint)
        
        paint.color = Color.parseColor("#1a1f3a")
        for (i in 0 until 3) {
            canvas.drawRect(0f, i * 100f, screenWidth.toFloat(), (i + 1) * 100f, paint)
        }
    }

    private fun drawHUD(canvas: Canvas) {
        paint.color = Color.WHITE
        paint.textSize = 40f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("SCORE: $score", 20f, 60f, paint)
        canvas.drawText("LEVEL: $level", 20f, 120f, paint)
        canvas.drawText("WAVE: $wave", 20f, 180f, paint)
        
        // Afficher les vies
        var healthText = "VIES: "
        for (i in 0 until player.health) {
            healthText += "♥ "
        }
        paint.textSize = 30f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(healthText, screenWidth - 20f, 60f, paint)

        if (gamePaused) {
            paint.color = Color.parseColor("#ff006e")
            paint.textSize = 80f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("PAUSE", screenWidth / 2f, screenHeight / 2f, paint)
        }

        if (gameOver) {
            paint.color = Color.parseColor("#ff5555")
            paint.textSize = 80f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("GAME OVER", screenWidth / 2f, screenHeight / 2f, paint)
            paint.textSize = 40f
            canvas.drawText("Score Final: $score", screenWidth / 2f, screenHeight / 2f + 100f, paint)
        }
    }

    private fun createBulletFromPlayer() {
        val startX = player.x + player.width / 2
        val startY = player.y + player.height / 4
        
        val targetX = player.targetX
        val targetY = player.targetY
        
        val angle = atan2(targetY - startY, targetX - startX)
        bullets.add(Bullet(startX, startY, angle, screenWidth, screenHeight))
        vibrate(30L)
    }

    fun createBullet(x: Float, y: Float, targetX: Float, targetY: Float) {
        val angle = atan2(targetY - y, targetX - x)
        bullets.add(Bullet(x, y, angle, screenWidth, screenHeight))
    }

        private fun createParticles(x: Float, y: Float, count: Float, color: String) {
        repeat(count.toInt()) {
            val angle = Random.nextFloat() * 6.28f
            val speed = Random.nextFloat() * 5f + 2f
            particles.add(Particle(
                x, y,
                cos(angle) * speed,
                sin(angle) * speed,
                color,
                Random.nextInt(5, 15).toFloat()  // ✅ AJOUT .toFloat()
            ))
        }
        }

    fun addScore(amount: Int) {
        score += amount
    }

    fun addEnemy(enemy: Enemy) {
        enemies.add(enemy)
    }

    fun addPowerUp(x: Float, y: Float, type: String) {
        powerUps.add(PowerUp(x, y, type, screenWidth, screenHeight))
    }

    fun vibrate(duration: Long) {
        try {
            vibrator.vibrate(duration)
        } catch (e: Exception) {
            Log.e("GameEngine", "Vibration error: ${e.message}")
        }
    }

    fun pauseGame() {
        gamePaused = true
    }

    fun resumeGame() {
        gamePaused = false
    }

    fun endGame() {
        gameOver = true
        gameRunning = false
    }

    fun getScore() = score
    fun getLevel() = level
    fun getWave() = wave
    fun isGameOver() = gameOver
    fun getGameTime() = gameTime
    fun getEnemiesKilled() = score / 10
}
