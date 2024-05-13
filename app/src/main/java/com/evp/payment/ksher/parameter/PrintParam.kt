package com.evp.payment.ksher.parameter

import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil

object PrintParam : AbstractParam() {
    private const val PREFIX = "PrintParam"

    /**
     * Print slip number
     */
    val printSlipNum = SharedPreferencesUtil.getString(PREFIX + ".printSlipNum", "1")

    /**
     * Print gray(depth)
     */
    val printGray = SharedPreferencesUtil.getString(PREFIX + ".printGray", "500")

    /**
     * Whether to print iso message log
     */
    val printIsoLogEnabled = SharedPreferencesUtil.getBoolean(PREFIX + ".printIsoLogEnabled", false)

    /**
     * Whether to print "NO REFUND" on Void slip.
     */
    val voidSlipPrintNoRefund =
        SharedPreferencesUtil.getBoolean(PREFIX + ".voidSlipPrintNoRefund", true)

    /**
     * Font file
     */
    const val FONT_PATH = "fonts/THSarabunNew.ttf"

    /**
     * Print Slip width
     */
    const val PRINT_WIDTH = 384
}