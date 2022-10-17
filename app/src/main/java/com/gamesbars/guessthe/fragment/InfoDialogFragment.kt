package com.gamesbars.guessthe.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.Storage.getAuthorAndLicense
import com.gamesbars.guessthe.Storage.getCurrentLevel
import com.gamesbars.guessthe.ads.AdsUtils
import com.gamesbars.guessthe.databinding.DialogLevelInfoBinding
import com.gamesbars.guessthe.screen.PlayActivity

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
        AdsUtils.fixDensity(resources)
        val builder = AlertDialog.Builder(context)
        val binding = DialogLevelInfoBinding.inflate(activity!!.layoutInflater)
        val fragment = fragmentManager!!.findFragmentByTag(resources.getString(R.string.level_fragment_tag)) as LevelFragment

        binding.apply {
            authorTv.also {
                it.setText(Html.fromHtml(author))
                it.movementMethod = LinkMovementMethod.getInstance()
            }
            licenseTv.also {
                it.setText(Html.fromHtml(license))
                it.movementMethod = LinkMovementMethod.getInstance()
            }
            cancelTv.setOnClickListener {
                dismiss()
                updateFragment(fragment)
            }
        }

        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        return dialog
    }

    fun updateFragment(fragment: LevelFragment) {
        (fragment.activity!! as PlayActivity).currentDialog = null
        fragment.isClickable = true
    }
}
