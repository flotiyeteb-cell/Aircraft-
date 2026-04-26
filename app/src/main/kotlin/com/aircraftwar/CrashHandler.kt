package com.aircraftwar

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {

        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val errorLog = sw.toString()

            val file = File(context.getExternalFilesDir(null), "crash_log.txt")
            file.appendText("\n\n=== CRASH ===\n$errorLog")

        } catch (e: Exception) {
            e.printStackTrace()
        }

        // ❌ NE PAS relancer killProcess immédiatement (cause reboot loop)
        try {
            Thread.sleep(1000)
        } catch (_: Exception) {}

        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
