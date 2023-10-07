package com.gamesbars.guessthe.data

import android.content.Context
import com.gamesbars.guessthe.App
import com.gamesbars.guessthe.util.RemoteConfig

object CoinsStorage {

    val resources get() = App.appContext.resources!!
    val saves get() = App.appContext.getSharedPreferences("saves", Context.MODE_PRIVATE)!!

    fun getCoins() = saves.getInt("coins", 0)

    fun addCoins(coinsToAdd: Int) {
        saves.edit().apply {
            putInt("coins", getCoins() + coinsToAdd)
            apply()
        }
    }

    fun getLevelReward(): Int {
        return RemoteConfig.getLevelReward()
    }

    fun getRateReward(): Int {
        return RemoteConfig.getRateReward()
    }
}