package com.aircraftwar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private var gameThread: GameThread? = null
    private var gameEngine: GameEngine? = null
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
        isRunning = false

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
        val engine = gameEngine ?: return true

        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> engine.onTouchDown(it.x, it.y)
                MotionEvent.ACTION_MOVE -> engine.onTouchMove(it.x, it.y)
                MotionEvent.ACTION_UP -> engine.onTouchUp()
            }
        }
        return true
    }

    fun drawGame(canvas: Canvas) {
        canvas.drawColor(Color.BLACK, PorterDuff.Mode.SRC)

        gameEngine?.draw(canvas)
    }

    fun updateGame() {
        gameEngine?.update()
    }

    fun pause() {
        gameEngine?.pauseGame()
    }

    fun resume() {
        gameEngine?.resumeGame()
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

                    sleep(16) // ~60 FPS

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
