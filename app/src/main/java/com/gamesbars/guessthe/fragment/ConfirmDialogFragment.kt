package com.gamesbars.guessthe.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.gamesbars.guessthe.AnalyticsHelper
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.data.CoinsStorage
import com.gamesbars.guessthe.data.Storage
import com.gamesbars.guessthe.playSound
import com.gamesbars.guessthe.screen.levelmenu.LevelMenuActivity

class ConfirmDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(pack: String): ConfirmDialogFragment {
            val args = Bundle()
            args.putString("pack", pack)
            val fragment = ConfirmDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var pack: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pack = requireArguments().getString("pack")!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val saves = requireContext().getSharedPreferences("saves", Context.MODE_PRIVATE)
        val packPrice = Storage.getPackPrice(pack)
        val builder = AlertDialog.Builder(context)
        if (CoinsStorage.getCoins() >= packPrice) {
            builder.setMessage(getString(R.string.confirm_dialog_message, packPrice))
            builder.setPositiveButton(R.string.ok) { _, _ ->
                saves.edit().apply {
                    putBoolean(pack + "purchased", true)
                    apply()
                }
                CoinsStorage.addCoins(-packPrice)

                AnalyticsHelper.logPackUnlock(pack, "coins")

                playSound(requireContext(), R.raw.button)
                updateActivity()
            }
            builder.setNegativeButton(R.string.cancel) { _, _ ->
                playSound(requireContext(), R.raw.button)
                updateActivity()
            }
        } else {
            builder.setMessage(getString(R.string.dont_enough_coins))
            builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
                playSound(requireContext(), R.raw.button)
                updateActivity()
            }
        }
        val dialog = builder.create()
        dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        return dialog
    }

    fun updateActivity() {
        val levelMenuActivity = (activity as LevelMenuActivity)
        levelMenuActivity.updateCoins()
        levelMenuActivity.updateProgressBars()
        levelMenuActivity.isClickable = true
        levelMenuActivity.currentDialog = null
    }
}