package com.gamesbars.guessthe.data

import android.content.Context
import com.gamesbars.guessthe.App
import com.gamesbars.guessthe.R

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
        return resources.getInteger(R.integer.level_reward)
    }    
    
    fun getRateReward(): Int {
        return resources.getInteger(R.integer.rate_reward)
    }
}