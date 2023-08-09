package com.gamesbars.guessthe.screen.levelmenu

sealed class LevelMenuItem {

    data class Pack(
        val id: String,
        val name: String,
    ) : LevelMenuItem()

    data class Folder(
        val id: String,
        val name: String,
        val items: List<LevelMenuItem>,
    ) : LevelMenuItem()
}