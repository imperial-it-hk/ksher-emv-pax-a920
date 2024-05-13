package com.evp.payment.ksher.parameter

import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil

object ResourceParam : AbstractParam() {
    private const val PREFIX = "ResourceParam"

    /**
     * Print logo file path
     */
    @JvmStatic
    val printLogoFileName = SharedPreferencesUtil.getString("$PREFIX.printLogoFileName", "ic_merchant_logo.png")
    @JvmStatic
    val promotionFileName = SharedPreferencesUtil.getString("$PREFIX.promotionFileName", "ic_promotion_logo.png")
    @JvmStatic
    val disclaimerTxt = SharedPreferencesUtil.getString("$PREFIX.disclaimerTxt", "")
    @JvmStatic
    val labelFooter = SharedPreferencesUtil.getString("$PREFIX.labelFooter", "")
}