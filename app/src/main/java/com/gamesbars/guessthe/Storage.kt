package com.gamesbars.guessthe

import android.content.Context

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

    fun getStringArrayResIdByName(name: String): Int {
        return resources.getIdentifier(name, "array", App.appContext.packageName)
    }

    fun getCurrentLevel(pack: String) = saves.getInt(pack, 1)

    fun getLevelName(pack: String, level: Int) = pack + "_" + level

    fun getLevelCount(pack: String): Int {
        val packId = resources.getStringArray(R.array.packs).indexOf(pack)
        return resources.getIntArray(R.array.packs_sizes)[packId]
    }

    fun getLevelsRemainingToUnlock(packIndex: Int): Int? {
        val levelsToUnlock = resources.getIntArray(R.array.packs_levels_to_unlock)[packIndex]
        if (levelsToUnlock == -1) return null
        return levelsToUnlock - getCompletedLevelsCount()
    }

    fun getCompletedLevels(pack: String): Int {
        return saves.getInt(pack + "completed", 0)
    }

    fun getCompletedLevelsCount(): Int {
        val packs = resources.getStringArray(R.array.packs)
        return packs.fold(0, { completedLevels, pack ->
            completedLevels + getCompletedLevels(pack)
        })
    }

    fun getCoins() = saves.getInt("coins", 0)

    fun addCoins(coinsToAdd: Int) {
        saves.edit().apply {
            putInt("coins", getCoins() + coinsToAdd)
            apply()
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

    fun isPackOpen(pack: String, packIndex: Int): Boolean {
        val levelsToUnlock = resources.getIntArray(R.array.packs_levels_to_unlock)[packIndex]
        val isPackOpenedByLevels = levelsToUnlock != -1 && getCompletedLevelsCount() >= levelsToUnlock
        return isPackPurchased(pack) || isPackOpenedByLevels
    }

    fun isPackPurchased(pack: String) = saves.getBoolean(pack + "purchased", false)

    /** @return is level completed first time */
    fun completeLevel(pack: String): Boolean {
        val currentLevel = getCurrentLevel(pack)
        val levelCount = getLevelCount(pack)
        val levelName = pack + currentLevel
        var isCompletedFirstTime = false

        val editor = saves.edit()
        editor.putString(levelName, saves.getString(levelName, "")!!.replace("!", "").replace("*", ""))
        if (currentLevel > saves.getInt(pack + "completed", 0)) {
            editor.putInt(pack + "completed", currentLevel)
            addCoins(resources.getInteger(R.integer.level_reward))
            isCompletedFirstTime = true
            AnalyticsHelper.logLevelComplete(pack, currentLevel)
        }
        if (currentLevel + 1 > levelCount) editor.putInt(pack, 1)
        else editor.putInt(pack, currentLevel + 1)
        editor.apply()

        if (isCompletedFirstTime) checkUnlockedLevels()

        return isCompletedFirstTime
    }

    private fun checkUnlockedLevels() {
        val packs = resources.getStringArray(R.array.packs)
        val packsLevelsToUnlock = resources.getIntArray(R.array.packs_levels_to_unlock)
        val completedLevelsCount = getCompletedLevelsCount()

        for (id in packs.indices) {
            val pack = packs[id]
            if (packsLevelsToUnlock[id] == completedLevelsCount && !isPackPurchased(pack)) {
                AnalyticsHelper.logPackUnlock(pack, "levels")
            }
        }
    }
}
