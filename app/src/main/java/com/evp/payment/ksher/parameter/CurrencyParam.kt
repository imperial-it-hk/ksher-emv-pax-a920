package com.evp.payment.ksher.parameter

import com.evp.payment.ksher.parameter.Currency.queryCurrencyByName
import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil

object CurrencyParam : AbstractParam() {
    private const val PREFIX = "CurrencyParam"
    private val defaultCurrency = Currency.THB

    /**
     * Currency name
     */
    val currencyName = SharedPreferencesUtil.getString("$PREFIX.currencyName", defaultCurrency.name)

    /**
     * Get current currency type
     */
    val currency: Currency
        get() = queryCurrencyByName(currencyName.get()!!, defaultCurrency)
}