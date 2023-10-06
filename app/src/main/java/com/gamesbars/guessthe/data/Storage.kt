package com.gamesbars.guessthe.data

import android.annotation.SuppressLint
import android.content.Context
import com.gamesbars.guessthe.AnalyticsHelper
import com.gamesbars.guessthe.App
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.util.RemoteConfig

@SuppressLint("DiscouragedApi")
object Storage {

    val resources get() = App.appContext.resources!!
    val saves get() = App.appContext.getSharedPreferences("saves", Context.MODE_PRIVATE)!!

    fun getDrawableResIdByName(name: String): Int {
        return resources.getIdentifier(name, "drawable", App.appContext.packageName)
    }

    fun getWinImageResId(name: String): Int {
        val winDrawableId = getDrawableResIdByName(name + "_win")
        return if (winDrawableId == 0) getDrawableResIdByName(name)
        else winDrawableId
    }

    fun getStringResIdByName(name: String): Int {
        return resources.getIdentifier(name, "string", App.appContext.packageName)
    }

    fun getStringArrayResIdByName(name: String): Int {
        return resources.getIdentifier(name, "array", App.appContext.packageName)
    }

    fun getCurrentLevel(pack: String) = saves.getInt(pack, 1)

    fun getLevelName(pack: String, level: Int) = pack + "_" + level

    fun getLevelsRemainingToUnlock(pack: String): Int? {
        val packIndex = getPackIndex(pack)
        val levelsToUnlock = getPacksLevelsToUnlock()[packIndex]
        if (levelsToUnlock == -1) return null
        return levelsToUnlock - getCompletedLevelsCount()
    }

    fun getCompletedLevels(pack: String): Int {
        return saves.getInt(pack + "completed", 0)
    }

    fun getCompletedLevelsCount(): Int {
        val packs = resources.getStringArray(R.array.packs)
        return packs.fold(0) { completedLevels, pack ->
            completedLevels + getCompletedLevels(pack)
        }
    }

    fun getAuthorAndLicense(pack: String, level: Int): Pair<String, String> {
        val authorResId = getStringArrayResIdByName(pack + "_author")
        val author = resources.getStringArray(authorResId)[level - 1]

        val licenseResId = getStringArrayResIdByName(pack + "_license")
        val license = resources.getStringArray(licenseResId)[level - 1]

        return Pair(author, license)
    }

    fun isLevelHaveInfo(pack: String, level: Int): Boolean {
        return if (getStringArrayResIdByName(pack + "_author") == 0) false
        else getAuthorAndLicense(pack, level).first != "-"
    }

    fun getLevelCaption(pack: String, level: Int): String? {
        val levelCaptionsResId = getStringArrayResIdByName(pack + "_captions")
        if (levelCaptionsResId != 0) {
            val levelCaptions = resources.getStringArray(levelCaptionsResId)
            val levelCaption = levelCaptions[level - 1]
            if (levelCaption == "-") return null
            if (levelCaption != "*") return levelCaption
        }

        val packCaptionResId = getStringResIdByName(pack + "_caption")
        if (packCaptionResId != 0) {
            return resources.getString(packCaptionResId)
        }

        return null
    }

    fun isPackOpen(pack: String): Boolean {
        val packIndex = getPackIndex(pack)
        val levelsToUnlock = getPacksLevelsToUnlock()[packIndex]
        val isPackOpenedByLevels = levelsToUnlock != -1 && getCompletedLevelsCount() >= levelsToUnlock
        return isPackPurchased(pack) || isPackOpenedByLevels
    }

    fun isPackPurchased(pack: String) = saves.getBoolean(pack + "purchased", false)

    /** @return is level completed first time */
    fun completeLevel(pack: String): Boolean {
        val currentLevel = getCurrentLevel(pack)
        val packSize = getPackSize(pack)
        val levelName = getLevelName(pack, currentLevel)
        var isCompletedFirstTime = false

        val editor = saves.edit()
        editor.putString(levelName, saves.getString(levelName, "")!!.replace("!", "").replace("*", ""))
        if (currentLevel > getCompletedLevels(pack)) {
            editor.putInt(pack + "completed", currentLevel)
            CoinsStorage.addCoins(CoinsStorage.getLevelReward())
            isCompletedFirstTime = true
            AnalyticsHelper.logLevelComplete(pack, currentLevel)
        }
        if (currentLevel + 1 > packSize) editor.putInt(pack, 1)
        else editor.putInt(pack, currentLevel + 1)
        editor.apply()

        if (isCompletedFirstTime) checkUnlockedLevels()

        return isCompletedFirstTime
    }

    private fun checkUnlockedLevels() {
        val packs = resources.getStringArray(R.array.packs)
        val packsLevelsToUnlock = getPacksLevelsToUnlock()
        val completedLevelsCount = getCompletedLevelsCount()

        for (id in packs.indices) {
            val pack = packs[id]
            if (packsLevelsToUnlock[id] == completedLevelsCount && !isPackPurchased(pack)) {
                AnalyticsHelper.logPackUnlock(pack, "levels")
            }
        }
    }

    fun getPackSize(pack: String): Int {
        val packIndex = getPackIndex(pack)
        return resources.getIntArray(R.array.packs_sizes)[packIndex]
    }

    fun getPackPrice(pack: String): Int {
        val packIndex = getPackIndex(pack)
        return resources.getIntArray(R.array.packs_prices)[packIndex]
    }

    private fun getPackIndex(pack: String) = resources.getStringArray(R.array.packs).indexOf(pack)

    private fun getPacksLevelsToUnlock(): IntArray {
        return RemoteConfig.getPacksLevelsToUnlock()
    }
}
