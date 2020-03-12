package com.gamesbars.guessthe

import android.content.Context

object Storage {

    val resources get() = App.appContext.resources!!
    val saves get() = App.appContext.getSharedPreferences("saves", Context.MODE_PRIVATE)!!

    fun getCurrentLevel(pack: String) = saves.getInt(pack, 1)

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
}
