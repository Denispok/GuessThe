package com.gamesbars.guessthe.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.gamesbars.guessthe.AnalyticsHelper
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.Storage.getCurrentLevel
import com.gamesbars.guessthe.Storage.getDrawableResIdByName
import com.gamesbars.guessthe.Storage.getLevelCount
import com.gamesbars.guessthe.Storage.getStringArrayResIdByName
import com.gamesbars.guessthe.Storage.isLevelHaveInfo
import com.gamesbars.guessthe.Storage.saves
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
    private var isFirstStart = true
    var isClickable = true
    private val tipsDialog = TipsDialogFragment()
    private val infoDialog by lazy { InfoDialogFragment.newInstance(pack) }

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
        val view = inflater.inflate(R.layout.fragment_level, container, false)

        view.findViewById<ImageView>(R.id.level_image).setImageResource(getDrawableResIdByName(image))
        wordLetters.addLettersToLayout(view.findViewById(R.id.level_word))
        letters.addLettersToLayout(view.findViewById(R.id.level_letters_1), view.findViewById(R.id.level_letters_2))

        view.findViewById<Button>(R.id.level_level).text = getString(R.string.level, saves.getInt(pack, 1))
        view.findViewById<ImageView>(R.id.level_back).setOnClickListener {
            if (isClickable) {
                playSound(context!!, R.raw.button)
                activity!!.onBackPressed()
            }
        }
        view.findViewById<TextView>(R.id.level_level).setOnClickListener { startLevelSelection() }
        view.findViewById<ImageView>(R.id.level_info).apply {
            if (isLevelHaveInfo(pack, getCurrentLevel(pack))) {
                setOnClickListener { info() }
            } else {
                visibility = View.GONE
            }
        }
        view.findViewById<TextView>(R.id.level_coins).setOnClickListener { coins() }
        view.findViewById<TextView>(R.id.level_tips_button).setOnClickListener { tips() }
        view.findViewById<TextView>(R.id.level_coins_button).setOnClickListener { coins() }

        if (isFirstStart) {
            Handler().postDelayed({
                playSound(view.context, R.raw.start)
                letters.showChosenLetters(wordLetters)
                if (letters.removeTipUsed) letters.tipRemoveLetters(wordLetters)
            }, 100L)

            isFirstStart = false
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        isClickable = true
        updateCoins()
    }

    private fun loadLevel(pack: String) {

        if (!saves.contains(pack)) {
            val editor = saves.edit()
            editor.putInt(pack, 1)
            editor.apply()
        }

        val currentLevel = getCurrentLevel(pack)

        image = pack + currentLevel
        word = resources.getStringArray(getStringArrayResIdByName(pack))[currentLevel - 1]
    }

    private fun startLevelSelection() {
        if (isClickable) {
            isClickable = false
            playSound(context!!, R.raw.button)
            fragmentManager!!.beginTransaction()
                .replace(R.id.activity_play_fragment, LevelSelectionFragment.newInstance(pack))
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
        val coins = saves.getInt("coins", 0).toString()
        activity!!.findViewById<TextView>(R.id.level_coins).text = coins
        firebaseAnalytics.setUserProperty("coins", coins)
    }

    fun win() {
        val currentLevel = getCurrentLevel(pack)
        val levelCount = getLevelCount(pack)
        val levelName = pack + currentLevel
        var isLevelReward = false

        val editor = saves.edit()
        editor.putString(levelName, saves.getString(levelName, "")!!.replace("!", "").replace("*", ""))
        if (currentLevel > saves.getInt(pack + "completed", 0)) {
            editor.putInt(pack + "completed", currentLevel)
            editor.putInt("coins", saves.getInt("coins", 0) + resources.getInteger(R.integer.level_reward))
            isLevelReward = true
            AnalyticsHelper.logLevelComplete(pack, currentLevel)
        }
        if (currentLevel + 1 > levelCount) editor.putInt(pack, 1)
        else editor.putInt(pack, currentLevel + 1)
        editor.apply()

        val fragment = WinFragment.newInstance(word, image, arguments!!.getString("pack")!!, isLevelReward)
        fragmentManager!!.beginTransaction()
            .replace(R.id.activity_play_fragment, fragment, resources.getString(R.string.win_fragment_tag))
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .addSharedElement(activity!!.findViewById<ImageView>(R.id.level_image), "ImageTransition")
            .commit()
    }
}