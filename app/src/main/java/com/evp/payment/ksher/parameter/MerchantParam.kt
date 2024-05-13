package com.evp.payment.ksher.parameter

import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil

object MerchantParam : AbstractParam() {
    private const val PREFIX = "MerchantParam"

    /**
     * Merchant number
     * Test data: 300990001280
     */
    @JvmStatic
    val merchantId = SharedPreferencesUtil.getString("$PREFIX.merchantId", "000000000000000")

    /**
     * Merchant name
     * Test data: TEST MERCHANT
     */
    @JvmStatic
    val name = SharedPreferencesUtil.getString("$PREFIX.name", "")

    /**
     * Merchant address
     * Test data: TEST ADDRESS
     */
    @JvmStatic
    val address = SharedPreferencesUtil.getString(
        "$PREFIX.address",
        "Merchant Address Line1\nMerchant Address Line2\nMerchant Address Line3"
    )

    /**
     * Store ID
     * Test data: 00001
     */
    @JvmStatic
    val storeId = SharedPreferencesUtil.getString("$PREFIX.storeId", "")

    /**
     * Store Name
     * Test data: TEST STORE NAME
     */
    @JvmStatic
    val storeName = SharedPreferencesUtil.getString("$PREFIX.storeName", "")

    @JvmStatic
    val apn = SharedPreferencesUtil.getString("$PREFIX.apn", "")

}