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

    fun getLevelCount(pack: String): Int {
        val packId = resources.getStringArray(R.array.packs).indexOf(pack)
        return resources.getIntArray(R.array.packs_sizes)[packId]
    }

    fun getLevelsToUnlock(packIndex: Int): Int {
        return resources.getIntArray(R.array.packs_levels_to_unlock)[packIndex] - getCompletedLevelsCount()
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
        return isPackPurchased(pack) || getCompletedLevelsCount() >= resources.getIntArray(R.array.packs_levels_to_unlock)[packIndex]
    }

    fun isPackPurchased(pack: String) = saves.getBoolean(pack + "purchased", false)
}
