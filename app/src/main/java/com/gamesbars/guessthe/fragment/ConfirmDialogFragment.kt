package com.gamesbars.guessthe.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.gamesbars.guessthe.AnalyticsHelper
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.ads.AdsUtils
import com.gamesbars.guessthe.playSound
import com.gamesbars.guessthe.screen.LevelMenuActivity

class ConfirmDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(packId: Int, pack: String): ConfirmDialogFragment {
            val args = Bundle()
            args.putInt("packId", packId)
            args.putString("pack", pack)
            val fragment = ConfirmDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var packId: Int = -1
    private lateinit var pack: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        packId = arguments!!.getInt("packId")
        pack = arguments!!.getString("pack")!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        AdsUtils.fixDensity(resources)
        val saves = context!!.getSharedPreferences("saves", Context.MODE_PRIVATE)
        val packPrice = resources.getIntArray(R.array.packs_prices)[packId]
        val builder = AlertDialog.Builder(context)
        if (saves.getInt("coins", 0) >= packPrice) {
            builder.setMessage(getString(R.string.confirm_dialog_message, packPrice))
            builder.setPositiveButton(R.string.ok) { _, _ ->
                saves.edit().apply {
                    putBoolean(pack + "purchased", true)
                    putInt("coins", saves.getInt("coins", 0) - packPrice)
                    apply()
                }

                AnalyticsHelper.logPackUnlock(pack, "coins")

                playSound(context!!, R.raw.button)
                updateActivity()
            }
            builder.setNegativeButton(R.string.cancel) { _, _ ->
                playSound(context!!, R.raw.button)
                updateActivity()
            }
        } else {
            builder.setMessage(getString(R.string.dont_enough_coins))
            builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
                playSound(context!!, R.raw.button)
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