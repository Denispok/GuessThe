package com.gamesbars.guessthe.screen.coins

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.ads.BannerAdDelegate
import com.gamesbars.guessthe.ads.RewardedAdDelegate
import com.gamesbars.guessthe.data.CoinsStorage
import com.gamesbars.guessthe.databinding.ActivityCoinsBinding
import com.gamesbars.guessthe.playSound
import com.gamesbars.guessthe.screen.coins.data.ProductDTO
import com.gamesbars.guessthe.screen.coins.data.ProductDTOProvider
import com.gamesbars.guessthe.sliceUntilIndex
import com.gamesbars.guessthe.toast
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class CoinsActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private const val BILLING_CONNECTION_RETRY_DELAY = 3000L
        private const val LOADER_ANIMATION_DURATION = 100L
    }

    private val productAdapter = ProductAdapter()
    private lateinit var binding: ActivityCoinsBinding
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
        binding = ActivityCoinsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        saves = getSharedPreferences("saves", Context.MODE_PRIVATE)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        productDTOList = ProductDTOProvider.getProductDTOList(resources)
        rewardedAdDelegate = RewardedAdDelegate(this, ::onRewardEarned)
        bannerAdDelegate = BannerAdDelegate(this)

        billingClient = BillingClient.newBuilder(this)
            .setListener(PurchasesUpdatedListenerImpl())
            .enablePendingPurchases()
            .build()
        connectToBilling()

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
        binding.backIv.setOnClickListener {
            playSound(this, R.raw.button)
            onBackPressed()
        }

        binding.productRv.adapter = productAdapter
        binding.productRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.productRv.addItemDecoration(ProductDecoration(this))
        productAdapter.onItemClickListener = { product ->
            launchBillingFlow(product)
        }

        binding.coinsVideoTv.text = (2 * CoinsStorage.getLevelReward()).toString()
        binding.coinsRewardedVideoLl.setOnClickListener {
            rewardedAdDelegate.showRewardedVideoAd()
        }
    }

    @MainThread
    private fun showLoader(isShow: Boolean) {
        binding.progressFl.isVisible = true
        binding.progressFl.animate()
            .setDuration(LOADER_ANIMATION_DURATION)
            .alpha(if (isShow) 1f else 0f)
            .withEndAction { if (!isShow) binding.progressFl.isVisible = false }
            .start()
    }

    private fun updateBannerAd() {
        if (saves.getBoolean("ads", true)) {
            binding.adViewContainer.visibility = View.VISIBLE
            bannerAdDelegate.updateBanner(binding.adViewContainer)
        } else {
            binding.adViewContainer.visibility = View.GONE
        }
    }

    private fun onRewardEarned() {
        CoinsStorage.addCoins(2 * CoinsStorage.getLevelReward())
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
        if (removeAds) saves.edit().apply {
            putBoolean("ads", false)
            apply()
        }
        CoinsStorage.addCoins(coins)
        updateCoins()
    }

    private fun updateCoins() {
        val coins = CoinsStorage.getCoins().toString()
        runOnUiThread {
            binding.coinsTv.text = coins
        }
        firebaseAnalytics.setUserProperty("coins", coins)
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        val productId = purchase.products.getOrNull(0)

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
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val purchasesResult = billingClient.queryPurchasesAsync(params)

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
        val productDetailsParamsList =
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(product.productDetails)
                    .build()
            )
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        val billingResult = billingClient.launchBillingFlow(this, flowParams)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            runOnUiThread { showLoader(false) }
            showBillingError(getString(R.string.purchases_error_billing_flow), "launchBillingFlow: ${billingResult.debugMessage}")
        }
    }

    private suspend fun loadProducts() {
        val ads = saves.getBoolean("ads", true)

        val queryDetailsProductList = productDTOList.map { productDto ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(if (productDto.isSellingWithAds(ads)) productDto.adsId!! else productDto.id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(queryDetailsProductList)
            .build()

        val productDetailsResult = billingClient.queryProductDetails(params)

        if (productDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val productDetailsList = productDetailsResult.productDetailsList
            if (!productDetailsList.isNullOrEmpty()) {

                val productList = mutableListOf<Product>()
                for (productDTO in productDTOList) {

                    val productId = if (productDTO.isSellingWithAds(ads)) productDTO.adsId else productDTO.id
                    val productDetails = productDetailsList.find { productId == it.productId }
                    val oneTimePurchaseOfferDetails = productDetails?.oneTimePurchaseOfferDetails
                    if (productDetails != null && oneTimePurchaseOfferDetails != null) {
                        productList.add(
                            Product(
                                coins = productDTO.coinsCount,
                                price = oneTimePurchaseOfferDetails.formattedPrice,
                                productDetails = productDetails,
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
                    "productDetailsList is null or empty: ${productDetailsResult.billingResult.debugMessage}"
                )
            }
        } else {
            showBillingError(getString(R.string.purchases_error), "loadProducts: ${productDetailsResult.billingResult.debugMessage}")
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