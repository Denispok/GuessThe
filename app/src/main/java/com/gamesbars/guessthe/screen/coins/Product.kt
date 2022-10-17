package com.gamesbars.guessthe.screen.coins

import com.android.billingclient.api.ProductDetails

data class Product(
    val coins: Int,
    val price: String,
    val productDetails: ProductDetails,
    val isAdsTitleVisible: Boolean = false
)