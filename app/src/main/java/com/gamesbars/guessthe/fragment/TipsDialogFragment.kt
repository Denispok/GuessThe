package com.gamesbars.guessthe.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import com.gamesbars.guessthe.PlayActivity
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.playSound
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.launch

class TipsDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_tips, null)

        CoroutineScope(Dispatchers.Main).launch {
            val fragment = fragmentManager!!.findFragmentByTag(resources.getString(R.string.level_fragment_tag)) as LevelFragment

            val tipLetter = view.findViewById<RelativeLayout>(R.id.tips_letter)
            val tipRemove = view.findViewById<RelativeLayout>(R.id.tips_remove)
            val tipSkip = view.findViewById<RelativeLayout>(R.id.tips_skip)
            view.findViewById<TextView>(R.id.tips_cancel).setOnClickListener {
                playSound(context!!, R.raw.button)
                dismiss()
                updateFragment(fragment)
            }

            val tipLetterCost = resources.getInteger(R.integer.tip_letter_cost)
            val tipRemoveCost = resources.getInteger(R.integer.tip_remove_cost)
            val tipSkipCost = resources.getInteger(R.integer.tip_skip_cost)

            view.findViewById<TextView>(R.id.tips_letter_cost).text = tipLetterCost.toString()
            view.findViewById<TextView>(R.id.tips_remove_cost).text = tipRemoveCost.toString()
            view.findViewById<TextView>(R.id.tips_skip_cost).text = tipSkipCost.toString()

            val saves = context!!.getSharedPreferences("saves", Context.MODE_PRIVATE)
            val levelName = fragment.pack + saves.getInt(fragment.pack, 0)
            var levelString = saves.getString(levelName, "")

            val coins = saves.getInt("coins", 0)
            if (coins >= tipLetterCost) {
                tipLetter.setOnClickListener {
                    val letter = fragment.letters.tipShowLetter(fragment.wordLetters)

                    val replacedString = "_" + letter.letter + letter.wordLetterId!!.toString()
                    val newString = "_" + letter.letter + letter.wordLetterId.toString() + "*"
                    levelString = levelString.replace(replacedString, newString)

                    val editor = saves.edit()
                    editor.putString(levelName, levelString)
                    editor.putInt("coins", coins - tipLetterCost)
                    editor.apply()

                    playSound(context!!, R.raw.tips)
                    dismiss()
                    updateFragment(fragment)
                }
            } else tipLetter.isEnabled = false
            if (coins >= tipRemoveCost && levelString.last() != '!') {
                tipRemove.setOnClickListener {
                    fragment.letters.tipRemoveLetters(fragment.wordLetters)

                    val editor = saves.edit()
                    editor.putString(levelName, "$levelString!")
                    editor.putInt("coins", coins - tipRemoveCost)
                    editor.apply()

                    playSound(context!!, R.raw.tips)
                    dismiss()
                    updateFragment(fragment)
                }
            } else tipRemove.isEnabled = false
            if (coins >= tipSkipCost) {
                tipSkip.setOnClickListener {
                    val editor = saves.edit()
                    editor.putInt("coins", coins - tipSkipCost)
                    editor.apply()

                    playSound(context!!, R.raw.tips)
                    dismiss()
                    updateFragment(fragment)
                    fragment.win()
                }
            } else tipSkip.isEnabled = false
        }
        builder.setView(view)
        val dialog = builder.create()
        dialog.window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        return dialog
    }

    fun updateFragment(fragment: LevelFragment) {
        (fragment.activity!! as PlayActivity).currentDialog = null
        fragment.updateCoins()
        fragment.isClickable = true
    }
}