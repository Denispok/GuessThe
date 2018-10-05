package com.gamesbars.guessthe

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.WindowManager
import android.widget.RelativeLayout
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.launch

class TipsDialogFragment : DialogFragment() {

    lateinit var viewInit: Job

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_tips, null)

        viewInit = CoroutineScope(Dispatchers.Main).launch {
            val fragment = fragmentManager!!.findFragmentByTag(resources.getString(R.string.level_fragment_tag)) as LevelFragment

            val saves = context!!.getSharedPreferences("saves", Context.MODE_PRIVATE)
            val levelName = fragment.pack + saves.getInt(fragment.pack, 0)
            var levelString = saves.getString(levelName, "")

            view.findViewById<RelativeLayout>(R.id.tips_letter).setOnClickListener {
                val letter = fragment.letters.tipShowLetter(fragment.wordLetters)

                val replacedString = letter.letter + letter.wordLetterId!!.toString()
                levelString = levelString.replace(replacedString, replacedString.toUpperCase())

                val editor = saves.edit()
                editor.putString(levelName, levelString)

                // COINS
                editor.apply()

                dialog.dismiss()
            }
            view.findViewById<RelativeLayout>(R.id.tips_remove).setOnClickListener {
                fragment.letters.tipRemoveLetters(fragment.wordLetters)

                val editor = saves.edit()
                editor.putString(levelName, "$levelString!")

                // COINS
                editor.apply()

                dialog.dismiss()
            }
            view.findViewById<RelativeLayout>(R.id.tips_skip).setOnClickListener {
                dialog.dismiss()
                fragment.win()
            }
        }

        builder.setView(view)
        val dialog = builder.create()
        dialog.window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        viewInit.cancel()
        (fragmentManager!!.findFragmentByTag(resources.getString(R.string.level_fragment_tag)) as LevelFragment).isClickable = true
    }
}