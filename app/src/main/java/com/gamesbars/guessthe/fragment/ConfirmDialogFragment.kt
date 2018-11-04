package com.gamesbars.guessthe.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.gamesbars.guessthe.LevelMenuActivity
import com.gamesbars.guessthe.R

class ConfirmDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(pack: String): DialogFragment {
            val args = Bundle()
            args.putString("pack", pack)
            val fragment = ConfirmDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    lateinit var pack: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pack = arguments!!.getString("pack")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val saves = context!!.getSharedPreferences("saves", Context.MODE_PRIVATE)
        val packPrice = resources.getInteger(R.integer.pack_price)
        val builder = AlertDialog.Builder(context)
        if (saves.getInt("coins", 0) >= packPrice) {
            builder.setMessage(getString(R.string.confirm_dialog_message, packPrice))
            builder.setPositiveButton(R.string.ok) { _, _ ->
                val editor = saves.edit()
                editor.putBoolean(pack + "purchased", true)
                editor.putInt("coins", saves.getInt("coins", 0) - packPrice)
                editor.apply()
            }
            builder.setNegativeButton(R.string.cancel) { _, _ -> }
        } else {
            builder.setMessage(getString(R.string.dont_enough_coins))
            builder.setPositiveButton(getString(R.string.ok)) { _, _ -> }
        }
        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        val levelMenuActivity = (activity as LevelMenuActivity)
        levelMenuActivity.updateCoins()
        levelMenuActivity.updateProgressBars()
        levelMenuActivity.isClickable = true
    }
}