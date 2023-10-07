package com.gamesbars.guessthe.screen.levelmenu

import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.data.Storage
import org.json.JSONArray
import org.json.JSONObject

object LevelMenuStorage {

    private const val PROPERTY_NAME = "name"
    private const val PROPERTY_ITEMS = "items"


    fun getItems(folderItemId: String? = null): List<LevelMenuItem> {
        val jsonString = Storage.resources.openRawResource(R.raw.levels)
            .bufferedReader()
            .use { it.readText() }
        val json = JSONObject(jsonString)

        if (folderItemId != null) {
            val items = json.findObjectByName(folderItemId)!!.get(PROPERTY_ITEMS) as JSONObject
            return items.parseItems()
        }

        return json.parseItems()
    }

    private fun JSONObject.findObjectByName(name: String): JSONObject? {
        keys().forEach { key ->
            val value = get(key)
            if (key == name) return value as JSONObject
            if (value is JSONObject) {
                val jsonObject = value.findObjectByName(name)
                if (jsonObject != null) return jsonObject
            }
        }
        return null
    }

    private fun JSONObject.parseItems(): List<LevelMenuItem> {
        return this.names()!!.toList<String>().map { key ->
            val itemValue = get(key)
            if (itemValue is String) {
                LevelMenuItem.Pack(key, itemValue)
            } else {
                val itemJsonObject = itemValue as JSONObject
                val name = itemJsonObject.getString(PROPERTY_NAME)
                val jsonItems = itemJsonObject.get(PROPERTY_ITEMS) as JSONObject
                LevelMenuItem.Folder(key, name, jsonItems.parseItems())
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> JSONArray.toList(): List<T> {
        val list = mutableListOf<T>()
        for (i in 0 until length()) {
            list.add(this[i] as T)
        }
        return list
    }
}