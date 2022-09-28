package com.gamesbars.guessthe.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.ads.AdsUtils
import com.gamesbars.guessthe.playSound
import com.gamesbars.guessthe.screen.PlayActivity
import com.google.firebase.analytics.FirebaseAnalytics

class TipsDialogFragment : DialogFragment() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        AdsUtils.fixDensity(resources)
        val builder = AlertDialog.Builder(context)
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_level_tips, null)

        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)
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
        val level = saves.getInt(fragment.pack, 0)
        val levelName = fragment.pack + level
        var levelString = saves.getString(levelName, "")

        val coins = saves.getInt("coins", 0)
        if (coins >= tipLetterCost) {
            tipLetter.setOnClickListener {
                val letter = fragment.letters.tipShowLetter(fragment.wordLetters)

                val replacedString = "_" + letter.letter + letter.wordLetterId!!.toString()
                val newString = "_" + letter.letter + letter.wordLetterId.toString() + "*"
                levelString = levelString!!.replace(replacedString, newString)

                val editor = saves.edit()
                editor.putString(levelName, levelString)
                editor.putInt("coins", coins - tipLetterCost)
                editor.apply()

                val params = Bundle()
                params.putString(FirebaseAnalytics.Param.LEVEL, "${fragment.pack} $level")
                firebaseAnalytics.logEvent("tip_letter", params)

                playSound(context!!, R.raw.tips)
                dismiss()
                updateFragment(fragment)
            }
        } else tipLetter.isEnabled = false
        if (coins >= tipRemoveCost && levelString!!.last() != '!') {
            tipRemove.setOnClickListener {
                fragment.letters.tipRemoveLetters(fragment.wordLetters)

                val editor = saves.edit()
                editor.putString(levelName, "$levelString!")
                editor.putInt("coins", coins - tipRemoveCost)
                editor.apply()

                val params = Bundle()
                params.putString(FirebaseAnalytics.Param.LEVEL, "${fragment.pack} $level")
                firebaseAnalytics.logEvent("tip_remove", params)

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

                val params = Bundle()
                params.putString(FirebaseAnalytics.Param.LEVEL, "${fragment.pack} $level")
                firebaseAnalytics.logEvent("tip_skip", params)

                playSound(context!!, R.raw.tips)
                dismiss()
                updateFragment(fragment)
                fragment.win()
            }
        } else tipSkip.isEnabled = false

        builder.setView(view)
        val dialog = builder.create()
        dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        return dialog
    }

    fun updateFragment(fragment: LevelFragment) {
        (fragment.activity!! as PlayActivity).currentDialog = null
        fragment.updateCoins()
        fragment.isClickable = true
    }
}