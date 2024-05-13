package com.evp.payment.ksher.function.payment.view

import android.app.Activity

interface PaymentSelectorContact {
    interface View {
        fun initMenu()
        fun showAmountFromCustomerScan()
        fun hideAmountFromBusinessScan()
        fun linePayFunction()
        fun promptPayFunction()
        fun aliPayFunction()
        fun wechatPayFunction()
        fun trueMoneyFunction()
        fun shopeeFunction()
    }
}