package com.gamesbars.guessthe.screen.coins.data

import android.content.res.Resources
import com.gamesbars.guessthe.R

object ProductDTOProvider {

    private const val PRODUCT_1_ID = "android.test.purchased"
    private const val PRODUCT_2_ID = "android.test.purchased"
    private const val PRODUCT_3_ID = "android.test.purchased"
    private const val PRODUCT_2_ID_WITH_ADS = "android.test.purchased"
    private const val PRODUCT_3_ID_WITH_ADS = "android.test.purchased"

    fun getProductDTOList(resources: Resources): List<ProductDTO> {
        return listOf(
            ProductDTO(PRODUCT_1_ID, null, resources.getInteger(R.integer.coins_purchase_1)),
            ProductDTO(PRODUCT_2_ID, PRODUCT_2_ID_WITH_ADS, resources.getInteger(R.integer.coins_purchase_2)),
            ProductDTO(PRODUCT_3_ID, PRODUCT_3_ID_WITH_ADS, resources.getInteger(R.integer.coins_purchase_3))
        )
    }
}