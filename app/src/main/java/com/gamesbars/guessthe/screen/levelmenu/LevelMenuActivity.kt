package com.gamesbars.guessthe.screen.levelmenu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.Storage
import com.gamesbars.guessthe.ads.BannerAdDelegate
import com.gamesbars.guessthe.databinding.ActivityLevelmenuBinding
import com.gamesbars.guessthe.databinding.ButtonLevelmenuFolderBinding
import com.gamesbars.guessthe.databinding.ButtonLevelmenuPackBinding
import com.gamesbars.guessthe.fragment.ConfirmDialogFragment
import com.gamesbars.guessthe.playSound
import com.gamesbars.guessthe.screen.PlayActivity
import com.gamesbars.guessthe.screen.coins.CoinsActivity
import com.google.firebase.analytics.FirebaseAnalytics

class LevelMenuActivity : AppCompatActivity() {

    companion object {

        private const val KEY_ITEM_ID = "item_id"

        fun getIntent(context: Context, itemId: String? = null): Intent {
            val intent = Intent(context, LevelMenuActivity::class.java)
            intent.putExtra(KEY_ITEM_ID, itemId)
            return intent
        }
    }

    private lateinit var saves: SharedPreferences
    var isClickable: Boolean = true
    var currentDialog: ConfirmDialogFragment? = null

    private lateinit var binding: ActivityLevelmenuBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var bannerAdDelegate: BannerAdDelegate
    private var itemId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLevelmenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        bannerAdDelegate = BannerAdDelegate(this, saves)
        itemId = intent.extras!!.getString(KEY_ITEM_ID)

        loadPacks()

        binding.backIv.setOnClickListener {
            if (isClickable) {
                playSound(this, R.raw.button)
                this.onBackPressed()
            }
        }

        binding.coinsTv.setOnClickListener {
            if (isClickable) {
                isClickable = false
                startActivity(Intent(applicationContext, CoinsActivity::class.java))
            }
        }

        if (saves.getBoolean("ads", true)) bannerAdDelegate.loadBanner(this, binding.adViewContainer)
    }

    override fun onResume() {
        super.onResume()
        updateCoins()
        updateProgressBars()
        updateBannerAd()
        isClickable = true
    }

    fun updateCoins() {
        val coins = Storage.getCoins().toString()
        binding.coinsTv.text = coins
        firebaseAnalytics.setUserProperty("coins", coins)
    }

    private fun loadPacks() {
        val items = LevelMenuStorage.getItems(itemId)
        val packsList = binding.levelsLl

        for (item in items) {
            when (item) {
                is LevelMenuItem.Folder -> {
                    val buttonBinding = ButtonLevelmenuFolderBinding.inflate(layoutInflater, packsList, false)
                    buttonBinding.root.text = item.name
                    buttonBinding.root.tag = item
                    buttonBinding.root.setOnClickListener {
                        startActivity(Companion.getIntent(this, item.id))
                    }
                    packsList.addView(buttonBinding.root)
                }

                is LevelMenuItem.Pack -> {
                    val buttonBinding = ButtonLevelmenuPackBinding.inflate(layoutInflater, packsList, false)
                    buttonBinding.packNameTv.text = item.name
                    buttonBinding.root.tag = item
                    packsList.addView(buttonBinding.root)
                }
            }
        }
    }

    private fun updateBannerAd() {
        if (saves.getBoolean("ads", true)) {
            binding.adViewContainer.visibility = View.VISIBLE
            bannerAdDelegate.updateBanner(this, binding.adViewContainer)
        } else {
            binding.adViewContainer.visibility = View.GONE
        }
    }

    fun updateProgressBars() {
        val packsList = binding.levelsLl

        for (id in 0 until packsList.childCount) {
            val button = packsList.getChildAt(id)
            val item = button.tag
            if (item is LevelMenuItem.Pack) {
                bindPack(item.id, button)
            }
        }
    }

    private fun bindPack(pack: String, view: View) {
        val packSize = Storage.getPackSize(pack)
        val buttonBinding = ButtonLevelmenuPackBinding.bind(view)
        val progressBarText = buttonBinding.progressTv
        if (Storage.isPackOpen(pack)) {
            val completedLevels = Storage.getCompletedLevels(pack)
            val completedPercent = ((completedLevels + 1).toFloat() / packSize * 100).toInt()
            buttonBinding.progressPb.progress = completedPercent
            progressBarText.text =
                if (completedLevels == packSize) getString(R.string.completed)
                else getString(R.string.percent, completedLevels + 1, packSize)
            progressBarText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            progressBarText.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = 0
            }
            buttonBinding.completeLevelsTv.isVisible = false
            view.setOnClickListener {
                if (isClickable) {
                    isClickable = false

                    val intent = Intent(this, PlayActivity::class.java)
                    intent.putExtra("pack", pack)
                    startActivity(intent)
                }
            }
        } else {
            progressBarText.text = getString(R.string.buy_levels, Storage.getPackPrice(pack))
            progressBarText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.coin_icon_16, 0)

            val levelsRemainingToUnlock = Storage.getLevelsRemainingToUnlock(pack)
            if (levelsRemainingToUnlock != null) {
                progressBarText.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    topMargin = resources.getDimension(R.dimen.levelmenu_progress_bar_text_margin).toInt()
                }
                buttonBinding.completeLevelsTv.isVisible = true
                buttonBinding.completeLevelsTv.text = resources.getQuantityString(
                    R.plurals.complete_levels_to_unlock, levelsRemainingToUnlock, levelsRemainingToUnlock
                )
            } else {
                progressBarText.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    topMargin = 0
                }
                buttonBinding.completeLevelsTv.isVisible = false
            }

            view.setOnClickListener {
                if (isClickable) {
                    isClickable = false
                    playSound(this, R.raw.button)
                    currentDialog = ConfirmDialogFragment.newInstance(pack)
                    currentDialog!!.show(supportFragmentManager, getString(R.string.confirm_dialog_fragment_tag))
                }
            }
        }
    }

    override fun onBackPressed() {
        currentDialog?.apply {
            updateActivity()
            dismiss()
        } ?: super.onBackPressed()
    }
}