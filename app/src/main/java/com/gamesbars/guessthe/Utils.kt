package com.gamesbars.guessthe

import android.app.ActivityManager
import android.app.Application
import android.app.Application.getProcessName
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Process
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.toast(message: String?, length: Int = Toast.LENGTH_LONG) =
    Toast.makeText(this, message ?: "unknown error", length).show()

fun Number.toPx(context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context.resources.displayMetrics
    )
}

fun logd(msg: Any?) = Log.d("DEBUG_TAG", msg.toString())

fun playSound(context: Context, resID: Int) {
    val saves = context.getSharedPreferences("saves", Context.MODE_PRIVATE)
    if (saves.getBoolean("sound", true)) {
        MediaPlayer.create(context, resID).apply {
            start()
            setOnCompletionListener { release() }
        }
    }
}

fun String.sliceUntilIndex(index: Int): String = this.run { slice(0..minOf(index, length - 1)) }

fun Application.isProcessMain(): Boolean {
    val processName = getProcessNameCompat() ?: return true
    return packageName == processName
}

fun Application.getProcessNameCompat(): String? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return getProcessName()
    }

    val pid = Process.myPid()
    val manager = getSystemService(Application.ACTIVITY_SERVICE) as ActivityManager
    for (processInfo in manager.runningAppProcesses) {
        if (processInfo.pid == pid) {
            return processInfo.processName
        }
    }

    return null
}