package com.evp.payment.ksher.parameter

import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil

object TerminalParam : AbstractParam() {
    private const val PREFIX = "TerminalParam"

    @JvmStatic
    val number = SharedPreferencesUtil.getString("$PREFIX.number", "123456")
}