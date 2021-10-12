package com.gamesbars.guessthe

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest

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

fun hasConnection(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo?.isConnected ?: false
}

fun buildAdRequest(saves: SharedPreferences): AdRequest {
    val adBuilder = AdRequest.Builder()
    if (saves.getBoolean("npa", true)) {
        val extras = Bundle()
        extras.putString("npa", "1")
        adBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
    }
    return adBuilder.build()
}

fun String.sliceUntilIndex(index: Int): String = this.run { slice(0..minOf(index, length - 1)) }
