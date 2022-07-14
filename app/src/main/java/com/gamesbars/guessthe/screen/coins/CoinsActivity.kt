package com.gamesbars.guessthe.screen.coins

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.ads.BannerAdDelegate
import com.gamesbars.guessthe.ads.RewardedAdDelegate
import com.gamesbars.guessthe.playSound
import com.gamesbars.guessthe.screen.coins.data.ProductDTO
import com.gamesbars.guessthe.screen.coins.data.ProductDTOProvider
import com.gamesbars.guessthe.sliceUntilIndex
import com.gamesbars.guessthe.toast
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_coins.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class CoinsActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private const val BILLING_CONNECTION_RETRY_DELAY = 3000L
        private const val LOADER_ANIMATION_DURATION = 100L
    }

    private val productAdapter = ProductAdapter()
    private lateinit var productDTOList: List<ProductDTO>
    private lateinit var billingClient: BillingClient
    private lateinit var saves: SharedPreferences
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var rewardedAdDelegate: RewardedAdDelegate
    private lateinit var bannerAdDelegate: BannerAdDelegate

    private val activityJob = SupervisorJob()
    override val coroutineContext: CoroutineContext =
        activityJob + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            runOnUiThread { throw throwable }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coins)

        saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        productDTOList = ProductDTOProvider.getProductDTOList(resources)
        rewardedAdDelegate = RewardedAdDelegate(this, saves, ::onRewardEarned)
        bannerAdDelegate = BannerAdDelegate(this, saves)

        billingClient = BillingClient.newBuilder(this)
            .setListener(PurchasesUpdatedListenerImpl())
            .enablePendingPurchases()
            .build()
        connectToBilling()

        if (saves.getBoolean("ads", true)) bannerAdDelegate.loadBanner(this, adViewContainer)

        rewardedAdDelegate.loadRewardedAd()

        setupUI()
        updateCoins()
    }

    override fun onResume() {
        super.onResume()
        updateBannerAd()
    }

    override fun onDestroy() {
        activityJob.cancel()
        billingClient.endConnection()
        super.onDestroy()
    }

    @MainThread
    private fun setupUI() {
        backIv.setOnClickListener {
            playSound(this, R.raw.button)
            onBackPressed()
        }

        productRv.adapter = productAdapter
        productRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        productRv.addItemDecoration(ProductDecoration(this))
        productAdapter.onItemClickListener = { product ->
            launchBillingFlow(product)
        }

        coinsVideoTv.text = (2 * resources.getInteger(R.integer.level_reward)).toString()
        coinsRewardedVideoLl.setOnClickListener {
            rewardedAdDelegate.showRewardedVideoAd()
        }
    }

    @MainThread
    private fun showLoader(isShow: Boolean) {
        progressFl.isVisible = true
        progressFl.animate()
            .setDuration(LOADER_ANIMATION_DURATION)
            .alpha(if (isShow) 1f else 0f)
            .withEndAction { if (!isShow) progressFl.isVisible = false }
            .start()
    }

    private fun updateBannerAd() {
        if (saves.getBoolean("ads", true)) {
            adViewContainer.visibility = View.VISIBLE
            bannerAdDelegate.updateBanner(this, adViewContainer)
        } else {
            adViewContainer.visibility = View.GONE
        }
    }

    private fun onRewardEarned() {
        saves.edit().apply {
            putInt("coins", saves.getInt("coins", 0) + 2 * resources.getInteger(R.integer.level_reward))
            apply()
        }
        toast(getString(R.string.video_reward))
        updateCoins()
    }

    private fun connectToBilling() {
        val listener = object : BillingClientStateListener {

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    launch {
                        checkUnhandledPurchases()
                        loadProducts()
                    }
                } else {
                    showBillingError(getString(R.string.purchases_error), "onBillingSetupFinished: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                showBillingError(getString(R.string.purchases_error_retry), "onBillingServiceDisconnected")
                launch {
                    delay(BILLING_CONNECTION_RETRY_DELAY)
                    runOnUiThread { connectToBilling() }
                }
            }
        }
        billingClient.startConnection(listener)
    }

    private fun addAndUpdateCoins(coins: Int, removeAds: Boolean = false) {
        val editor = saves.edit()
        if (removeAds) editor.putBoolean("ads", false)
        editor.putInt("coins", saves.getInt("coins", 0) + coins)
        editor.apply()
        updateCoins()
    }

    private fun updateCoins() {
        val coins = saves.getInt("coins", 0).toString()
        runOnUiThread {
            coinsTv.text = coins
        }
        firebaseAnalytics.setUserProperty("coins", coins)
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        val productId = purchase.skus.getOrNull(0)

        productDTOList.forEach { productDto ->
            if (productId == productDto.id) {
                // consume one-time purchase
                addAndUpdateCoins(productDto.coinsCount, false)
                consumePurchase(purchase)
                runOnUiThread { toast(getString(R.string.purchases_successful)) }
            } else if (productId == productDto.adsId) {
                if (!purchase.isAcknowledged) {
                    // acknowledge permanent purchase
                    addAndUpdateCoins(productDto.coinsCount, true)
                    acknowledgePurchase(purchase)
                    runOnUiThread { toast(getString(R.string.purchases_successful)) }
                } else {
                    // restore permanent purchase
                    val ads = saves.getBoolean("ads", true)
                    if (ads) {
                        addAndUpdateCoins(productDto.coinsCount, true)
                        runOnUiThread { toast(getString(R.string.purchases_restored)) }
                        firebaseAnalytics.logEvent("purchase_restored", null)
                    }
                }
            }
        }
    }

    /** Ignore consuming result, user can get reward again because of consuming fail */
    private suspend fun consumePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.consumePurchase(consumeParams)
    }

    /** Ignore acknowledging result, user can get reward again because of acknowledging fail */
    private suspend fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(acknowledgePurchaseParams)
    }

    private suspend fun checkUnhandledPurchases() {
        val purchasesResult = billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP)

        if (purchasesResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            purchasesResult.purchasesList.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    handlePurchase(purchase)
                }
            }
        } else {
            showBillingError(null, "checkUnhandledPurchases: ${purchasesResult.billingResult.debugMessage}")
        }
    }

    private fun launchBillingFlow(product: Product) {
        runOnUiThread { showLoader(true) }
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(product.skuDetails)
            .build()
        val billingResult = billingClient.launchBillingFlow(this, flowParams)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            runOnUiThread { showLoader(false) }
            showBillingError(getString(R.string.purchases_error_billing_flow), "launchBillingFlow: ${billingResult.debugMessage}")
        }
    }

    private suspend fun loadProducts() {
        val ads = saves.getBoolean("ads", true)

        val skuList = productDTOList.map { productDto ->
            if (productDto.isSellingWithAds(ads)) productDto.adsId else productDto.id
        }

        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build()

        val skuDetailsResult = billingClient.querySkuDetails(params)

        if (skuDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val skuDetailsList = skuDetailsResult.skuDetailsList
            if (!skuDetailsList.isNullOrEmpty()) {

                val productList = mutableListOf<Product>()
                for (productDTO in productDTOList) {

                    val productSku = if (productDTO.isSellingWithAds(ads)) productDTO.adsId else productDTO.id
                    val skuDetails = skuDetailsList.find { productSku == it.sku }
                    if (skuDetails != null) {
                        productList.add(
                            Product(
                                coins = productDTO.coinsCount,
                                price = skuDetails.price,
                                skuDetails = skuDetails,
                                isAdsTitleVisible = productDTO.isSellingWithAds(ads)
                            )
                        )
                    }
                }
                withContext(Dispatchers.Main) {
                    productAdapter.submitList(productList)
                }
            } else {
                showBillingError(
                    getString(R.string.purchases_error),
                    "skuDetailsList is null or empty: ${skuDetailsResult.billingResult.debugMessage}"
                )
            }
        } else {
            showBillingError(getString(R.string.purchases_error), "loadProducts: ${skuDetailsResult.billingResult.debugMessage}")
        }
    }

    private fun showBillingError(message: String?, debugMessage: String?) {
        message?.let {
            runOnUiThread { toast(it) }
        }

        debugMessage?.let { msg ->
            val params = Bundle()
            params.putString("debug_message", msg.sliceUntilIndex(99))
            firebaseAnalytics.logEvent("billing_error", params)
        }
    }

    private fun ProductDTO.isSellingWithAds(isAdsEnabled: Boolean) = isAdsEnabled && this.adsId != null

    inner class PurchasesUpdatedListenerImpl : PurchasesUpdatedListener {

        override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
            runOnUiThread { showLoader(false) }
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                launch {
                    purchases?.forEach { purchase ->
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            handlePurchase(purchase)
                        }
                    }
                    loadProducts()
                }
            } else {
                showBillingError(null, "onPurchasesUpdated: ${billingResult.debugMessage}")
            }
        }
    }
}