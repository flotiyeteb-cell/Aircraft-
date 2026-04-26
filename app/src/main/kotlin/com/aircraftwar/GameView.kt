package com.aircraftwar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Vibrator
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.sqrt

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    
    private var gameThread: GameThread? = null
    private lateinit var gameEngine: GameEngine
    private var isRunning = true

    init {
        holder.addCallback(this)
        isFocusable = true
        setBackgroundColor(Color.BLACK)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        gameEngine = GameEngine(width, height, context as MainActivity)
        gameThread = GameThread(holder, this).apply {
            start()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        while (retry) {
            try {
                gameThread?.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    gameEngine.onTouchDown(event.x, event.y)
                }
                MotionEvent.ACTION_MOVE -> {
                    gameEngine.onTouchMove(event.x, event.y)
                }
                MotionEvent.ACTION_UP -> {
                    gameEngine.onTouchUp()
                }
            }
        }
        return true
    }

    fun drawGame(canvas: Canvas) {
        canvas.drawColor(Color.BLACK, PorterDuff.Mode.SRC)
        gameEngine.draw(canvas)
    }

    fun updateGame() {
        gameEngine.update()
    }

    fun pause() {
        gameEngine.pauseGame()
    }

    fun resume() {
        gameEngine.resumeGame()
    }

    fun cleanup() {
        isRunning = false
        gameThread?.interrupt()
    }

    private inner class GameThread(
        private val holder: SurfaceHolder,
        private val gameView: GameView
    ) : Thread() {

        override fun run() {
            while (isRunning) {
                var canvas: Canvas? = null
                try {
                    canvas = holder.lockCanvas()
                    if (canvas != null) {
                        synchronized(holder) {
                            gameView.updateGame()
                            gameView.drawGame(canvas)
                        }
                    }
                    Thread.sleep(16) // ~60 FPS
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas)
                    }
                }
            }
        }
    }
}
