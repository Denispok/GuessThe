package com.gamesbars.guessthe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_coins.*

class CoinsActivity : AppCompatActivity(), BillingProcessor.IBillingHandler, RewardedVideoAdListener {

    companion object {
        const val PRODUCT_1_ID = "android.test.purchased"
        const val PRODUCT_2_ID = "android.test.purchased"
        const val PRODUCT_3_ID = "android.test.purchased"
        const val PRODUCT_2_ID_WITH_ADS = "android.test.purchased"
        const val PRODUCT_3_ID_WITH_ADS = "android.test.purchased"
    }

    var isClickable: Boolean = true

    private lateinit var saves: SharedPreferences
    private lateinit var billingProcessor: BillingProcessor
    private lateinit var mRewardedVideoAd: RewardedVideoAd

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ViewPump.init(ViewPump.builder()
            .addInterceptor(CalligraphyInterceptor(
                CalligraphyConfig.Builder()
                    .setDefaultFontPath("fonts/Exo_2/Exo2-Medium.ttf")
                    .setFontAttrId(R.attr.fontPath)
                    .build()))
            .build())

        setContentView(R.layout.activity_coins)

        saves = getSharedPreferences("saves", Context.MODE_PRIVATE)

        billingProcessor = BillingProcessor(this, "INSERT LICENSE KEY HERE", this)
        if (BillingProcessor.isIabServiceAvailable(this)) {
            billingProcessor.initialize()
        } else {
            Toast.makeText(this, getString(R.string.purchases_unavailable), Toast.LENGTH_LONG).show()
            hidePurchases()
        }

        hideSystemUI()
        checkPurchases()
        updateCoins()

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this

        if (saves.getBoolean("ads", true)) {
            if (hasConnection(this)) {
                adView.visibility = View.VISIBLE
                adView.loadAd(buildAdRequest(saves))
            }
        }

