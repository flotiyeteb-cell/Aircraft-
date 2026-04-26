package com.aircraftwar

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // ⚠️ TEST CRASH (à enlever après test)
        // val test: String? = null
        // test!!.length

        // Créer GameView
        gameView = GameView(this)
        setContentView(gameView)
    }

    override fun onPause() {
        super.onPause()
        if (::gameView.isInitialized) {
            gameView.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::gameView.isInitialized) {
            gameView.resume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::gameView.isInitialized) {
            gameView.cleanup()
        }
    }
}
