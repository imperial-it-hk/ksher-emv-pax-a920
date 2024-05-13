package com.evp.payment.ksher.parameter

import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil

object PasswordParam : AbstractParam() {
    private const val PREFIX = "PasswordParam"

    @JvmStatic
    val merchant = SharedPreferencesUtil.getString("$PREFIX.merchant", "")

    @JvmStatic
    val void = SharedPreferencesUtil.getString("$PREFIX.void", "")

    @JvmStatic
    val settlement = SharedPreferencesUtil.getString("$PREFIX.settlement", "")

    @JvmStatic
    val admin = SharedPreferencesUtil.getString("$PREFIX.admin", "")

}