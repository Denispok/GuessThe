package com.gamesbars.guessthe.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.WindowManager
import android.widget.TextView
import com.gamesbars.guessthe.PlayActivity
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.Storage.getAuthorAndLicense
import com.gamesbars.guessthe.Storage.getCurrentLevel

class InfoDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(pack: String): InfoDialogFragment {
            val args = Bundle()
            args.putString("pack", pack)
            val fragment = InfoDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var author: String
    private lateinit var license: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pack = arguments!!.getString("pack")!!
        val currentLevel = getCurrentLevel(pack)

        val authorAndLicense = getAuthorAndLicense(pack, currentLevel)
        author = authorAndLicense.first
        license = authorAndLicense.second
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_level_info, null)
        val fragment = fragmentManager!!.findFragmentByTag(resources.getString(R.string.level_fragment_tag)) as LevelFragment

        view.apply {
            findViewById<TextView>(R.id.info_author).also {
                it.setText(Html.fromHtml(author))
                it.movementMethod = LinkMovementMethod.getInstance()
            }
            findViewById<TextView>(R.id.info_license).also {
                it.setText(Html.fromHtml(license))
                it.movementMethod = LinkMovementMethod.getInstance()
            }
            findViewById<TextView>(R.id.info_cancel).setOnClickListener {
                dismiss()
                updateFragment(fragment)
            }
        }

        builder.setView(view)
        val dialog = builder.create()
        dialog.window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        return dialog
    }

    fun updateFragment(fragment: LevelFragment) {
        (fragment.activity!! as PlayActivity).currentDialog = null
        fragment.isClickable = true
    }
}
