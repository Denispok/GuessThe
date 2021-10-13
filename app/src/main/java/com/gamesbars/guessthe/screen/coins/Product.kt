package com.gamesbars.guessthe.screen.coins

import com.android.billingclient.api.SkuDetails

data class Product(
    val coins: Int,
    val price: String,
    val skuDetails: SkuDetails,
    val isAdsTitleVisible: Boolean = false
)