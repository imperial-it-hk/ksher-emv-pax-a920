package com.evp.payment.ksher.function.main.view

import android.app.Activity

interface MainContact {
    interface View {
        fun initMenu()
        fun inputFunction()
        fun scanFunction()
        fun voidFunction()
        fun settlementFunction()
        fun queryFunction()
        fun printFunction()
        fun reportFunction()
        fun moreFunction()
    }
}