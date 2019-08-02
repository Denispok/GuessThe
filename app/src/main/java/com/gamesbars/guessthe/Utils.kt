package com.gamesbars.guessthe

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest

fun AppCompatActivity.hideSystemUI() {
    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
}

fun AppCompatActivity.toast(message: String?, length: Int = Toast.LENGTH_LONG) =
    Toast.makeText(this, message ?: "unknown error", length).show()

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

fun getDrawableResIdByName(context: Context, name: String): Int {
    return context.resources.getIdentifier(name, "drawable", context.packageName)
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
