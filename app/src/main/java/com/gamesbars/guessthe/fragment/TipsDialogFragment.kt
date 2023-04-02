package com.gamesbars.guessthe.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.Storage
import com.gamesbars.guessthe.databinding.DialogLevelTipsBinding
import com.gamesbars.guessthe.playSound
import com.gamesbars.guessthe.screen.PlayActivity
import com.google.firebase.analytics.FirebaseAnalytics

class TipsDialogFragment : DialogFragment() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val binding = DialogLevelTipsBinding.inflate(activity!!.layoutInflater)

        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)
        val fragment = fragmentManager!!.findFragmentByTag(resources.getString(R.string.level_fragment_tag)) as LevelFragment

        val tipLetter = binding.tipLetterRl
        val tipRemove = binding.tipRemoveRl
        val tipSkip = binding.tipSkipRl
        binding.cancelTv.setOnClickListener {
            playSound(context!!, R.raw.button)
            dismiss()
            updateFragment(fragment)
        }

        val tipLetterCost = resources.getInteger(R.integer.tip_letter_cost)
        val tipRemoveCost = resources.getInteger(R.integer.tip_remove_cost)
        val tipSkipCost = resources.getInteger(R.integer.tip_skip_cost)

        binding.tipLetterCostTv.text = tipLetterCost.toString()
        binding.tipRemoveCostTv.text = tipRemoveCost.toString()
        binding.tipSkipCostTv.text = tipSkipCost.toString()

        val saves = context!!.getSharedPreferences("saves", Context.MODE_PRIVATE)
        val level = Storage.getCurrentLevel(fragment.pack)
        val levelName = Storage.getLevelName(fragment.pack, level)
        var levelString = saves.getString(levelName, "")

        val coins = Storage.getCoins()
        if (coins >= tipLetterCost) {
            tipLetter.setOnClickListener {
                val letter = fragment.letters.tipShowLetter(fragment.wordLetters)

                val replacedString = "_" + letter.letter + letter.wordLetterId!!.toString()
                val newString = "_" + letter.letter + letter.wordLetterId.toString() + "*"
                levelString = levelString!!.replace(replacedString, newString)

                val editor = saves.edit()
                editor.putString(levelName, levelString)
                editor.apply()
                Storage.addCoins(-tipLetterCost)

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
                editor.apply()
                Storage.addCoins(-tipRemoveCost)

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
                Storage.addCoins(-tipSkipCost)

                val params = Bundle()
                params.putString(FirebaseAnalytics.Param.LEVEL, "${fragment.pack} $level")
                firebaseAnalytics.logEvent("tip_skip", params)

                playSound(context!!, R.raw.tips)
                dismiss()
                updateFragment(fragment)
                fragment.win()
            }
        } else tipSkip.isEnabled = false

        builder.setView(binding.root)
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