        findViewById<ImageView>(R.id.coins_back).setOnClickListener {
            if (isClickable) {
                playSound(this, R.raw.button)
                this.onBackPressed()
            }
        }
        findViewById<TextView>(R.id.coins_purchase_1_coins).text = resources.getInteger(R.integer.coins_purchase_1).toString()
        findViewById<TextView>(R.id.coins_purchase_2_coins).text = resources.getInteger(R.integer.coins_purchase_2).toString()
        findViewById<TextView>(R.id.coins_purchase_3_coins).text = resources.getInteger(R.integer.coins_purchase_3).toString()
        findViewById<TextView>(R.id.coins_video).text = (2 * resources.getInteger(R.integer.level_reward)).toString()
        findViewById<LinearLayout>(R.id.coins_rewarded_video).setOnClickListener {
            if (isClickable) {
                isClickable = false
                showRewardedVideoAd()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isClickable = true
    }

    private fun updateCoins() {
        val coins = saves.getInt("coins", 0).toString()
        findViewById<TextView>(R.id.coins_coins).text = coins
    }

    private fun checkPurchases() {
        if (!saves.getBoolean("ads", true)) {
            findViewById<TextView>(R.id.coins_purchase_2_ads).visibility = View.GONE
            findViewById<TextView>(R.id.coins_purchase_3_ads).visibility = View.GONE
        }
    }

    private fun addCoins(coins: Int, productId: String? = null, removeAds: Boolean = false) {
        val editor = saves.edit()
        if (removeAds) editor.putBoolean("ads", false)
        editor.putInt("coins", saves.getInt("coins", 0) + coins)
        editor.apply()
        if (productId != null) billingProcessor.consumePurchase(productId)
        updateCoins()
    }

    private fun hidePurchases() {
        findViewById<LinearLayout>(R.id.coins_purchase_1).visibility = View.GONE
        findViewById<LinearLayout>(R.id.coins_purchase_2).visibility = View.GONE
        findViewById<LinearLayout>(R.id.coins_purchase_3).visibility = View.GONE
        findViewById<View>(R.id.coins_divider_1).visibility = View.GONE
        findViewById<View>(R.id.coins_divider_2).visibility = View.GONE
        findViewById<View>(R.id.coins_divider_3).visibility = View.GONE
    }

    override fun onBillingInitialized() {
        try {
            val ads = saves.getBoolean("ads", true)

            val purchase1 = billingProcessor.getPurchaseListingDetails(PRODUCT_1_ID)
            findViewById<TextView>(R.id.coins_purchase_1_price).text = purchase1.priceText
            findViewById<LinearLayout>(R.id.coins_purchase_1).setOnClickListener {
                if (isClickable) {
                    isClickable = false
                    playSound(this, R.raw.button)
                    billingProcessor.purchase(this, PRODUCT_1_ID)
                }
            }

            val purchase2Id = if (ads) PRODUCT_2_ID_WITH_ADS else PRODUCT_2_ID
            val purchase3Id = if (ads) PRODUCT_3_ID_WITH_ADS else PRODUCT_3_ID

            val purchase2 = billingProcessor.getPurchaseListingDetails(purchase2Id)
            val purchase3 = billingProcessor.getPurchaseListingDetails(purchase3Id)

            findViewById<TextView>(R.id.coins_purchase_2_price).text = purchase2.priceText
            findViewById<LinearLayout>(R.id.coins_purchase_2).setOnClickListener {
                if (isClickable) {
                    isClickable = false
                    playSound(this, R.raw.button)
                    billingProcessor.purchase(this, purchase2Id)
                }
            }

            findViewById<TextView>(R.id.coins_purchase_3_price).text = purchase3.priceText
            findViewById<LinearLayout>(R.id.coins_purchase_3).setOnClickListener {
                if (isClickable) {
                    isClickable = false
                    playSound(this, R.raw.button)
                    billingProcessor.purchase(this, purchase3Id)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.purchases_error), Toast.LENGTH_LONG).show()
            hidePurchases()
        }
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        when (productId) {
            PRODUCT_1_ID -> addCoins(resources.getInteger(R.integer.coins_purchase_1), productId)
            PRODUCT_2_ID -> addCoins(resources.getInteger(R.integer.coins_purchase_2), productId)
            PRODUCT_3_ID -> addCoins(resources.getInteger(R.integer.coins_purchase_3), productId)
            PRODUCT_2_ID_WITH_ADS -> addCoins(resources.getInteger(R.integer.coins_purchase_2), removeAds = true)
            PRODUCT_3_ID_WITH_ADS -> addCoins(resources.getInteger(R.integer.coins_purchase_3), removeAds = true)
        }
    }

    override fun onPurchaseHistoryRestored() {
        val products = billingProcessor.listOwnedProducts()
        if (saves.getBoolean("ads", true)) {
            if (products.contains(PRODUCT_2_ID_WITH_ADS)) {
                addCoins(resources.getInteger(R.integer.coins_purchase_2), removeAds = true)
                Toast.makeText(this, getString(R.string.purchases_restored), Toast.LENGTH_LONG).show()
            }
            if (products.contains(PRODUCT_3_ID_WITH_ADS)) {
                addCoins(resources.getInteger(R.integer.coins_purchase_3), removeAds = true)
                Toast.makeText(this, getString(R.string.purchases_restored), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    public override fun onDestroy() {
        billingProcessor.release()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun showRewardedVideoAd() {
        mRewardedVideoAd.loadAd(getString(R.string.rewarded_video_id), buildAdRequest(saves))
        Toast.makeText(this, getString(R.string.video_is_loading), Toast.LENGTH_LONG).show()
    }

    override fun onRewardedVideoAdClosed() {
        isClickable = true
    }

    override fun onRewardedVideoAdLeftApplication() {}

    override fun onRewardedVideoAdLoaded() {
        mRewardedVideoAd.show()
    }

    override fun onRewardedVideoAdOpened() {}

    override fun onRewardedVideoCompleted() {}

    override fun onRewarded(p0: RewardItem?) {
        saves.edit().apply {
            putInt("coins", saves.getInt("coins", 0) + 2 * resources.getInteger(R.integer.level_reward))
            apply()
        }
        Toast.makeText(this, R.string.video_reward, Toast.LENGTH_LONG).show()
        updateCoins()
    }

    override fun onRewardedVideoStarted() {}

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
        Toast.makeText(this, getString(R.string.video_error), Toast.LENGTH_LONG).show()
        isClickable = true
    }
}
