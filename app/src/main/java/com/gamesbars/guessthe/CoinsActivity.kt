package com.gamesbars.guessthe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper


class CoinsActivity : AppCompatActivity(), BillingProcessor.IBillingHandler {

    companion object {
        const val PRODUCT_1_ID = "android.test.purchased"
        const val PRODUCT_2_ID = "android.test.purchased"
        const val PRODUCT_3_ID = "android.test.purchased"
        const val PRODUCT_2_ID_WITH_ADS = "android.test.purchased"
        const val PRODUCT_3_ID_WITH_ADS = "android.test.purchased"

    }

    private lateinit var billingProcessor: BillingProcessor

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

        billingProcessor = BillingProcessor(this, "LICENSE KEY HERE", this)
        if (BillingProcessor.isIabServiceAvailable(this)) {
            billingProcessor.initialize()
        } else {
            Toast.makeText(this, getString(R.string.purchases_unavailable), Toast.LENGTH_LONG).show()
        }

        setContentView(R.layout.activity_coins)
        checkPurchases()
        updateCoins()

        findViewById<ImageView>(R.id.coins_back).setOnClickListener { this.onBackPressed() }
        findViewById<TextView>(R.id.coins_purchase_1_coins).text = resources.getInteger(R.integer.coins_purchase_1).toString()
        findViewById<TextView>(R.id.coins_purchase_2_coins).text = resources.getInteger(R.integer.coins_purchase_2).toString()
        findViewById<TextView>(R.id.coins_purchase_3_coins).text = resources.getInteger(R.integer.coins_purchase_3).toString()
        findViewById<TextView>(R.id.coins_video).text = (2 * resources.getInteger(R.integer.level_reward)).toString()
    }

    private fun updateCoins() {
        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        findViewById<TextView>(R.id.coins_coins).text = saves.getInt("coins", 0).toString()
    }

    private fun checkPurchases() {
        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        if (!saves.getBoolean("ads", true)) {
            findViewById<TextView>(R.id.coins_purchase_2_ads).visibility = View.GONE
            findViewById<TextView>(R.id.coins_purchase_3_ads).visibility = View.GONE
        }
    }

    private fun addCoins(coins: Int, productId: String? = null, removeAds: Boolean = false) {
        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        val editor = saves.edit()
        if (removeAds) editor.putBoolean("ads", false)
        editor.putInt("coins", saves.getInt("coins", 0) + coins)
        editor.apply()
        if (productId != null) billingProcessor.consumePurchase(productId)
    }

    override fun onBillingInitialized() {
        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        val ads = saves.getBoolean("ads", true)

        val purchase1 = billingProcessor.getPurchaseListingDetails(PRODUCT_1_ID)
        findViewById<TextView>(R.id.coins_purchase_1_price).text = purchase1.priceText
        findViewById<LinearLayout>(R.id.coins_purchase_1).setOnClickListener { billingProcessor.purchase(this, PRODUCT_1_ID) }

        val purchase2Id = if (ads) PRODUCT_2_ID_WITH_ADS else PRODUCT_2_ID
        val purchase3Id = if (ads) PRODUCT_3_ID_WITH_ADS else PRODUCT_3_ID

        val purchase2 = billingProcessor.getPurchaseListingDetails(purchase2Id)
        val purchase3 = billingProcessor.getPurchaseListingDetails(purchase3Id)

        findViewById<TextView>(R.id.coins_purchase_2_price).text = purchase2.priceText
        findViewById<LinearLayout>(R.id.coins_purchase_2).setOnClickListener { billingProcessor.purchase(this, purchase2Id) }

        findViewById<TextView>(R.id.coins_purchase_3_price).text = purchase3.priceText
        findViewById<LinearLayout>(R.id.coins_purchase_3).setOnClickListener { billingProcessor.purchase(this, purchase3Id) }
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
        val saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        if (saves.getBoolean("ads", true)) {
            if (products.contains(PRODUCT_2_ID_WITH_ADS)) addCoins(resources.getInteger(R.integer.coins_purchase_2), removeAds = true)
            if (products.contains(PRODUCT_3_ID_WITH_ADS)) addCoins(resources.getInteger(R.integer.coins_purchase_3), removeAds = true)
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
}
