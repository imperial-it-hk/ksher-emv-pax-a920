package com.evp.payment.ksher.function.settings.view

import android.app.Activity

interface PasswordContact {
    interface View {
        fun initMenu()
        fun merchantFunction()
        fun voidAndRefundFunction()
        fun settlementFunction()
    }
}