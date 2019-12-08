package com.gamesbars.guessthe.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.WindowManager
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.playSound

class InternetConnectionDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.lost_internet_connection_title))
        builder.setMessage(getString(R.string.lost_internet_connection_message))
        builder.setPositiveButton(R.string.ok) { _, _ ->
            playSound(context!!, R.raw.button)
        }
        val dialog = builder.create()
        dialog.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        return dialog
    }
}
