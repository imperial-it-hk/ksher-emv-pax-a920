package com.evp.payment.ksher.function.settings.view

import android.app.Activity

interface ConfigContact {
    interface View {
        fun initMenu()
        fun communicationTypeFunction()
        fun commFunction()
        fun acquirerFunction()
        fun passwordAdminFunction()
        fun transactionSetting()
        fun otherFunction()
    }
}