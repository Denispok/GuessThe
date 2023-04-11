package com.gamesbars.guessthe.fragment

import android.content.Intent
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.Storage
import com.gamesbars.guessthe.Storage.getCurrentLevel
import com.gamesbars.guessthe.Storage.getDrawableResIdByName
import com.gamesbars.guessthe.Storage.getLevelName
import com.gamesbars.guessthe.Storage.getStringArrayResIdByName
import com.gamesbars.guessthe.Storage.isLevelHaveInfo
import com.gamesbars.guessthe.databinding.FragmentLevelBinding
import com.gamesbars.guessthe.level.Letter
import com.gamesbars.guessthe.level.Letters
import com.gamesbars.guessthe.level.WordLetters
import com.gamesbars.guessthe.playSound
import com.gamesbars.guessthe.screen.PlayActivity
import com.gamesbars.guessthe.screen.coins.CoinsActivity
import com.google.firebase.analytics.FirebaseAnalytics

class LevelFragment : Fragment() {

    private lateinit var image: String
    private lateinit var word: String
    lateinit var pack: String
    lateinit var letters: Letters
    lateinit var wordLetters: WordLetters
    private var _binding: FragmentLevelBinding? = null
    private val binding get() = _binding!!
    private var isFirstStart = true
    var isClickable = true
    private val tipsDialog = TipsDialogFragment()
    private val infoDialog by lazy { InfoDialogFragment.newInstance(pack) }

    private val saves = Storage.saves
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    companion object {
        fun newInstance(pack: String): LevelFragment {
            val args = Bundle()
            args.putString("pack", pack)
            val fragment = LevelFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)

        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)

        pack = arguments!!.getString("pack")!!
        loadLevel(pack)

        wordLetters = WordLetters(this, word)
        letters = Letters(activity!!, word, pack)
        letters.setLettersOnClickListener {
            if (isClickable) wordLetters.addLetter(it as Letter)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLevelBinding.inflate(inflater, container, false)

        val levelCaption = Storage.getLevelCaption(pack, getCurrentLevel(pack))
        if (levelCaption != null) {
            binding.imageCaptionTv.text = levelCaption
        } else {
            binding.imageCaptionTv.isVisible = false
        }

        binding.levelImageIv.setImageResource(getDrawableResIdByName(image))
        wordLetters.addLettersToLayout(binding.wordLettersL)
        letters.addLettersToLayout(binding.letters1Ll, binding.letters2Ll)

        binding.levelTitleBtn.text = getString(R.string.level, getCurrentLevel(pack))
        binding.backIv.setOnClickListener {
            if (isClickable) {
                playSound(context!!, R.raw.button)
                activity!!.onBackPressed()
            }
        }
        binding.levelTitleBtn.setOnClickListener { startLevelSelection() }
        binding.levelInfoIv.apply {
            if (isLevelHaveInfo(pack, getCurrentLevel(pack))) {
                setOnClickListener { info() }
            } else {
                visibility = View.GONE
            }
        }
        binding.coinsTv.setOnClickListener { coins() }
        binding.tipsBtn.setOnClickListener { tips() }
        binding.getCoinsBtn.setOnClickListener { coins() }

        if (isFirstStart) {
            binding.root.doOnNextLayout {
                playSound(binding.root.context, R.raw.start)
                letters.showChosenLetters(wordLetters)
                if (letters.removeTipUsed) letters.tipRemoveLetters(wordLetters)
            }

            isFirstStart = false
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        isClickable = true
        updateCoins()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadLevel(pack: String) {
        if (!saves.contains(pack)) {
            val editor = saves.edit()
            editor.putInt(pack, 1)
            editor.apply()
        }

        val currentLevel = getCurrentLevel(pack)

        image = getLevelName(pack, currentLevel)
        word = resources.getStringArray(getStringArrayResIdByName(pack))[currentLevel - 1]
    }

    private fun startLevelSelection() {
        if (isClickable) {
            isClickable = false
            playSound(context!!, R.raw.button)
            fragmentManager!!.beginTransaction()
                .replace(R.id.fragmentFl, LevelSelectionFragment.newInstance(pack))
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
        }
    }

    private fun info() {
        if (isClickable) {
            isClickable = false
            playSound(context!!, R.raw.button)
            (activity!! as PlayActivity).currentDialog = infoDialog
            infoDialog.show(fragmentManager!!, null)
            val params = Bundle()
            params.putString(FirebaseAnalytics.Param.LEVEL, "$pack ${getCurrentLevel(pack)}")
            firebaseAnalytics.logEvent("level_info", params)
        }
    }

    private fun tips() {
        if (isClickable) {
            isClickable = false
            playSound(context!!, R.raw.button)
            (activity!! as PlayActivity).currentDialog = tipsDialog
            tipsDialog.show(fragmentManager!!, resources.getString(R.string.tips_dialog_fragment_tag))
        }
    }

    private fun coins() {
        if (isClickable) {
            isClickable = false
            playSound(context!!, R.raw.button)
            startActivity(Intent(context, CoinsActivity::class.java))
        }
    }

    fun updateCoins() {
        val coins = Storage.getCoins().toString()
        binding.coinsTv.text = coins
        firebaseAnalytics.setUserProperty("coins", coins)
    }

    fun win() {
        val isLevelReward = Storage.completeLevel(pack)

        val fragment = WinFragment.newInstance(word, image, arguments!!.getString("pack")!!, isLevelReward)
        fragmentManager!!.beginTransaction()
            .replace(R.id.fragmentFl, fragment, resources.getString(R.string.win_fragment_tag))
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .addSharedElement(binding.levelImageIv, "ImageTransition")
            .commit()
    }
}